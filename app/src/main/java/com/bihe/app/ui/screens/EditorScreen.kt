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
import com.bihe.app.data.model.Chapter
import com.bihe.app.ui.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

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
    
    var showChapterList by remember { mutableStateOf(false) }
    var showNewChapterDialog by remember { mutableStateOf(false) }
    var showAISettings by remember { mutableStateOf(false) }
    
    var content by remember(currentChapter?.content ?: "") { mutableStateOf(currentChapter?.content ?: "") }
    
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId, chapterId)
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
                    onClick = { viewModel.startAIWriting(content) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("AI续写") }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isWriting) {
                LinearProgressIndicator(
                    progress = writingProgress / 100f,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
                Text(
                    text = "AI续写中... ${writingProgress.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            BasicTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.updateContent(it)
                },
                modifier = Modifier
                    .fillMaxSize()
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
                    if (content.isEmpty()) {
                        Text(
                            text = "开始写作...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("字数: ${content.length}")
                    if (currentChapter != null) {
                        Text("目标: ${currentChapter.wordCount}字")
                    }
                }
            }
        }
    }
    
    if (showChapterList) {
        ModalBottomSheet(onDismissRequest = { showChapterList = false }) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                item {
                    Text("章节列表", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(chapters) { chapter ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            viewModel.selectChapter(chapter)
                            showChapterList = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(chapter.title)
                            Text("${chapter.wordCount}字", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showNewChapterDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("新建章节")
                    }
                }
            }
        }
    }
    
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
                TextButton(
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
    
    if (showAISettings) {
        var apiKey by remember { mutableStateOf(viewModel.getApiKey()) }
        var targetWords by remember { mutableStateOf(2000) }
        
        AlertDialog(
            onDismissRequest = { showAISettings = false },
            title = { Text("AI续写设置") },
            text = {
                Column {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("目标字数: $targetWords")
                    Slider(
                        value = targetWords.toFloat(),
                        onValueChange = { targetWords = it.toInt() },
                        valueRange = 500f..5000f,
                        steps = 9
                    )
                }
            },
            confirmButton = {
                TextButton(
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
