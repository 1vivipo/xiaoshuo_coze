package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreationViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _selectedType = MutableStateFlow<ProjectType?>(null)
    val selectedType: StateFlow<ProjectType?> = _selectedType
    
    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog
    
    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject
    
    val projects: StateFlow<List<Project>> = _selectedType
        .flatMapLatest { type ->
            database.projectDao().getAllProjects().map { list ->
                if (type != null) list.filter { it.type == type } else list
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
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
        viewModelScope.launch {
            val project = Project(
                name = name,
                type = type,
                totalWordGoal = if (type == ProjectType.NOVEL) 100000 else 50000
            )
            database.projectDao().insertProject(project)
            _showCreateDialog.value = false
        }
    }
    
    fun deleteProject(project: Project) {
        viewModelScope.launch {
            database.projectDao().deleteProject(project)
            _selectedProject.value = null
        }
    }
}
