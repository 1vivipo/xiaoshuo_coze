package com.bihe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bihe.app.ui.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: Long,
    chapterId: Long? = null,
    onBack: () -> Unit = {},
    viewModel: EditorViewModel = viewModel()
) {
    val project by viewModel.project.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapter by viewModel.currentChapter.collectAsState()
    val isWriting by viewModel.isWriting.collectAsState()
    val writingProgress by viewModel.writingProgress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val content by viewModel.content.collectAsState()
    
    var showChapterList by remember { mutableStateOf(false) }
    var showNewChapterDialog by remember { mutableStateOf(false) }
    var showAISettings by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    
    // 加载项目
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId, chapterId)
    }
    
    // 同步内容
    LaunchedEffect(content) {
        editText = content
    }
    
    // 显示错误
    LaunchedEffect(error) {
        error?.let {
            // 可以显示Snackbar
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentChapter?.title ?: project?.name ?: "写作") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(Icons.Default.List, contentDescription = "章节")
                    }
                    IconButton(onClick = { showAISettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentChapter != null && !isWriting) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (viewModel.getApiKey().isBlank()) {
                            showAISettings = true
                        } else {
                            viewModel.startAIWriting(editText)
                        }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("AI续写") }
                )
            }
            if (isWriting) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.stopWriting() },
                    containerColor = MaterialTheme.colorScheme.error,
                    icon = { Icon(Icons.Default.Stop, contentDescription = null) },
                    text = { Text("停止") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 进度条
            if (isWriting) {
                LinearProgressIndicator(
                    progress = writingProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Text(
                    text = "AI续写中... ${writingProgress.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // 错误提示
            error?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
            }
            
            // 加载中
            if (isLoading && !isWriting) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // 编辑器
            BasicTextField(
                value = editText,
                onValueChange = { 
                    editText = it
                    viewModel.updateContent(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                enabled = !isWriting,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (editText.isEmpty()) {
                        Text(
                            text = "开始写作...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
            
            // 底部状态栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("字数: ${editText.length}")
                    currentChapter?.let { 
                        Text("章节: ${it.title}")
                    }
                }
            }
        }
    }
    
    // 章节列表
    if (showChapterList) {
        ModalBottomSheet(onDismissRequest = { showChapterList = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("章节列表", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                chapters.forEach { chapter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            viewModel.selectChapter(chapter)
                            showChapterList = false
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(chapter.title)
                            Text(
                                "${chapter.wordCount}字",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showNewChapterDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("新建章节")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // 新建章节对话框
    if (showNewChapterDialog) {
        var newChapterTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewChapterDialog = false },
            title = { Text("新建章节") },
            text = {
                OutlinedTextField(
                    value = newChapterTitle,
                    onValueChange = { newChapterTitle = it },
                    label = { Text("章节标题") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChapterTitle.isNotBlank()) {
                            viewModel.createChapter(newChapterTitle)
                            showNewChapterDialog = false
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChapterDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // AI设置
    if (showAISettings) {
        var apiKey by remember { mutableStateOf(viewModel.getApiKey()) }
        var baseUrl by remember { mutableStateOf("https://api.deepseek.com") }
        
        AlertDialog(
            onDismissRequest = { showAISettings = false },
            title = { Text("AI续写设置") },
            text = {
                Column {
                    Text(
                        "请输入DeepSeek API Key",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("sk-xxxxx") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("API地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "获取API Key: platform.deepseek.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateApiKey(apiKey)
                        showAISettings = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAISettings = false }) {
                    Text("取消")
                }
            }
        )
    }
}
