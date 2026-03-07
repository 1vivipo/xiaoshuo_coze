package com.bihe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelScreen() {
    var apiKey by remember { mutableStateOf("sk-632f27c66a4445e091a101b29da605f3") }
    var baseUrl by remember { mutableStateOf("https://api.deepseek.com") }
    var selectedModel by remember { mutableStateOf("deepseek-chat") }
    var isOnlineMode by remember { mutableStateOf(true) }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var maxTokens by remember { mutableIntStateOf(4096) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("模型配置") }
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 模式切换
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("运行模式", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isOnlineMode,
                            onClick = { isOnlineMode = true },
                            label = { Text("在线模式 (DeepSeek API)") }
                        )
                        FilterChip(
                            selected = !isOnlineMode,
                            onClick = { isOnlineMode = false },
                            label = { Text("离线模式 (本地模型)") }
                        )
                    }
                }
            }
            
            // API配置
            if (isOnlineMode) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DeepSeek API 配置", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API Key") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { /* 测试连接 */ }) {
                                    Icon(Icons.Default.Check, contentDescription = "测试")
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            label = { Text("Base URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = selectedModel,
                            onValueChange = { selectedModel = it },
                            label = { Text("模型名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // 离线模型配置
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("本地模型配置", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { /* 导入模型 */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导入 GGUF 模型")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "支持导入 DeepSeek-R1、千问2 等量化模型",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 生成参数
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("生成参数", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Temperature: ${String.format("%.1f", temperature)}")
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0f..2f,
                        steps = 19
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Max Tokens: $maxTokens")
                    Slider(
                        value = maxTokens.toFloat(),
                        onValueChange = { maxTokens = it.toInt() },
                        valueRange = 512f..8192f,
                        steps = 15
                    )
                }
            }
            
            // Prompt模板
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Prompt 模板", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val templates = listOf(
                        "网文续写" to "NOVEL_CONTINUE",
                        "剧本创作" to "SCRIPT_CREATE",
                        "分镜生成" to "STORYBOARD_GEN",
                        "润色优化" to "POLISH",
                        "拆书仿写" to "ANALYZE_EMULATE"
                    )
                    
                    templates.forEach { (name, _) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name)
                            IconButton(onClick = { /* 编辑模板 */ }) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑")
                            }
                        }
                    }
                }
            }
        }
    }
}
