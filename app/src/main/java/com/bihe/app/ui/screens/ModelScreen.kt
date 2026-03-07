package com.bihe.app.ui.screens

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
                ListItem(
                    headlineContent = { Text("API Key") },
                    supportingContent = { 
                        Text(if (apiKey.isNotBlank()) "${apiKey.take(8)}...${apiKey.takeLast(4)}" else "未设置")
                    },
                    leadingContent = {
                        Icon(Icons.Default.Key, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { showApiKeyDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    }
                )
                
                HorizontalDivider()
                
                // API地址
                ListItem(
                    headlineContent = { Text("API地址") },
                    supportingContent = { Text(baseUrl) },
                    leadingContent = {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                    }
                )
                
                HorizontalDivider()
                
                // 模型选择
                ListItem(
                    headlineContent = { Text("模型") },
                    supportingContent = { Text(model) },
                    leadingContent = {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { showModelDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.testConnection() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = apiKey.isNotBlank()
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试连接")
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
                    "本地模型 (离线)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("GGUF模型") },
                    supportingContent = { Text("未下载") },
                    leadingContent = {
                        Icon(Icons.Default.Storage, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { viewModel.downloadLocalModel() }) {
                            Icon(Icons.Default.Download, contentDescription = "下载")
                        }
                    }
                )
                
                HorizontalDivider()
                
                ListItem(
                    headlineContent = { Text("模型路径") },
                    supportingContent = { Text("/data/local/models/") },
                    leadingContent = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    }
                )
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
                    "1. 在线模型需要API Key，访问 platform.deepseek.com 获取\n" +
                    "2. 本地模型可离线使用，但需要下载约2GB\n" +
                    "3. 在线模型响应更快，本地模型隐私更好",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // API Key对话框
    if (showApiKeyDialog) {
        var newKey by remember { mutableStateOf(apiKey) }
        var newUrl by remember { mutableStateOf(baseUrl) }
        
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("设置API") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("API Key") },
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
                    viewModel.updateApiKey(newKey)
                    viewModel.updateBaseUrl(newUrl)
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
    
    // 错误提示
    error?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("关闭")
                }
            }
        ) {
            Text(msg)
        }
    }
    
    // 加载中
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
