package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.DramaEpisode
import com.bihe.app.data.model.EpisodeStatus
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DramaViewModel : ViewModel() {
    
    companion object {
        private const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
    }
    
    private val database by lazy { BiHeApplication.instance.database }
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
    
    private val _episodes = MutableStateFlow<List<DramaEpisode>>(emptyList())
    val episodes: StateFlow<List<DramaEpisode>> = _episodes
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var currentProjectId: Long = 0
    private var deepSeekService: DeepSeekService? = null
    
    init {
        deepSeekService = DeepSeekService(DEFAULT_API_KEY, DEFAULT_BASE_URL)
        loadDramaProject()
    }
    
    private fun loadDramaProject() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projects = database.projectDao().getAllProjects().first()
                val dramaProject = projects.find { it.type == ProjectType.COMIC_DRAMA }
                
                if (dramaProject != null) {
                    currentProjectId = dramaProject.id
                    val episodeList = database.dramaDao().getEpisodesByProject(dramaProject.id).first()
                    _episodes.value = episodeList
                }
                
                if (deepSeekService == null) {
                    val apiKey = settingsRepository.apiKey.first().ifBlank { DEFAULT_API_KEY }
                    deepSeekService = DeepSeekService(apiKey)
                }
                
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createDramaProject(title: String, episodeCount: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = Project(
                    name = title,
                    type = ProjectType.COMIC_DRAMA,
                    totalWordGoal = episodeCount * 800
                )
                val projectId = database.projectDao().insertProject(project)
                currentProjectId = projectId
                
                val episodeList = (1..episodeCount).map { i ->
                    DramaEpisode(
                        projectId = projectId,
                        episodeNumber = i,
                        title = "第${i}集"
                    )
                }
                
                episodeList.forEach { episode ->
                    database.dramaDao().insertEpisode(episode)
                }
                
                val savedEpisodes = database.dramaDao().getEpisodesByProject(projectId).first()
                _episodes.value = savedEpisodes
                
            } catch (e: Exception) {
                _error.value = "创建失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateScript(episode: DramaEpisode) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (deepSeekService == null) {
                    val apiKey = settingsRepository.apiKey.first().ifBlank { DEFAULT_API_KEY }
                    deepSeekService = DeepSeekService(apiKey)
                }
                
                // 在IO线程执行网络请求
                val result = withContext(Dispatchers.IO) {
                    deepSeekService!!.chat(
                        messages = listOf(
                            com.bihe.app.domain.ai.Message(
                                role = "system",
                                content = "你是一位专业的短剧编剧，擅长创作节奏紧凑、情感丰富的短剧剧本。"
                            ),
                            com.bihe.app.domain.ai.Message(
                                role = "user",
                                content = "请为第${episode.episodeNumber}集《${episode.title}》创作剧本，约800字，包含对话和场景描述。直接输出剧本内容。"
                            )
                        ),
                        maxTokens = 1500
                    )
                }
                
                result.fold(
                    onSuccess = { script ->
                        database.dramaDao().updateEpisode(
                            episode.copy(
                                script = script,
                                status = EpisodeStatus.SCRIPT
                            )
                        )
                        loadDramaProject()
                    },
                    onFailure = { e ->
                        _error.value = "生成失败: ${e.message}"
                    }
                )
                
            } catch (e: Exception) {
                _error.value = "生成失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateStoryboard(episode: DramaEpisode) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (episode.script.isBlank()) {
                    _error.value = "请先生成剧本"
                    return@launch
                }
                
                database.dramaDao().updateEpisode(
                    episode.copy(status = EpisodeStatus.STORYBOARD)
                )
                loadDramaProject()
                
            } catch (e: Exception) {
                _error.value = "生成失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateAllScripts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = "批量生成功能开发中..."
            _isLoading.value = false
        }
    }
    
    fun generateAllStoryboards() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = "批量生成功能开发中..."
            _isLoading.value = false
        }
    }
    
    fun deleteEpisode(episode: DramaEpisode) {
        viewModelScope.launch {
            database.dramaDao().deleteEpisode(episode)
            loadDramaProject()
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
