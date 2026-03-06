package com.bihe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DramaScreen() {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val episodes = remember {
        mutableStateListOf(
            "第1集 - 命运的相遇",
            "第2集 - 误会重重",
            "第3集 - 真相大白",
            "第4集 - 情感升温"
        )
    }
    
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
                onClick = { },
                label = { Text("批量生成剧本") },
                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { },
                label = { Text("生成分镜") },
                leadingIcon = { Icon(Icons.Default.ViewCarousel, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { },
                label = { Text("批量导出") },
                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
        
        Divider()
        
        // 剧集列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(episodes) { episode ->
                EpisodeCard(episode = episode)
            }
        }
    }
}

@Composable
fun EpisodeCard(episode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = episode,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "800字 · 2分30秒",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "预览")
                }
            }
        }
    }
}
