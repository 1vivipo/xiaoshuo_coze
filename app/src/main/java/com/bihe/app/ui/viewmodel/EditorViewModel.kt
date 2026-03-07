package com.bihe.app.ui.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Chapter
import com.bihe.app.data.model.Project
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "EditorViewModel"
        private const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com/"
    }
    
    private val database by lazy { BiHeApplication.instance.database }
    private val context by lazy { BiHeApplication.instance }
    
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project
    
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters
    
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    val currentChapter: StateFlow<Chapter?> = _currentChapter
    
    private val _isWriting = MutableStateFlow(false)
    val isWriting: StateFlow<Boolean> = _isWriting
    
    private val _writingProgress = MutableStateFlow(0f)
    val writingProgress: StateFlow<Float> = _writingProgress
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content
    
    private var deepSeekService: DeepSeekService? = null
    
    init {
        Log.d(TAG, "EditorViewModel 初始化")
        deepSeekService = DeepSeekService(DEFAULT_API_KEY, DEFAULT_BASE_URL)
    }
    
    fun loadProject(projectId: Long, chapterId: Long? = null) {
        Log.d(TAG, "加载项目: $projectId")
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proj = database.projectDao().getProjectById(projectId)
                _project.value = proj
                Log.d(TAG, "项目加载成功: ${proj?.name}")
                
                val chapterList = database.chapterDao().getChaptersByProject(projectId).first()
                _chapters.value = chapterList
                Log.d(TAG, "章节数: ${chapterList.size}")
                
                if (chapterId != null) {
                    val chapter = database.chapterDao().getChapterById(chapterId)
                    _currentChapter.value = chapter
                    _content.value = chapter?.content ?: ""
                } else if (chapterList.isNotEmpty()) {
                    _currentChapter.value = chapterList.first()
                    _content.value = chapterList.first().content
                } else {
                    val newChapter = Chapter(
                        projectId = projectId,
                        title = "第一章",
                        orderIndex = 1
                    )
                    val id = database.chapterDao().insertChapter(newChapter)
                    val createdChapter = newChapter.copy(id = id)
                    _currentChapter.value = createdChapter
                    _chapters.value = listOf(createdChapter)
                    _content.value = ""
                    Log.d(TAG, "创建第一章")
                }
                
                _error.value = null
            } catch (e: Exception) {
                Log.e(TAG, "加载失败", e)
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun selectChapter(chapter: Chapter) {
        _currentChapter.value = chapter
        _content.value = chapter.content
    }
    
    fun updateContent(newContent: String) {
        _content.value = newContent
        
        val chapter = _currentChapter.value ?: return
        viewModelScope.launch {
            try {
                database.chapterDao().updateChapter(
                    chapter.copy(
                        content = newContent,
                        wordCount = newContent.length,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                database.projectDao().updateWordCount(chapter.projectId)
                
                _currentChapter.value = chapter.copy(
                    content = newContent,
                    wordCount = newContent.length
                )
            } catch (e: Exception) {
                Log.e(TAG, "保存失败", e)
                _error.value = "保存失败: ${e.message}"
            }
        }
    }
    
    fun createChapter(title: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val maxOrder = database.chapterDao().getMaxOrderIndex(proj.id)
                val chapter = Chapter(
                    projectId = proj.id,
                    title = title,
                    orderIndex = maxOrder + 1
                )
                val id = database.chapterDao().insertChapter(chapter)
                val newChapter = chapter.copy(id = id)
                
                _currentChapter.value = newChapter
                _content.value = ""
                
                val chapterList = database.chapterDao().getChaptersByProject(proj.id).first()
                _chapters.value = chapterList
                
                _error.value = null
                Log.d(TAG, "创建章节: $title")
            } catch (e: Exception) {
                Log.e(TAG, "创建章节失败", e)
                _error.value = "创建章节失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startAIWriting(currentContent: String) {
        Log.d(TAG, "开始AI续写")
        
        val chapter = _currentChapter.value
        val proj = _project.value
        
        if (proj == null) {
            _error.value = "项目未加载"
            Log.e(TAG, "项目未加载")
            return
        }
        
        if (deepSeekService == null) {
            deepSeekService = DeepSeekService(DEFAULT_API_KEY, DEFAULT_BASE_URL)
        }
        
        val service = deepSeekService!!
        
        viewModelScope.launch {
            _isWriting.value = true
            _writingProgress.value = 0f
            _error.value = null
            
            Toast.makeText(context, "开始AI续写...", Toast.LENGTH_SHORT).show()
            
            try {
                val characters = try {
                    database.characterDao()
                        .getCharactersByProject(proj.id)
                        .first()
                        .joinToString("\n") { "${it.name}：${it.description}" }
                } catch (e: Exception) {
                    ""
                }
                
                val worldSettings = try {
                    database.worldSettingDao()
                        .getWorldSettingsByProject(proj.id)
                        .first()
                        .joinToString("\n") { "${it.title}：${it.content}" }
                } catch (e: Exception) {
                    ""
                }
                
                var totalContent = currentContent
                val targetWords = 500
                
                Log.d(TAG, "目标字数: $targetWords")
                
                while (totalContent.length < targetWords && _isWriting.value) {
                    _writingProgress.value = (totalContent.length.toFloat() / targetWords) * 100
                    
                    Log.d(TAG, "发送续写请求...")
                    
                    val result = withContext(Dispatchers.IO) {
                        service.continueWriting(
                            context = if (totalContent.isNotBlank()) totalContent.takeLast(1500) else "请开始写一个精彩的小说开头。",
                            outline = chapter?.outline ?: "",
                            characters = characters,
                            worldSetting = worldSettings,
                            targetWords = 300
                        )
                    }
                    
                    result.fold(
                        onSuccess = { newContent ->
                            if (newContent.isNotBlank()) {
                                totalContent += if (totalContent.isNotBlank()) "\n\n$newContent" else newContent
                                _content.value = totalContent
                                updateContent(totalContent)
                                Log.d(TAG, "续写成功，当前字数: ${totalContent.length}")
                            } else {
                                _error.value = "AI返回空内容"
                                _isWriting.value = false
                                return@launch
                            }
                        },
                        onFailure = { e ->
                            Log.e(TAG, "续写失败", e)
                            _error.value = "AI续写失败: ${e.message}"
                            _isWriting.value = false
                            Toast.makeText(context, "续写失败: ${e.message}", Toast.LENGTH_LONG).show()
                            return@launch
                        }
                    )
                }
                
                _writingProgress.value = 100f
                _isWriting.value = false
                Toast.makeText(context, "续写完成！", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "续写完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "续写出错", e)
                _error.value = "续写出错: ${e.message}"
                _isWriting.value = false
                Toast.makeText(context, "续写出错: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    fun stopWriting() {
        _isWriting.value = false
        Log.d(TAG, "停止续写")
    }
    
    fun getApiKey(): String {
        return DEFAULT_API_KEY
    }
    
    fun updateApiKey(key: String) {
        deepSeekService = DeepSeekService(key.ifBlank { DEFAULT_API_KEY }, DEFAULT_BASE_URL)
        Log.d(TAG, "API Key 已更新")
    }
    
    fun clearError() {
        _error.value = null
    }
}
