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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe.app.data.model.Project
import com.bihe.app.data.model.ProjectType
import com.bihe.app.ui.viewmodel.CreationViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationScreen(
    onNavigateToEditor: (Long) -> Unit = {},
    viewModel: CreationViewModel = viewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("创作中枢") },
            actions = {
                IconButton(onClick = { viewModel.showCreateDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "新建项目")
                }
            }
        )
        
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
            Tab(selected = selectedType == null, onClick = { viewModel.filterByType(null) }, text = { Text("全部") })
            Tab(selected = selectedType == ProjectType.NOVEL, onClick = { viewModel.filterByType(ProjectType.NOVEL) }, text = { Text("小说") })
            Tab(selected = selectedType == ProjectType.SHORT_DRAMA, onClick = { viewModel.filterByType(ProjectType.SHORT_DRAMA) }, text = { Text("短剧") })
            Tab(selected = selectedType == ProjectType.COMIC_DRAMA, onClick = { viewModel.filterByType(ProjectType.COMIC_DRAMA) }, text = { Text("漫剧") })
            Tab(selected = selectedType == ProjectType.PROMO_COPY, onClick = { viewModel.filterByType(ProjectType.PROMO_COPY) }, text = { Text("推文") })
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    onClick = { viewModel.selectProject(project) }
                )
            }
        }
    }
    
    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { name, type ->
                viewModel.createProject(name, type)
            }
        )
    }
    
    selectedProject?.let { project ->
        ProjectDetailSheet(
            project = project,
            onDismiss = { viewModel.clearSelectedProject() },
            onWrite = {
                viewModel.clearSelectedProject()
                onNavigateToEditor(project.id)
            },
            onManageOutline = { /* TODO */ },
            onManageCharacters = { /* TODO */ },
            onManageWorldSetting = { /* TODO */ },
            onDelete = { viewModel.deleteProject(project) }
        )
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = project.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                AssistChip(onClick = {}, label = { Text(when (project.type) { ProjectType.NOVEL -> "小说" ProjectType.SHORT_DRAMA -> "短剧" ProjectType.COMIC_DRAMA -> "漫剧" ProjectType.PROMO_COPY -> "推文" }) }, modifier = Modifier.height(28.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${project.wordCount} / ${project.totalWordGoal} 字", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${project.chapterCount} 章", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = if (project.totalWordGoal > 0) (project.wordCount.toFloat() / project.totalWordGoal).coerceIn(0f, 1f) else 0f, modifier = Modifier.fillMaxWidth())
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
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("项目名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("项目类型", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ProjectType.values().forEach { type ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { selectedType = type }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(when (type) { ProjectType.NOVEL -> "小说" ProjectType.SHORT_DRAMA -> "短剧剧本" ProjectType.COMIC_DRAMA -> "漫剧剧本" ProjectType.PROMO_COPY -> "推文文案" })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onCreate(name, selectedType) }, enabled = name.isNotBlank()) { Text("创建") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailSheet(
    project: Project,
    onDismiss: () -> Unit,
    onWrite: () -> Unit,
    onManageOutline: () -> Unit,
    onManageCharacters: () -> Unit,
    onManageWorldSetting: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = project.name, style = MaterialTheme.typography.headlineSmall)
            Text(text = when (project.type) { ProjectType.NOVEL -> "小说" ProjectType.SHORT_DRAMA -> "短剧" ProjectType.COMIC_DRAMA -> "漫剧" ProjectType.PROMO_COPY -> "推文" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onWrite) { Icon(Icons.Default.Edit, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("开始写作") }
                OutlinedButton(onClick = onManageOutline) { Icon(Icons.Default.List, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("大纲") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = onManageCharacters) { Icon(Icons.Default.Person, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("人物") }
                OutlinedButton(onClick = onManageWorldSetting) { Icon(Icons.Default.Public, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("世界观") }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = { showDeleteConfirm = true }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("删除项目") }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${project.name}」吗？此操作不可恢复。") },
            confirmButton = { Button(onClick = { showDeleteConfirm = false; onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") } }
        )
    }
}
