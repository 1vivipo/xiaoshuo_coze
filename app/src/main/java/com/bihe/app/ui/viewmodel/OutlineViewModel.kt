package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Chapter
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.Volume
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OutlineViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project
    
    private val _volumes = MutableStateFlow<List<Volume>>(emptyList())
    val volumes: StateFlow<List<Volume>> = _volumes
    
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters
    
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
                
                val volumeList = database.volumeDao().getVolumesByProject(projectId).first()
                _volumes.value = volumeList
                
                val chapterList = database.chapterDao().getChaptersByProject(projectId).first()
                _chapters.value = chapterList
                
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addVolume(title: String, outline: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            try {
                val maxOrder = _volumes.value.maxOfOrNull { it.orderIndex } ?: 0
                val volume = Volume(
                    projectId = proj.id,
                    title = title,
                    outline = outline,
                    orderIndex = maxOrder + 1
                )
                database.volumeDao().insertVolume(volume)
                
                val volumeList = database.volumeDao().getVolumesByProject(proj.id).first()
                _volumes.value = volumeList
                
            } catch (e: Exception) {
                _error.value = "添加失败: ${e.message}"
            }
        }
    }
    
    fun addChapter(title: String, outline: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            try {
                val maxOrder = database.chapterDao().getMaxOrderIndex(proj.id)
                val chapter = Chapter(
                    projectId = proj.id,
                    title = title,
                    outline = outline,
                    orderIndex = maxOrder + 1
                )
                database.chapterDao().insertChapter(chapter)
                
                val chapterList = database.chapterDao().getChaptersByProject(proj.id).first()
                _chapters.value = chapterList
                
            } catch (e: Exception) {
                _error.value = "添加失败: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
