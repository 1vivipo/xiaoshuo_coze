package com.bihe.app.ui.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
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
        private const val TAG = "ModelViewModel"
        private const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com"
    }
    
    private val context by lazy { BiHeApplication.instance }
    private val settingsRepository by lazy { BiHeApplication.instance.settingsRepository }
    
    val apiKey: StateFlow<String> = settingsRepository.apiKey
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_API_KEY)
    
    val baseUrl: StateFlow<String> = settingsRepository.baseUrl
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_BASE_URL)
    
    val model: StateFlow<String> = settingsRepository.modelName
        .stateIn(viewModelScope, SharingStarted.Lazily, "deepseek-chat")
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success
    
    private val _localModelStatus = MutableStateFlow("not_downloaded")
    val localModelStatus: StateFlow<String> = _localModelStatus
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress
    
    private var downloadId: Long = -1
    
    init {
        checkLocalModel()
    }
    
    private fun checkLocalModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelDir = File(context.filesDir, "models")
                val files = modelDir.listFiles()?.filter { it.extension == "gguf" }
                _localModelStatus.value = if (!files.isNullOrEmpty()) "downloaded" else "not_downloaded"
            } catch (e: Exception) {
                Log.e(TAG, "检查本地模型失败", e)
                _localModelStatus.value = "not_downloaded"
            }
        }
    }
    
    fun downloadLocalModel(modelUrl: String = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf") {
        viewModelScope.launch {
            try {
                _localModelStatus.value = "downloading"
                _downloadProgress.value = 0f
                _error.value = null
                
                Toast.makeText(context, "开始下载模型...", Toast.LENGTH_SHORT).show()
                
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                
                val fileName = modelUrl.substringAfterLast("/")
                val request = DownloadManager.Request(Uri.parse(modelUrl))
                    .setTitle("笔核 - 本地模型")
                    .setDescription("正在下载: $fileName")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                
                downloadId = downloadManager.enqueue(request)
                monitorDownload(downloadManager, fileName)
                
            } catch (e: Exception) {
                Log.e(TAG, "下载失败", e)
                _error.value = "下载失败: ${e.message}"
                _localModelStatus.value = "not_downloaded"
                Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun monitorDownload(downloadManager: DownloadManager, fileName: String) {
        viewModelScope.launch {
            var downloading = true
            var lastProgress = 0f
            
            while (downloading) {
                try {
                    val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                    
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        
                        if (statusIndex != -1) {
                            val status = cursor.getInt(statusIndex)
                            
                            when (status) {
                                DownloadManager.STATUS_RUNNING -> {
                                    if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                                        val downloaded = cursor.getLong(bytesDownloadedIndex)
                                        val total = cursor.getLong(bytesTotalIndex)
                                        if (total > 0) {
                                            val progress = (downloaded * 100f / total)
                                            if (progress - lastProgress > 1f) {
                                                lastProgress = progress
                                                _downloadProgress.value = progress
                                            }
                                        }
                                    }
                                }
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    downloading = false
                                    _downloadProgress.value = 100f
                                    _localModelStatus.value = "downloaded"
                                    _success.value = "模型下载完成！"
                                    Toast.makeText(context, "模型下载完成！", Toast.LENGTH_LONG).show()
                                    moveModelToAppDir(fileName)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    downloading = false
                                    val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                                    val reason = if (reasonIndex != -1) cursor.getInt(reasonIndex) else -1
                                    _error.value = "下载失败 (错误码: $reason)"
                                    _localModelStatus.value = "not_downloaded"
                                    Toast.makeText(context, "下载失败，请重试", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    
                    cursor.close()
                } catch (e: Exception) {
                    Log.e(TAG, "监控下载失败", e)
                }
                
                delay(500)
            }
        }
    }
    
    private fun moveModelToAppDir(fileName: String) {
        try {
            val sourceFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val modelDir = File(context.filesDir, "models")
            if (!modelDir.exists()) modelDir.mkdirs()
            
            val destFile = File(modelDir, fileName)
            
            if (sourceFile.exists()) {
                sourceFile.copyTo(destFile, overwrite = true)
                sourceFile.delete()
                Log.d(TAG, "模型已移动到: ${destFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "移动模型失败", e)
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
        Log.d(TAG, "=== 开始测试连接 ===")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null
            
            Toast.makeText(context, "正在测试连接...", Toast.LENGTH_SHORT).show()
            
            try {
                val key = apiKey.value.ifBlank { DEFAULT_API_KEY }
                val url = baseUrl.value.ifBlank { DEFAULT_BASE_URL }
                
                Log.d(TAG, "API Key: ${key.take(10)}...")
                Log.d(TAG, "Base URL: $url")
                Log.d(TAG, "开始网络请求...")
                
                val result = withContext(Dispatchers.IO) {
                    try {
                        val service = DeepSeekService(key, url)
                        service.chat(
                            messages = listOf(
                                com.bihe.app.domain.ai.Message(
                                    role = "user",
                                    content = "请回复：测试成功"
                                )
                            ),
                            maxTokens = 20
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "网络请求异常", e)
                        Result.failure(e)
                    }
                }
                
                Log.d(TAG, "网络请求完成")
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "成功: $response")
                        _success.value = "✅ 连接成功！响应: ${response.take(100)}"
                        Toast.makeText(context, "连接成功！", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Log.e(TAG, "失败: ${e.message}", e)
                        val errorMsg = when {
                            e.message?.contains("Unable to resolve host") == true -> "网络错误：无法连接服务器，请检查网络"
                            e.message?.contains("timeout") == true -> "网络超时，请重试"
                            e.message?.contains("401") == true -> "API Key无效"
                            e.message?.contains("402") == true -> "API余额不足"
                            else -> "连接失败: ${e.message}"
                        }
                        _error.value = "❌ $errorMsg"
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "测试异常", e)
                _error.value = "❌ 测试异常: ${e.message}"
                Toast.makeText(context, "测试异常: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== 测试结束 ===")
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
