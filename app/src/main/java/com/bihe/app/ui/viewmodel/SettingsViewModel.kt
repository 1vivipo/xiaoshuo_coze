package com.bihe.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    
    companion object {
        private const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
    }
    
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
    private val database by lazy { BiHeApplication.instance.database }
    
    val apiKey: StateFlow<String> = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_API_KEY)
    
    val baseUrl: StateFlow<String> = settingsRepository.baseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_BASE_URL)
    
    val model: StateFlow<String> = settingsRepository.modelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-chat")
    
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
    
    fun exportData() {
        // TODO: 实现数据导出
    }
    
    fun importData() {
        // TODO: 实现数据导入
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            // 清除所有数据
            database.clearAllTables()
        }
    }
    
    fun checkUpdate() {
        // TODO: 检查更新
    }
    
    fun feedback() {
        // TODO: 打开反馈页面
    }
}
