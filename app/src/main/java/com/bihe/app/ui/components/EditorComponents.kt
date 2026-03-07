package com.bihe.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RichTextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "开始写作...",
    enabled: Boolean = true
) {
    var textFieldValue by remember(text) {
        mutableStateOf(TextFieldValue(text))
    }
    
    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onTextChange(it.text)
        },
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 16.sp,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            if (textFieldValue.text.isEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
            innerTextField()
        }
    )
}

@Composable
fun EditorToolbar(
    onBold: () -> Unit = {},
    onItalic: () -> Unit = {},
    onIndent: () -> Unit = {},
    onFind: () -> Unit = {},
    onExport: () -> Unit = {},
    wordCount: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(onClick = onBold) {
                    Text("B", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onItalic) {
                    Text("I", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onIndent) {
                    Text("→", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onFind) {
                    Text("🔍", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onExport) {
                    Text("导出", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Text(
                text = "$wordCount 字",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun OutlineItem(
    title: String,
    level: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDrag: () -> Unit = {}
) {
    val indent = (level * 16).dp
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = when (level) {
                    0 -> MaterialTheme.typography.titleMedium
                    1 -> MaterialTheme.typography.titleSmall
                    else -> MaterialTheme.typography.bodyMedium
                }
            )
            
            IconButton(
                onClick = onDrag,
                modifier = Modifier.size(24.dp)
            ) {
                Text("⋮⋮", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCard(
    name: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun WritingProgressBar(
    current: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "写作进度",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "$current / $target 字",
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = (current.toFloat() / target).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
