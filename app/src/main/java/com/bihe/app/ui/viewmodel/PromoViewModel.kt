package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PromoViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        loadProjects()
    }
    
    private fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allProjects = database.projectDao().getAllProjects().first()
                _projects.value = allProjects.filter { it.type == ProjectType.PROMO_COPY }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createPromoProject(title: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = Project(
                    name = title,
                    type = ProjectType.PROMO_COPY,
                    description = content,
                    totalWordGoal = content.length
                )
                database.projectDao().insertProject(project)
                loadProjects()
            } catch (e: Exception) {
                _error.value = "创建失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateVideo(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: 实现视频生成
                _error.value = "视频生成功能开发中..."
            } catch (e: Exception) {
                _error.value = "生成失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateAllVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = "批量生成功能开发中..."
            _isLoading.value = false
        }
    }
    
    fun exportAll() {
        viewModelScope.launch {
            _error.value = "导出功能开发中..."
        }
    }
    
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            database.projectDao().deleteProject(project)
            loadProjects()
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
