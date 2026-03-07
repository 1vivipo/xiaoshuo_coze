package com.bihe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe.app.data.model.WorldSetting
import com.bihe.app.ui.viewmodel.WorldSettingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingScreen(
    projectId: Long,
    onBack: () -> Unit,
    viewModel: WorldSettingViewModel = viewModel()
) {
    val project by viewModel.project.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf<WorldSetting?>(null) }
    
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("${project?.name ?: "项目"} - 世界观") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加设定")
                }
            }
        )
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (settings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("还没有世界观设定", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                    Text("点击右上角 + 添加", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(settings, key = { it.id }) { setting ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedSetting = setting }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(setting.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                setting.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                setting.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 添加设定对话框
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("背景设定") }
        var content by remember { mutableStateOf("") }
        
        val categories = listOf("背景设定", "势力组织", "地理环境", "魔法体系", "科技设定", "历史事件", "其他")
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加世界观设定") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题 *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("分类", style = MaterialTheme.typography.labelMedium)
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = category == cat,
                                onClick = { category = cat }
                            )
                            Text(cat)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("详细内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addSetting(title, category, content)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 设定详情
    selectedSetting?.let { setting ->
        WorldSettingDetailSheet(
            setting = setting,
            onDismiss = { selectedSetting = null },
            onDelete = {
                viewModel.deleteSetting(setting)
                selectedSetting = null
            }
        )
    }
    
    // 错误提示
    error?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = { viewModel.clearError() }) { Text("关闭") } }
        ) {
            Text(msg)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldSettingDetailSheet(
    setting: WorldSetting,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(setting.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                setting.category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(setting.content, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("删除设定")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${setting.title}」吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
