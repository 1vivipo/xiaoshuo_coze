package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.data.model.Character
import com.bihe.app.data.model.Project
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CharactersViewModel : ViewModel() {
    
    private val database by lazy { BiHeApplication.instance.database }
    
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project
    
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters
    
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
                
                val characterList = database.characterDao().getCharactersByProject(projectId).first()
                _characters.value = characterList
                
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addCharacter(name: String, alias: String, description: String, personality: String, background: String) {
        val proj = _project.value ?: return
        viewModelScope.launch {
            try {
                val character = Character(
                    projectId = proj.id,
                    name = name,
                    alias = alias,
                    description = description,
                    personality = personality,
                    background = background
                )
                database.characterDao().insertCharacter(character)
                
                val characterList = database.characterDao().getCharactersByProject(proj.id).first()
                _characters.value = characterList
                
            } catch (e: Exception) {
                _error.value = "添加失败: ${e.message}"
            }
        }
    }
    
    fun deleteCharacter(character: Character) {
        viewModelScope.launch {
            try {
                database.characterDao().deleteCharacter(character)
                
                val characterList = database.characterDao().getCharactersByProject(character.projectId).first()
                _characters.value = characterList
                
            } catch (e: Exception) {
                _error.value = "删除失败: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
