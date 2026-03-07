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
import com.bihe.app.data.model.Character
import com.bihe.app.ui.viewmodel.CharactersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    projectId: Long,
    onBack: () -> Unit,
    viewModel: CharactersViewModel = viewModel()
) {
    val project by viewModel.project.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("${project?.name ?: "项目"} - 人物") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "添加人物")
                }
            }
        )
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (characters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("还没有人物", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                    Text("点击右上角 + 添加", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(characters, key = { it.id }) { character ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedCharacter = character }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(character.name, style = MaterialTheme.typography.titleMedium)
                            if (character.alias.isNotBlank()) {
                                Text(
                                    "别名: ${character.alias}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (character.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    character.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加人物对话框
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var alias by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var personality by remember { mutableStateOf("") }
        var background by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加人物") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("姓名 *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text("别名/外号") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("外貌描述") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("性格特点") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = background,
                        onValueChange = { background = it },
                        label = { Text("背景故事") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCharacter(name, alias, description, personality, background)
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
    
    // 人物详情
    selectedCharacter?.let { character ->
        CharacterDetailSheet(
            character = character,
            onDismiss = { selectedCharacter = null },
            onDelete = {
                viewModel.deleteCharacter(character)
                selectedCharacter = null
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
fun CharacterDetailSheet(
    character: Character,
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
            Text(character.name, style = MaterialTheme.typography.headlineSmall)
            
            if (character.alias.isNotBlank()) {
                Text("别名: ${character.alias}", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (character.description.isNotBlank()) {
                Text("外貌描述", style = MaterialTheme.typography.labelMedium)
                Text(character.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (character.personality.isNotBlank()) {
                Text("性格特点", style = MaterialTheme.typography.labelMedium)
                Text(character.personality, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (character.background.isNotBlank()) {
                Text("背景故事", style = MaterialTheme.typography.labelMedium)
                Text(character.background, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("删除人物")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除「${character.name}」吗？") },
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
