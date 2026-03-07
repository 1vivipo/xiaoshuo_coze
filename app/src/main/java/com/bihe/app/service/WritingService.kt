package com.bihe.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.bihe.app.BiHeApplication
import com.bihe.app.MainActivity
import com.bihe.app.R
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class WritingService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _writingState = MutableStateFlow<WritingState>(WritingState.Idle)
    val writingState: StateFlow<WritingState> = _writingState
    
    private var currentJob: Job? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_WRITING -> {
                val chapterId = intent.getLongExtra(EXTRA_CHAPTER_ID, -1)
                val targetWords = intent.getIntExtra(EXTRA_TARGET_WORDS, 10000)
                val mode = intent.getStringExtra(EXTRA_MODE) ?: "outline"
                
                startForeground(NOTIFICATION_ID, createNotification("准备续写..."))
                startWriting(chapterId, targetWords, mode)
            }
            ACTION_STOP_WRITING -> {
                stopWriting()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }
    
    private fun startWriting(chapterId: Long, targetWords: Int, mode: String) {
        currentJob = serviceScope.launch {
            _writingState.value = WritingState.Writing(0, targetWords)
            
            try {
                // 获取章节信息
                val database = BiHeApplication.instance.database
                val chapter = database.chapterDao().getChapterById(chapterId)
                    ?: throw Exception("章节不存在")
                
                val project = database.projectDao().getProjectById(chapter.projectId)
                    ?: throw Exception("项目不存在")
                
                // 获取人物设定
                val characters = database.characterDao()
                    .getCharactersByProject(project.id)
                    .first()
                    .joinToString("\n") { "${it.name}：${it.description}" }
                
                // 获取世界观设定
                val worldSettings = database.worldSettingDao()
                    .getWorldSettingsByProject(project.id)
                    .first()
                    .joinToString("\n") { "${it.title}：${it.content}" }
                
                val deepSeekService = DeepSeekService(
                    DeepSeekService.DEFAULT_API_KEY,
                    DeepSeekService.DEFAULT_BASE_URL
                )
                
                var currentContent = chapter.content
                var totalWords = currentContent.length
                
                while (totalWords < targetWords) {
                    updateNotification("续写中... ${totalWords}/${targetWords}字")
                    _writingState.value = WritingState.Writing(totalWords, targetWords)
                    
                    val result = deepSeekService.continueWriting(
                        context = currentContent.takeLast(2000),
                        outline = chapter.outline,
                        characters = characters,
                        worldSetting = worldSettings,
                        targetWords = minOf(2000, targetWords - totalWords)
                    )
                    
                    result.fold(
                        onSuccess = { newContent ->
                            currentContent += "\n$newContent"
                            totalWords = currentContent.length
                            
                            // 更新章节内容
                            database.chapterDao().updateChapter(
                                chapter.copy(
                                    content = currentContent,
                                    wordCount = totalWords,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        },
                        onFailure = { error ->
                            _writingState.value = WritingState.Error(error.message ?: "续写失败")
                            return@launch
                        }
                    )
                }
                
                _writingState.value = WritingState.Completed(totalWords)
                updateNotification("续写完成！共${totalWords}字")
                
            } catch (e: Exception) {
                _writingState.value = WritingState.Error(e.message ?: "续写失败")
                updateNotification("续写失败：${e.message}")
            }
        }
    }
    
    private fun stopWriting() {
        currentJob?.cancel()
        currentJob = null
        _writingState.value = WritingState.Idle
    }
    
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, BiHeApplication.CHANNEL_ID_WRITING)
            .setContentTitle("笔核 - 续写服务")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    sealed class WritingState {
        object Idle : WritingState()
        data class Writing(val current: Int, val target: Int) : WritingState()
        data class Completed(val totalWords: Int) : WritingState()
        data class Error(val message: String) : WritingState()
    }
    
    companion object {
        const val ACTION_START_WRITING = "start_writing"
        const val ACTION_STOP_WRITING = "stop_writing"
        const val EXTRA_CHAPTER_ID = "chapter_id"
        const val EXTRA_TARGET_WORDS = "target_words"
        const val EXTRA_MODE = "mode"
        const val NOTIFICATION_ID = 1001
    }
}
