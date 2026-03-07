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
                    } else {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoading) "测试中..." else "测试连接")
                }
                
                // 显示测试结果
                success?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearSuccess() }) {
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
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dirty-Muse-Writer 模型
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Dirty-Muse-Writer-v01",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Q2_K · 约1.5GB · 成人向写作模型",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            when (localModelStatus) {
                                "not_downloaded" -> {
                                    Button(onClick = { viewModel.downloadLocalModel() }) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("下载")
                                    }
                                }
                                "downloading" -> {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${downloadProgress.toInt()}%", style = MaterialTheme.typography.labelMedium)
                                        LinearProgressIndicator(
                                            progress = downloadProgress / 100f,
                                            modifier = Modifier.width(80.dp)
                                        )
                                    }
                                }
                                "downloaded" -> {
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("已下载") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                                else -> {
                                    Text(localModelStatus, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        if (localModelStatus == "downloading") {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "正在下载... ${downloadProgress.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("模型存储路径")
                        Text(
                            "/data/data/com.bihe.app/models/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                Text(
                    "使用说明",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• 在线模型：需要网络，响应快，质量高\n" +
                    "• 本地模型：离线可用，隐私保护，需下载\n" +
                    "• Dirty-Muse-Writer：专为成人向写作优化的模型\n" +
                    "• 下载后可在设置中切换使用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 错误提示
        error?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
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
                    Text(
                        "留空使用内置API Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    val models = listOf("deepseek-chat", "deepseek-coder")
                    models.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = model == m,
                                onClick = { viewModel.updateModel(m) }
                            )
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
}
