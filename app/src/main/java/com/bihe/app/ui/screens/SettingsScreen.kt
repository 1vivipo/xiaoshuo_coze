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
import com.bihe.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val model by viewModel.model.collectAsState()
    
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("我的") }
        )
        
        // API设置
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AI设置",
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
                
                // 模型
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
            }
        }
        
        // 数据管理
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "数据管理",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("导出数据") },
                    leadingContent = {
                        Icon(Icons.Default.Download, contentDescription = null)
                    },
                    onClick = { viewModel.exportData() }
                )
                
                HorizontalDivider()
                
                ListItem(
                    headlineContent = { Text("导入数据") },
                    leadingContent = {
                        Icon(Icons.Default.Upload, contentDescription = null)
                    },
                    onClick = { viewModel.importData() }
                )
                
                HorizontalDivider()
                
                ListItem(
                    headlineContent = { Text("清除所有数据") },
                    leadingContent = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    supportingContent = { Text("此操作不可恢复", color = MaterialTheme.colorScheme.error) },
                    onClick = { viewModel.clearAllData() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 关于
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "关于",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("笔核") },
                    supportingContent = { Text("版本 1.0.0") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    onClick = { showAboutDialog = true }
                )
                
                HorizontalDivider()
                
                ListItem(
                    headlineContent = { Text("检查更新") },
                    leadingContent = {
                        Icon(Icons.Default.Update, contentDescription = null)
                    },
                    onClick = { viewModel.checkUpdate() }
                )
                
                HorizontalDivider()
                
                ListItem(
                    headlineContent = { Text("反馈问题") },
                    leadingContent = {
                        Icon(Icons.Default.Feedback, contentDescription = null)
                    },
                    onClick = { viewModel.feedback() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // API Key对话框
    if (showApiKeyDialog) {
        var newKey by remember { mutableStateOf(apiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("设置API Key") },
            text = {
                Column {
                    Text("请输入DeepSeek API Key", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("sk-xxxxx") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "获取方式：访问 platform.deepseek.com 注册并创建API Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateApiKey(newKey)
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
    
    // 关于对话框
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于笔核") },
            text = {
                Column {
                    Text("笔核 v1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("一款专业的AI辅助写作工具")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("功能特性：")
                    Text("• 小说创作与AI续写")
                    Text("• 漫剧剧本制作")
                    Text("• 推文视频生成")
                    Text("• 本地数据存储")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}
