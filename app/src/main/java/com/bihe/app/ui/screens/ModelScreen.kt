package com.bihe.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe.app.ui.viewmodel.ModelViewModel

data class LocalModel(
    val name: String,
    val size: String,
    val description: String,
    val url: String,
    val isNsfw: Boolean = false,
    val isChineseFriendly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelScreen() {
    val viewModel: ModelViewModel = viewModel()
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val model by viewModel.model.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val localModelStatus by viewModel.localModelStatus.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf<LocalModel?>(null) }
    
    // 可用的本地模型列表
    val localModels = listOf(
        LocalModel(
            name = "Qwen2.5-1.5B",
            size = "~1GB",
            description = "中文友好，轻量快速",
            url = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            isChineseFriendly = true
        ),
        LocalModel(
            name = "Qwen2.5-3B",
            size = "~2GB",
            description = "中文友好，性能平衡",
            url = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
            isChineseFriendly = true
        ),
        LocalModel(
            name = "Phi-3.5-Mini",
            size = "~2GB",
            description = "多语言支持，快速响应",
            url = "https://huggingface.co/microsoft/Phi-3.5-mini-instruct-gguf/resolve/main/Phi-3.5-mini-instruct-q4.gguf"
        ),
        LocalModel(
            name = "Llama-3.2-3B",
            size = "~2GB",
            description = "创意写作，英文优秀",
            url = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf"
        ),
        LocalModel(
            name = "Dirty-Muse-Writer",
            size = "~1.5GB",
            description = "成人向写作专用",
            url = "https://huggingface.co/TheDrummer/Dark-Muse-Writer-v01-GGUF/resolve/main/Dirty-Muse-Writer-v01-Uncensored-Erotica-NSFW.Q2_K.gguf",
            isNsfw = true
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("模型管理") }
        )
        
        // 在线模型设置
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "在线模型 (DeepSeek)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "使用云端API，响应快速，质量高",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // API Key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showApiKeyDialog = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("API Key")
                        Text(
                            if (apiKey.isNotBlank()) "${apiKey.take(8)}...${apiKey.takeLast(4)}" else "使用内置Key",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                
                Divider()
                
                // API地址
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("API地址")
                        Text(baseUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Divider()
                
                // 模型选择
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showModelDialog = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("模型")
                        Text(model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 测试连接按钮
                Button(
                    onClick = { viewModel.testConnection() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试中...")
                    } else {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试连接")
                    }
                }
                
                // 成功提示
                success?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearSuccess() }) {
                                Icon(Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                // 错误提示
                error?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // 本地模型
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "本地模型 (离线可用)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "下载后可离线使用，保护隐私",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // 下载进度
                if (localModelStatus == "downloading") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("正在下载模型...", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${downloadProgress.toInt()}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // 模型列表
                localModels.forEach { localModel ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { selectedModel = localModel }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(localModel.name, style = MaterialTheme.typography.titleSmall)
                                    if (localModel.isChineseFriendly) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("中文", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }
                                    if (localModel.isNsfw) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("18+", style = MaterialTheme.typography.labelSmall) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            ),
                                            modifier = Modifier.height(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    "${localModel.size} · ${localModel.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.Download, contentDescription = "下载", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                if (localModelStatus == "downloaded") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("已下载模型可用", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 使用说明
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("使用说明", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• 在线模型：需要网络，响应快，质量高\n" +
                    "• 本地模型：下载后可离线使用\n" +
                    "• 中文友好：对中文支持更好\n" +
                    "• 18+模型：支持成人向内容创作",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // API Key对话框
    if (showApiKeyDialog) {
        var newKey by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf(baseUrl) }
        
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("设置API") },
            text = {
                Column {
                    Text("留空使用内置API Key", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("API Key (可选)") },
                        placeholder = { Text("sk-xxxxx") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("API地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newKey.isNotBlank()) viewModel.updateApiKey(newKey)
                    if (newUrl.isNotBlank()) viewModel.updateBaseUrl(newUrl)
                    showApiKeyDialog = false
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 模型选择对话框
    if (showModelDialog) {
        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = { Text("选择模型") },
            text = {
                Column {
                    listOf("deepseek-chat", "deepseek-coder").forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = model == m, onClick = { viewModel.updateModel(m) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(m)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // 下载确认对话框
    selectedModel?.let { model ->
        AlertDialog(
            onDismissRequest = { selectedModel = null },
            title = { Text("下载模型") },
            text = {
                Column {
                    Text("即将下载: ${model.name}")
                    Text("大小: ${model.size}")
                    Text("说明: ${model.description}")
                    if (model.isNsfw) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⚠️ 此模型支持成人向内容", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.downloadLocalModel(model.url)
                    selectedModel = null
                }) {
                    Text("开始下载")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedModel = null }) {
                    Text("取消")
                }
            }
        )
    }
}
