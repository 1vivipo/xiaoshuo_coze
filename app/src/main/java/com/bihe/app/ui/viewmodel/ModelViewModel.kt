package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ModelViewModel : ViewModel() {
    
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
    
    val apiKey: StateFlow<String> = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val baseUrl: StateFlow<String> = settingsRepository.baseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://api.deepseek.com")
    
    val model: StateFlow<String> = settingsRepository.modelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-chat")
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success
    
    fun updateApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(key)
        }
    }
    
    fun updateBaseUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setBaseUrl(url)
        }
    }
    
    fun updateModel(model: String) {
        viewModelScope.launch {
            settingsRepository.setModelName(model)
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null
            
            try {
                val key = apiKey.value
                val url = baseUrl.value
                
                if (key.isBlank()) {
                    _error.value = "请先设置API Key"
                    return@launch
                }
                
                val service = DeepSeekService(key, url)
                val result = service.chat(
                    messages = listOf(
                        com.bihe.app.domain.ai.Message(
                            role = "user",
                            content = "你好"
                        )
                    ),
                    maxTokens = 10
                )
                
                result.fold(
                    onSuccess = { response ->
                        _success.value = "连接成功！模型响应: ${response.take(50)}"
                    },
                    onFailure = { e ->
                        _error.value = "连接失败: ${e.message}"
                    }
                )
                
            } catch (e: Exception) {
                _error.value = "测试失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun downloadLocalModel() {
        viewModelScope.launch {
            _error.value = "本地模型下载功能开发中..."
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _success.value = null
    }
}
