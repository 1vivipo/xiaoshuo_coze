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
import com.bihe.app.data.model.Project
import com.bihe.app.ui.viewmodel.PromoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoScreen() {
    val viewModel: PromoViewModel = viewModel()
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("推文视频") },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "新建")
                }
            }
        )
        
        // 快捷操作
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { viewModel.generateAllVideos() },
                label = { Text("批量生成视频") },
                leadingIcon = { 
                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp)) 
                }
            )
            AssistChip(
                onClick = { viewModel.exportAll() },
                label = { Text("导出全部") },
                leadingIcon = { 
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp)) 
                }
            )
        }
        
        Divider()
        
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            projects.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("还没有推文项目", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                        Text("点击右上角 + 创建", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        PromoProjectCard(
                            project = project,
                            onClick = { selectedProject = project },
                            onGenerateVideo = { viewModel.generateVideo(project) }
                        )
                    }
                }
            }
        }
    }
    
    // 新建对话框
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建推文项目") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("推文内容") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createPromoProject(title, content)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 项目详情
    selectedProject?.let { project ->
        PromoDetailSheet(
            project = project,
            onDismiss = { selectedProject = null },
            onGenerateVideo = { 
                viewModel.generateVideo(project)
                selectedProject = null
            },
            onDelete = { 
                viewModel.deleteProject(project)
                selectedProject = null
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
}

@Composable
fun PromoProjectCard(
    project: Project,
    onClick: () -> Unit,
    onGenerateVideo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${project.wordCount}字",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onGenerateVideo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VideoFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成视频")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDetailSheet(
    project: Project,
    onDismiss: () -> Unit,
    onGenerateVideo: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    onDismiss()
                    onGenerateVideo()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VideoFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成视频")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("删除")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${project.name}」吗？") },
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
