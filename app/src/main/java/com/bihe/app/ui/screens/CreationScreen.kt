package com.bihe.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen() {
    var selectedType by remember { mutableStateOf<ProjectType?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    
    // 模拟数据
    val projects = remember {
        mutableStateListOf(
            Project(name = "都市修仙录", type = ProjectType.NOVEL, totalWordGoal = 1000000),
            Project(name = "霸道总裁爱上我", type = ProjectType.SHORT_DRAMA, totalWordGoal = 50000),
            Project(name = "玄幻世界", type = ProjectType.NOVEL, totalWordGoal = 2000000)
        )
    }
    
    val filteredProjects = if (selectedType != null) {
        projects.filter { it.type == selectedType }
    } else {
        projects.toList()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = { Text("创作中枢") },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "新建项目")
                }
            }
        )
        
        // 类型筛选
        ScrollableTabRow(
            selectedTabIndex = when (selectedType) {
                null -> 0
                ProjectType.NOVEL -> 1
                ProjectType.SHORT_DRAMA -> 2
                ProjectType.COMIC_DRAMA -> 3
                ProjectType.PROMO_COPY -> 4
                else -> 0
            },
            edgePadding = 16.dp
        ) {
            Tab(
                selected = selectedType == null,
                onClick = { selectedType = null },
                text = { Text("全部") }
            )
            Tab(
                selected = selectedType == ProjectType.NOVEL,
                onClick = { selectedType = ProjectType.NOVEL },
                text = { Text("小说") }
            )
            Tab(
                selected = selectedType == ProjectType.SHORT_DRAMA,
                onClick = { selectedType = ProjectType.SHORT_DRAMA },
                text = { Text("短剧") }
            )
            Tab(
                selected = selectedType == ProjectType.COMIC_DRAMA,
                onClick = { selectedType = ProjectType.COMIC_DRAMA },
                text = { Text("漫剧") }
            )
            Tab(
                selected = selectedType == ProjectType.PROMO_COPY,
                onClick = { selectedType = ProjectType.PROMO_COPY },
                text = { Text("推文") }
            )
        }
        
        // 项目列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProjects) { project ->
                ProjectCard(
                    project = project,
                    onClick = { selectedProject = project }
                )
            }
        }
    }
    
    // 新建项目对话框
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type ->
                projects.add(Project(name = name, type = type))
                showCreateDialog = false
            }
        )
    }
    
    // 项目详情
    selectedProject?.let { project ->
        ProjectDetailSheet(
            project = project,
            onDismiss = { selectedProject = null }
        )
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (project.type) {
                                ProjectType.NOVEL -> "小说"
                                ProjectType.SHORT_DRAMA -> "短剧"
                                ProjectType.COMIC_DRAMA -> "漫剧"
                                ProjectType.PROMO_COPY -> "推文"
                            }
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${project.wordCount} / ${project.totalWordGoal} 字",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${project.chapterCount} 章",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = if (project.totalWordGoal > 0) {
                    (project.wordCount.toFloat() / project.totalWordGoal).coerceIn(0f, 1f)
                } else 0f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, ProjectType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProjectType.NOVEL) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建项目") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("项目名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("项目类型", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                ProjectType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (type) {
                                ProjectType.NOVEL -> "小说"
                                ProjectType.SHORT_DRAMA -> "短剧剧本"
                                ProjectType.COMIC_DRAMA -> "漫剧剧本"
                                ProjectType.PROMO_COPY -> "推文文案"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, selectedType) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailSheet(
    project: Project,
    onDismiss: () -> Unit
) {
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { /* 打开编辑器 */ }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始写作")
                }
                OutlinedButton(onClick = { /* 打开大纲 */ }) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("大纲管理")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = { /* 打开人物设定 */ }) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("人物设定")
                }
                OutlinedButton(onClick = { /* 打开世界观 */ }) {
                    Icon(Icons.Default.Public, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("世界观")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
