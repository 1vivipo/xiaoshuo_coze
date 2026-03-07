package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.WorldSetting
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorldSettingViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project
    
    private val _settings = MutableStateFlow<List<WorldSetting>>(emptyList())
    val settings: StateFlow<List<WorldSetting>> = _settings
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val proj = database.projectDao().getProjectById(projectId)
                _project.value = proj
                
                val settingList = database.worldSettingDao().getWorldSettingsByProject(projectId).first()
                _settings.value = settingList
                
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addSetting(title: String, category: String, content: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            try {
                val maxOrder = _settings.value.maxOfOrNull { it.orderIndex } ?: 0
                val setting = WorldSetting(
                    projectId = proj.id,
                    title = title,
                    category = category,
                    content = content,
                    orderIndex = maxOrder + 1
                )
                database.worldSettingDao().insertWorldSetting(setting)
                
                val settingList = database.worldSettingDao().getWorldSettingsByProject(proj.id).first()
                _settings.value = settingList
                
            } catch (e: Exception) {
                _error.value = "添加失败: ${e.message}"
            }
        }
    }
    
    fun deleteSetting(setting: WorldSetting) {
        viewModelScope.launch {
            try {
                database.worldSettingDao().deleteWorldSetting(setting)
                
                val settingList = database.worldSettingDao().getWorldSettingsByProject(setting.projectId).first()
                _settings.value = settingList
                
            } catch (e: Exception) {
                _error.value = "删除失败: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
