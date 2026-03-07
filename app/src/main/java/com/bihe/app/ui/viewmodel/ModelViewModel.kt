package com.bihe.app.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bihe.app.BiHeApplication
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ModelViewModel : ViewModel() {
    
    companion object {
        private const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        
        private const val LOCAL_MODEL_NAME = "Dirty-Muse-Writer-v01-Uncensored-Erotica-NSFW.Q2_K.gguf"
        private const val LOCAL_MODEL_URL = "https://huggingface.co/TheDrummer/Dark-Muse-Writer-v01-GGUF/resolve/main/Dirty-Muse-Writer-v01-Uncensored-Erotica-NSFW.Q2_K.gguf"
    }
    
    private val context by lazy { BiHeApplication.instance }
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
    
    val apiKey: StateFlow<String> = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_API_KEY)
    
    val baseUrl: StateFlow<String> = settingsRepository.baseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_BASE_URL)
    
    val model: StateFlow<String> = settingsRepository.modelName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek-chat")
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success
    
    private val _localModelStatus = MutableStateFlow("checking")
    val localModelStatus: StateFlow<String> = _localModelStatus
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress
    
    private var downloadId: Long = -1
    
    init {
        checkLocalModel()
    }
    
    private fun checkLocalModel() {
        viewModelScope.launch {
            try {
                val modelDir = File(context.filesDir, "models")
                val modelFile = File(modelDir, LOCAL_MODEL_NAME)
                
                _localModelStatus.value = if (modelFile.exists() && modelFile.length() > 100000000) {
                    "downloaded"
                } else {
                    "not_downloaded"
                }
            } catch (e: Exception) {
                _localModelStatus.value = "not_downloaded"
            }
        }
    }
    
    fun downloadLocalModel() {
        viewModelScope.launch {
            try {
                _localModelStatus.value = "downloading"
                _downloadProgress.value = 0f
                _error.value = null
                
                val modelDir = File(context.filesDir, "models")
                if (!modelDir.exists()) modelDir.mkdirs()
                
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                
                val request = DownloadManager.Request(Uri.parse(LOCAL_MODEL_URL))
                    .setTitle("Dirty-Muse-Writer 模型")
                    .setDescription("正在下载本地写作模型...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, LOCAL_MODEL_NAME)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                
                downloadId = downloadManager.enqueue(request)
                monitorDownload(downloadManager)
                
            } catch (e: Exception) {
                _error.value = "下载失败: ${e.message}"
                _localModelStatus.value = "not_downloaded"
            }
        }
    }
    
    private fun monitorDownload(downloadManager: DownloadManager) {
        viewModelScope.launch {
            var downloading = true
            
            while (downloading) {
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor: Cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        
                        if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                            val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                            val bytesTotal = cursor.getLong(bytesTotalIndex)
                            
                            if (bytesTotal > 0) {
                                _downloadProgress.value = (bytesDownloaded * 100f / bytesTotal)
                            }
                        }
                        
                        if (statusIndex != -1) {
                            when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    downloading = false
                                    _downloadProgress.value = 100f
                                    _localModelStatus.value = "downloaded"
                                    _success.value = "模型下载完成！"
                                    moveModelToAppDir()
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    downloading = false
                                    _error.value = "下载失败"
                                    _localModelStatus.value = "not_downloaded"
                                }
                            }
                        }
                    }
                    
                    cursor.close()
                } catch (e: Exception) {
                    // 忽略查询错误
                }
                
                delay(500)
            }
        }
    }
    
    private fun moveModelToAppDir() {
        try {
            val sourceFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), LOCAL_MODEL_NAME)
            val modelDir = File(context.filesDir, "models")
            if (!modelDir.exists()) modelDir.mkdirs()
            
            val destFile = File(modelDir, LOCAL_MODEL_NAME)
            
            if (sourceFile.exists()) {
                sourceFile.copyTo(destFile, overwrite = true)
                sourceFile.delete()
            }
        } catch (e: Exception) {
            _error.value = "移动模型文件失败: ${e.message}"
        }
    }
    
    fun updateApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(key.ifBlank { DEFAULT_API_KEY })
        }
    }
    
    fun updateBaseUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setBaseUrl(url.ifBlank { DEFAULT_BASE_URL })
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
                // 在IO线程执行网络请求
                val result = withContext(Dispatchers.IO) {
                    try {
                        val key = apiKey.value.ifBlank { DEFAULT_API_KEY }
                        val url = baseUrl.value.ifBlank { DEFAULT_BASE_URL }
                        
                        val service = DeepSeekService(key, url)
                        service.chat(
                            messages = listOf(
                                com.bihe.app.domain.ai.Message(
                                    role = "user",
                                    content = "你好，请回复'测试成功'"
                                )
                            ),
                            maxTokens = 20
                        )
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
                
                result.fold(
                    onSuccess = { response ->
                        _success.value = "✅ 连接成功！响应: ${response.take(100)}"
                    },
                    onFailure = { e ->
                        _error.value = "❌ 连接失败: ${e.message ?: "未知错误"}"
                    }
                )
                
            } catch (e: Exception) {
                _error.value = "❌ 测试异常: ${e.message ?: "未知错误"}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _success.value = null
    }
}
