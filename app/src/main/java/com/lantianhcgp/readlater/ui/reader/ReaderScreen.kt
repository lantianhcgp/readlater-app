package com.lantianhcgp.readlater.ui.reader

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextSelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.jsoup.Jsoup

sealed class ContentBlock {
    data class Paragraph(val text: String) : ContentBlock()
    data class Heading(val text: String, val level: Int) : ContentBlock()
    data class ListItem(val text: String, val ordered: Boolean = false) : ContentBlock()
    data class Quote(val text: String) : ContentBlock()
    data class Code(val text: String, val language: String? = null) : ContentBlock()
    data class Image(val url: String, val caption: String? = null) : ContentBlock()
    data class Divider(val text: String = "---") : ContentBlock()
}

private fun parseHtmlToBlocks(html: String): List<ContentBlock> {
    val doc = Jsoup.parse(html)
    val blocks = mutableListOf<ContentBlock>()

    fun processElement(element: org.jsoup.nodes.Element) {
        for (child in element.children()) {
            when (child.tagName()) {
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val level = child.tagName().removePrefix("h").toIntOrNull() ?: 1
                    val text = child.text().trim()
                    if (text.isNotBlank()) blocks.add(ContentBlock.Heading(text, level))
                }
                "p" -> {
                    val text = child.text().trim()
                    if (text.isNotBlank()) blocks.add(ContentBlock.Paragraph(text))
                }
                "ul", "ol" -> {
                    val isOrdered = child.tagName() == "ol"
                    child.select("li").forEach { li ->
                        val text = li.text().trim()
                        if (text.isNotBlank()) blocks.add(ContentBlock.ListItem(text, isOrdered))
                    }
                }
                "blockquote" -> {
                    val text = child.text().trim()
                    if (text.isNotBlank()) blocks.add(ContentBlock.Quote(text))
                }
                "pre" -> {
                    val code = child.selectFirst("code")
                    val language = code?.className()?.removePrefix("language-")?.takeIf { it.isNotBlank() }
                    val text = (code ?: child).text().trim()
                    if (text.isNotBlank()) blocks.add(ContentBlock.Code(text, language))
                }
                "figure" -> {
                    val img = child.selectFirst("img")
                    val caption = child.selectFirst("figcaption")?.text()
                    if (img != null) {
                        val src = img.attr("abs:src").ifBlank { img.attr("data-src") }
                        if (src.isNotBlank()) blocks.add(ContentBlock.Image(src, caption))
                    }
                }
                "img" -> {
                    val src = child.attr("abs:src").ifBlank { child.attr("data-src") }
                    if (src.isNotBlank()) blocks.add(ContentBlock.Image(src, null))
                }
                "hr" -> blocks.add(ContentBlock.Divider())
                "div", "section", "article", "main" -> processElement(child)
                else -> {
                    if (child.children().isEmpty() || child.children().none {
                            it.tagName() in listOf("p", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "pre", "blockquote", "figure", "img")
                        }) {
                        val text = child.text().trim()
                        if (text.isNotBlank() && text.length > 10) {
                            blocks.add(ContentBlock.Paragraph(text))
                        }
                    } else {
                        processElement(child)
                    }
                }
            }
        }
    }

    processElement(doc)
    return blocks
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderScreen(onBack: () -> Unit, viewModel: ReaderViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                CircularProgressIndicator()
            }
        } else {
            val article = uiState.article
            if (article != null) {
                val contentBlocks = remember(article.content) {
                    article.content?.let { parseHtmlToBlocks(it) } ?: emptyList()
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
                    Spacer(Modifier.height(8.dp))
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
                        Spacer(Modifier.height(8.dp))
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
                                    Text(highlight.selectedText, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
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

                    TextSelectionContainer {
                        if (contentBlocks.isNotEmpty()) {
                            contentBlocks.forEach { block ->
                                when (block) {
                                    is ContentBlock.Paragraph -> {
                                        Text(
                                            text = block.text,
                                            style = MaterialTheme.typography.bodyLarge,
                                            lineHeight = 28.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    is ContentBlock.Heading -> {
                                        val style = when (block.level) {
                                            1 -> MaterialTheme.typography.headlineLarge
                                            2 -> MaterialTheme.typography.headlineMedium
                                            3 -> MaterialTheme.typography.headlineSmall
                                            4 -> MaterialTheme.typography.titleLarge
                                            else -> MaterialTheme.typography.titleMedium
                                        }
                                        Text(
                                            text = block.text,
                                            style = style,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    is ContentBlock.ListItem -> {
                                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Text(
                                                text = "• ",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = block.text,
                                                style = MaterialTheme.typography.bodyLarge,
                                                lineHeight = 28.sp
                                            )
                                        }
                                    }
                                    is ContentBlock.Quote -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = block.text,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    is ContentBlock.Code -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .padding(12.dp)
                                        ) {
                                            block.language?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = block.text,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }
                                    }
                                    is ContentBlock.Image -> {
                                        AsyncImage(
                                            model = block.url,
                                            contentDescription = block.caption,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.FillWidth
                                        )
                                        block.caption?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                    is ContentBlock.Divider -> {
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp)
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                }
                            }
                        } else if (article.plainText != null) {
                            Text(
                                text = article.plainText,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 28.sp
                            )
                        }
                    }

                    if (article.status == ArticleStatus.PROCESSING) {
                        Spacer(Modifier.height(20.dp))
                        CircularProgressIndicator(Modifier.height(24.dp), strokeWidth = 2.dp)
                        Text("AI 正在处理中...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        if (uiState.showNoteDialog) {
            var noteText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissNoteDialog() },
                title = { Text("添加标注") },
                text = {
                    Column {
                        Text("选中的文字:", style = MaterialTheme.typography.labelMedium)
                        Text(uiState.pendingHighlightText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("笔记（可选）") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.saveHighlight(noteText) }) { Text("保存") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissNoteDialog() }) { Text("取消") }
                }
            )
        }
    }
}
