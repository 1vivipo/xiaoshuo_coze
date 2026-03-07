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
        private const val DEFAULT_BASE_URL = "https://api.deepseek.com/"
    }
    
    // 直接使用MutableStateFlow，不依赖Repository
    private val _apiKey = MutableStateFlow(DEFAULT_API_KEY)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    private val _baseUrl = MutableStateFlow(DEFAULT_BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()
    
    private val _model = MutableStateFlow("deepseek-chat")
    val model: StateFlow<String> = _model.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success.asStateFlow()
    
    private val _localModelStatus = MutableStateFlow("not_downloaded")
    val localModelStatus: StateFlow<String> = _localModelStatus.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    private var downloadId: Long = -1
    
    private val context: Context by lazy { BiHeApplication.instance }
    
    init {
        Log.d(TAG, "ModelViewModel 初始化")
        checkLocalModel()
    }
    
    private fun checkLocalModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val modelDir = File(context.filesDir, "models")
                val files = modelDir.listFiles()?.filter { it.extension == "gguf" }
                _localModelStatus.value = if (!files.isNullOrEmpty()) "downloaded" else "not_downloaded"
                Log.d(TAG, "本地模型状态: ${_localModelStatus.value}")
            } catch (e: Exception) {
                Log.e(TAG, "检查本地模型失败", e)
                _localModelStatus.value = "not_downloaded"
            }
        }
    }
    
    fun downloadLocalModel(modelUrl: String) {
        Log.d(TAG, "开始下载模型: $modelUrl")
        
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
                Log.d(TAG, "下载任务已加入队列: $downloadId")
                
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
                            when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_RUNNING -> {
                                    if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                                        val downloaded = cursor.getLong(bytesDownloadedIndex)
                                        val total = cursor.getLong(bytesTotalIndex)
                                        if (total > 0) {
                                            val progress = (downloaded * 100f / total)
                                            if (progress - lastProgress > 1f) {
                                                lastProgress = progress
                                                _downloadProgress.value = progress
                                                Log.d(TAG, "下载进度: $progress%")
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
                                    _error.value = "下载失败"
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
        _apiKey.value = key.ifBlank { DEFAULT_API_KEY }
        Log.d(TAG, "API Key 已更新")
    }
    
    fun updateBaseUrl(url: String) {
        _baseUrl.value = url.ifBlank { DEFAULT_BASE_URL }
        Log.d(TAG, "Base URL 已更新: ${_baseUrl.value}")
    }
    
    fun updateModel(model: String) {
        _model.value = model
        Log.d(TAG, "Model 已更新: $model")
    }
    
    fun testConnection() {
        Log.d(TAG, "========== 开始测试连接 ==========")
        
        // 立即显示加载状态
        _isLoading.value = true
        _error.value = null
        _success.value = null
        
        // 显示Toast
        Toast.makeText(context, "正在测试连接...", Toast.LENGTH_SHORT).show()
        
        viewModelScope.launch {
            try {
                val key = _apiKey.value.ifBlank { DEFAULT_API_KEY }
                val url = _baseUrl.value.ifBlank { DEFAULT_BASE_URL }
                
                Log.d(TAG, "API Key: ${key.take(10)}...")
                Log.d(TAG, "Base URL: $url")
                Log.d(TAG, "开始创建DeepSeek服务...")
                
                // 在IO线程执行网络请求
                val result = withContext(Dispatchers.IO) {
                    try {
                        Log.d(TAG, "IO线程: 创建DeepSeekService")
                        val service = DeepSeekService(key, url)
                        
                        Log.d(TAG, "IO线程: 发送请求")
                        val response = service.chat(
                            messages = listOf(
                                com.bihe.app.domain.ai.Message(
                                    role = "user",
                                    content = "请回复：测试成功"
                                )
                            ),
                            maxTokens = 20
                        )
                        
                        Log.d(TAG, "IO线程: 请求完成")
                        response
                    } catch (e: Exception) {
                        Log.e(TAG, "IO线程: 请求异常", e)
                        Result.failure(e)
                    }
                }
                
                Log.d(TAG, "回到主线程处理结果")
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "成功! 响应: $response")
                        _success.value = "✅ 连接成功！响应: ${response.take(100)}"
                        Toast.makeText(context, "连接成功！", Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Log.e(TAG, "失败: ${e.message}", e)
                        val errorMsg = when {
                            e.message?.contains("Unable to resolve host") == true -> "无法连接服务器，请检查网络"
                            e.message?.contains("timeout") == true -> "连接超时，请重试"
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
                Log.d(TAG, "========== 测试结束 ==========")
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
