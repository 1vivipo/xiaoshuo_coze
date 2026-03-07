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
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
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
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content
    
    private var cachedApiKey: String = ""
    private var cachedBaseUrl: String = "https://api.deepseek.com"
    
    fun loadProject(projectId: Long, chapterId: Long? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载设置
                cachedApiKey = settingsRepository.apiKey.first()
                cachedBaseUrl = settingsRepository.baseUrl.first()
                
                // 初始化DeepSeek服务
                if (cachedApiKey.isNotBlank()) {
                    deepSeekService = DeepSeekService(cachedApiKey, cachedBaseUrl)
                }
                
                // 加载项目
                val proj = database.projectDao().getProjectById(projectId)
                _project.value = proj
                
                // 加载章节
                val chapterList = database.chapterDao().getChaptersByProject(projectId).first()
                _chapters.value = chapterList
                
                if (chapterId != null) {
                    val chapter = database.chapterDao().getChapterById(chapterId)
                    _currentChapter.value = chapter
                    _content.value = chapter?.content ?: ""
                } else if (chapterList.isNotEmpty()) {
                    _currentChapter.value = chapterList.first()
                    _content.value = chapterList.first().content
                } else {
                    // 没有章节，创建第一章
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
                }
                
                _error.value = null
            } catch (e: Exception) {
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
                
                // 更新当前章节引用
                _currentChapter.value = chapter.copy(
                    content = newContent,
                    wordCount = newContent.length
                )
            } catch (e: Exception) {
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
            } catch (e: Exception) {
                _error.value = "创建章节失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun startAIWriting(currentContent: String) {
        val chapter = _currentChapter.value
        val proj = _project.value
        
        if (proj == null) {
            _error.value = "项目未加载"
            return
        }
        
        if (cachedApiKey.isBlank()) {
            _error.value = "请先在「我的」页面设置API Key"
            return
        }
        
        // 确保服务已初始化
        if (deepSeekService == null) {
            deepSeekService = DeepSeekService(cachedApiKey, cachedBaseUrl)
        }
        
        val service = deepSeekService!!
        
        viewModelScope.launch {
            _isWriting.value = true
            _writingProgress.value = 0f
            _error.value = null
            
            try {
                // 获取人物设定
                val characters = try {
                    database.characterDao()
                        .getCharactersByProject(proj.id)
                        .first()
                        .joinToString("\n") { "${it.name}：${it.description}" }
                } catch (e: Exception) {
                    ""
                }
                
                // 获取世界观设定
                val worldSettings = try {
                    database.worldSettingDao()
                        .getWorldSettingsByProject(proj.id)
                        .first()
                        .joinToString("\n") { "${it.title}：${it.content}" }
                } catch (e: Exception) {
                    ""
                }
                
                var totalContent = currentContent
                val targetWords = 2000
                
                while (totalContent.length < targetWords && _isWriting.value) {
                    _writingProgress.value = (totalContent.length.toFloat() / targetWords) * 100
                    
                    val result = service.continueWriting(
                        context = if (totalContent.isNotBlank()) totalContent.takeLast(2000) else "开始写作",
                        outline = chapter?.outline ?: "",
                        characters = characters,
                        worldSetting = worldSettings,
                        targetWords = minOf(500, targetWords - totalContent.length)
                    )
                    
                    result.fold(
                        onSuccess = { newContent ->
                            totalContent += if (totalContent.isNotBlank()) "\n$newContent" else newContent
                            _content.value = totalContent
                            updateContent(totalContent)
                        },
                        onFailure = { e ->
                            _error.value = "AI续写失败: ${e.message}"
                            _isWriting.value = false
                            return@launch
                        }
                    )
                }
                
                _writingProgress.value = 100f
                _isWriting.value = false
                
            } catch (e: Exception) {
                _error.value = "续写出错: ${e.message}"
                _isWriting.value = false
            }
        }
    }
    
    fun stopWriting() {
        _isWriting.value = false
    }
    
    fun getApiKey(): String {
        return cachedApiKey
    }
    
    fun updateApiKey(key: String) {
        cachedApiKey = key
        deepSeekService = DeepSeekService(key, cachedBaseUrl)
        viewModelScope.launch {
            settingsRepository.setApiKey(key)
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
