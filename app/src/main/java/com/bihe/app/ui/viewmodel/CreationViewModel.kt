package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CreationViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _selectedType = MutableStateFlow<ProjectType?>(null)
    val selectedType: StateFlow<ProjectType?> = _selectedType
    
    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog
    
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    val projects: StateFlow<List<Project>> = _selectedType
        .flatMapLatest { type ->
            database.projectDao().getAllProjects().map { list ->
                if (type != null) list.filter { it.type == type } else list
            }
        }
        .catch { e -> _error.value = e.message }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        // 立即开始收集数据
        viewModelScope.launch {
            projects.collect()
        }
    }
    
    fun filterByType(type: ProjectType?) {
        _selectedType.value = type
    }
    
    fun showCreateDialog() {
        _showCreateDialog.value = true
    }
    
    fun hideCreateDialog() {
        _showCreateDialog.value = false
    }
    
    fun selectProject(project: Project) {
        _selectedProject.value = project
    }
    
    fun clearSelectedProject() {
        _selectedProject.value = null
    }
    
    fun createProject(name: String, type: ProjectType) {
        if (name.isBlank()) {
            _error.value = "项目名称不能为空"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val project = Project(
                    name = name.trim(),
                    type = type,
                    totalWordGoal = if (type == ProjectType.NOVEL) 100000 else 50000
                )
                database.projectDao().insertProject(project)
                _showCreateDialog.value = false
                _error.value = null
            } catch (e: Exception) {
                _error.value = "创建失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                database.projectDao().deleteProject(project)
                _selectedProject.value = null
                _error.value = null
            } catch (e: Exception) {
                _error.value = "删除失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
