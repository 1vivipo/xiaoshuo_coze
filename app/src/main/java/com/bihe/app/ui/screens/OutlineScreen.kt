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
import com.bihe.app.ui.viewmodel.OutlineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlineScreen(
    projectId: Long,
    onBack: () -> Unit,
    viewModel: OutlineViewModel = viewModel()
) {
    val project by viewModel.project.collectAsState()
    val volumes by viewModel.volumes.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddVolumeDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var selectedVolumeId by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("${project?.name ?: "项目"} - 大纲") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showAddVolumeDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加卷")
                }
            }
        )
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 显示卷
                items(volumes) { volume ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedVolumeId = volume.id }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(volume.title, style = MaterialTheme.typography.titleMedium)
                            if (volume.outline.isNotBlank()) {
                                Text(
                                    volume.outline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 显示章节
                items(chapters) { chapter ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(chapter.title, style = MaterialTheme.typography.titleSmall)
                            if (chapter.outline.isNotBlank()) {
                                Text(
                                    chapter.outline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // 添加章节按钮
                item {
                    OutlinedButton(
                        onClick = { showAddChapterDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加章节")
                    }
                }
            }
        }
    }
    
    // 添加卷对话框
    if (showAddVolumeDialog) {
        var title by remember { mutableStateOf("") }
        var outline by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddVolumeDialog = false },
            title = { Text("添加卷") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("卷名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = outline,
                        onValueChange = { outline = it },
                        label = { Text("大纲描述") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addVolume(title, outline)
                        showAddVolumeDialog = false
                    }
                }) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVolumeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 添加章节对话框
    if (showAddChapterDialog) {
        var title by remember { mutableStateOf("") }
        var outline by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("添加章节") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("章节名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = outline,
                        onValueChange = { outline = it },
                        label = { Text("章节大纲") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addChapter(title, outline)
                        showAddChapterDialog = false
                    }
                }) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) {
                    Text("取消")
                }
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
