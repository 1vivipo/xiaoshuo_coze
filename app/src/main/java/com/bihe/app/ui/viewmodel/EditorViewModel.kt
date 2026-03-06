package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Chapter
import com.bihe.app.data.model.Project
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    private var deepSeekService: DeepSeekService? = null
    
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
    
    fun loadProject(projectId: Long, chapterId: Long? = null) {
        viewModelScope.launch {
            val proj = database.projectDao().getProjectById(projectId)
            _project.value = proj
            
            val chapterList = database.chapterDao().getChaptersByProject(projectId).first()
            _chapters.value = chapterList
            
            if (chapterId != null) {
                val chapter = database.chapterDao().getChapterById(chapterId)
                _currentChapter.value = chapter
            } else if (chapterList.isNotEmpty()) {
                _currentChapter.value = chapterList.first()
            }
            
            // 初始化DeepSeek服务
            val apiKey = BiHeApplication.instance.settingsRepository.apiKey.first()
            deepSeekService = DeepSeekService(apiKey)
        }
    }
    
    fun selectChapter(chapter: Chapter) {
        _currentChapter.value = chapter
    }
    
    fun updateContent(content: String) {
        val chapter = _currentChapter.value ?: return
        viewModelScope.launch {
            database.chapterDao().updateChapter(
                chapter.copy(
                    content = content,
                    wordCount = content.length,
                    updatedAt = System.currentTimeMillis()
                )
            )
            database.projectDao().updateWordCount(chapter.projectId)
        }
    }
    
    fun createChapter(title: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            val maxOrder = database.chapterDao().getMaxOrderIndex(proj.id) ?: 0
            val chapter = Chapter(
                projectId = proj.id,
                title = title,
                orderIndex = maxOrder + 1
            )
            val id = database.chapterDao().insertChapter(chapter)
            val newChapter = chapter.copy(id = id)
            _currentChapter.value = newChapter
            
            val chapterList = database.chapterDao().getChaptersByProject(proj.id).first()
            _chapters.value = chapterList
        }
    }
    
    fun startAIWriting(currentContent: String) {
        val chapter = _currentChapter.value ?: return
        val proj = _project.value ?: return
        val service = deepSeekService ?: return
        
        viewModelScope.launch {
            _isWriting.value = true
            _writingProgress.value = 0f
            
            try {
                // 获取人物设定
                val characters = database.characterDao()
                    .getCharactersByProject(proj.id)
                    .first()
                    .joinToString("\n") { "${it.name}：${it.description}" }
                
                // 获取世界观设定
                val worldSettings = database.worldSettingDao()
                    .getWorldSettingsByProject(proj.id)
                    .first()
                    .joinToString("\n") { "${it.title}：${it.content}" }
                
                var totalContent = currentContent
                val targetWords = 2000
                
                while (totalContent.length < targetWords) {
                    _writingProgress.value = (totalContent.length.toFloat() / targetWords) * 100
                    
                    val result = service.continueWriting(
                        context = totalContent.takeLast(2000),
                        outline = chapter.outline,
                        characters = characters,
                        worldSetting = worldSettings,
                        targetWords = minOf(500, targetWords - totalContent.length)
                    )
                    
                    result.fold(
                        onSuccess = { newContent ->
                            totalContent += "\n$newContent"
                            updateContent(totalContent)
                        },
                        onFailure = { error ->
                            _isWriting.value = false
                            return@launch
                        }
                    )
                }
                
                _writingProgress.value = 100f
                _isWriting.value = false
                
            } catch (e: Exception) {
                _isWriting.value = false
            }
        }
    }
    
    fun getApiKey(): String {
        return BiHeApplication.instance.settingsRepository.apiKey.first()
    }
    
    fun updateApiKey(key: String) {
        viewModelScope.launch {
            BiHeApplication.instance.settingsRepository.setApiKey(key)
            deepSeekService = DeepSeekService(key)
        }
    }
}
