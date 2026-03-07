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
import com.bihe.app.data.model.DramaEpisode
import com.bihe.app.data.model.EpisodeStatus
import com.bihe.app.ui.viewmodel.DramaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DramaScreen() {
    val viewModel: DramaViewModel = viewModel()
    val episodes by viewModel.episodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedEpisode by remember { mutableStateOf<DramaEpisode?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("漫剧制作") },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "新建漫剧")
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
                onClick = { viewModel.generateAllScripts() },
                label = { Text("批量生成剧本") },
                leadingIcon = { 
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) 
                }
            )
            AssistChip(
                onClick = { viewModel.generateAllStoryboards() },
                label = { Text("生成分镜") },
                leadingIcon = { 
                    Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(18.dp)) 
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
            episodes.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("还没有漫剧项目", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
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
                    items(episodes, key = { it.id }) { episode ->
                        EpisodeCard(
                            episode = episode,
                            onClick = { selectedEpisode = episode },
                            onGenerateScript = { viewModel.generateScript(episode) },
                            onGenerateStoryboard = { viewModel.generateStoryboard(episode) }
                        )
                    }
                }
            }
        }
    }
    
    // 新建剧集对话框
    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var episodeCount by remember { mutableStateOf("60") }
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建漫剧项目") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("剧名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = episodeCount,
                        onValueChange = { episodeCount = it.filter { c -> c.isDigit() } },
                        label = { Text("集数") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createDramaProject(title, episodeCount.toIntOrNull() ?: 60)
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
    
    // 剧集详情
    selectedEpisode?.let { episode ->
        EpisodeDetailSheet(
            episode = episode,
            onDismiss = { selectedEpisode = null },
            onEditScript = { 
                // TODO: 打开剧本编辑
            },
            onDelete = { 
                viewModel.deleteEpisode(episode)
                selectedEpisode = null
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
fun EpisodeCard(
    episode: DramaEpisode,
    onClick: () -> Unit,
    onGenerateScript: () -> Unit,
    onGenerateStoryboard: () -> Unit
) {
    val statusText = when (episode.status) {
        EpisodeStatus.SCRIPT -> "剧本"
        EpisodeStatus.STORYBOARD -> "分镜"
        EpisodeStatus.RENDERING -> "渲染中"
        EpisodeStatus.COMPLETED -> "已完成"
    }
    
    val statusColor = when (episode.status) {
        EpisodeStatus.SCRIPT -> MaterialTheme.colorScheme.primary
        EpisodeStatus.STORYBOARD -> MaterialTheme.colorScheme.secondary
        EpisodeStatus.RENDERING -> MaterialTheme.colorScheme.tertiary
        EpisodeStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第${episode.episodeNumber}集 - ${episode.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(
                    onClick = {},
                    label = { Text(statusText) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = statusColor,
                        containerColor = statusColor.copy(alpha = 0.1f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${episode.script.length}字 · ${episode.duration}秒",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGenerateScript,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("生成剧本")
                }
                OutlinedButton(
                    onClick = onGenerateStoryboard,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("生成分镜")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeDetailSheet(
    episode: DramaEpisode,
    onDismiss: () -> Unit,
    onEditScript: () -> Unit,
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
                text = "第${episode.episodeNumber}集 - ${episode.title}",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    onDismiss()
                    onEditScript()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("编辑剧本")
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
            text = { Text("确定要删除第${episode.episodeNumber}集吗？") },
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
