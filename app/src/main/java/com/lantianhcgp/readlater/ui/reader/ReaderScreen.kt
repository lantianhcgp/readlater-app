package com.lantianhcgp.readlater.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lantianhcgp.readlater.data.model.ArticleStatus
import com.lantianhcgp.readlater.ui.components.TagChip
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle


private fun stripTitleFromDisplay(text: String, title: String?): String {
    if (title.isNullOrBlank()) return text
    
    val normalizedTitle = title.trim()
    val lines = text.split("\n").toMutableList()
    
    while (lines.isNotEmpty()) {
        val first = lines.first().trim()
        
        if (first.isBlank()) {
            lines.removeAt(0)
            continue
        }
        
        val normalizedFirst = first
            .removePrefix("## ")
            .removePrefix("### ")
            .removePrefix("# ")
            .trim()
        
        if (normalizedFirst == normalizedTitle) {
            lines.removeAt(0)
            continue
        }
        
        break
    }
    
    return lines.joinToString("\n").trim()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderScreen(onBack: () -> Unit, viewModel: ReaderViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📑 目录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    
                    if (uiState.tocItems.isEmpty()) {
                        Text("暂无目录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            uiState.tocItems.forEach { item ->
                                val indentation = if (item.level == 3) 16.dp else 0.dp
                                Text(
                                    text = item.title,
                                    style = if (item.level == 2) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (item.level == 2) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = indentation, vertical = 6.dp)
                                        .clickable {
                                            scope.launch { drawerState.close() }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("🔤 字号", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("A", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                        Slider(
                            value = uiState.fontSize,
                            onValueChange = viewModel::updateFontSize,
                            valueRange = 12f..24f,
                            steps = 5,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("A", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("${uiState.fontSize.toInt()}sp", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(uiState.article?.sourceDomain ?: "") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.List, contentDescription = "目录")
                        }
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                val article = uiState.article
                if (article != null) {
                    val fontSizeSp = uiState.fontSize.sp
                    val displayText = remember(article.plainText, article.title) {
                        stripTitleFromDisplay(article.plainText ?: "", article.title)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        article.imageUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Text(article.title ?: article.url, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(14.dp))
                        Text(buildString { append(article.sourceDomain); article.readingTimeMinutes?.let { append(" · $it 分钟阅读") } }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (uiState.tags.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                uiState.tags.forEach { TagChip(it.name) }
                            }
                        }

                        article.summary?.let { s ->
                            Spacer(Modifier.height(16.dp))
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(14.dp)
                            ) {
                                Text("✨ AI 摘要", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        if (uiState.highlights.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            Text("🖍️ 高亮标注", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(14.dp))
                            uiState.highlights.forEach { highlight ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(highlight.selectedText, style = MaterialTheme.typography.bodyMedium, maxLines = 3, fontSize = fontSizeSp)
                                        highlight.note?.let { note ->
                                            Spacer(Modifier.height(4.dp))
                                            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteHighlight(highlight) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        if (displayText.isNotBlank()) {
                            val annotatedText = remember(displayText, fontSizeSp) {
                                val lines = displayText.split("\n")
                                buildAnnotatedString {
                                    for (line in lines) {
                                        val trimmed = line.trim()
                                        when {
                                            trimmed.isEmpty() -> {
                                                append("\n\n")
                                            }
                                            trimmed.startsWith("## ") -> {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                                                    append(trimmed.removePrefix("## "))
                                                }
                                                append("\n\n")
                                            }
                                            trimmed.startsWith("### ") -> {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                                                    append(trimmed.removePrefix("### "))
                                                }
                                                append("\n\n")
                                            }
                                            trimmed.startsWith("> ") -> {
                                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                                    append("    " + trimmed.removePrefix("> "))
                                                }
                                                append("\n\n")
                                            }
                                            trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
                                                append("• " + trimmed.removePrefix("- ").removePrefix("• "))
                                                append("\n")
                                            }
                                            else -> {
                                                withStyle(SpanStyle(fontSize = fontSizeSp)) {
                                                    append(trimmed)
                                                }
                                                append("\n\n")
                                            }
                                        }
                                    }
                                }
                            }
                            SelectionContainer {
                                Text(
                                    text = annotatedText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = fontSizeSp,
                                    lineHeight = (fontSizeSp.value + 8).sp
                                )
                            }
                        }

                        if (article.status == ArticleStatus.PROCESSING) {
                            Spacer(Modifier.height(20.dp))
                            androidx.compose.material3.CircularProgressIndicator(Modifier.height(24.dp), strokeWidth = 2.dp)
                            Text("AI 正在处理中...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }

            if (uiState.showNoteDialog) {
                val noteTextState = remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { viewModel.dismissNoteDialog() },
                    title = { Text("添加标注") },
                    text = {
                        Column {
                            Text("选中的文字:", style = MaterialTheme.typography.labelMedium)
                            Text(uiState.pendingHighlightText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = noteTextState.value,
                                onValueChange = { noteTextState.value = it },
                                label = { Text("笔记（可选）") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.saveHighlight(noteTextState.value) }) { Text("保存") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissNoteDialog() }) { Text("取消") }
                    }
                )
            }
        }
    }
}
