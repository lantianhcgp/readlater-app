package com.lantianhcgp.readlater.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lantianhcgp.readlater.debug.DebugData
import com.lantianhcgp.readlater.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showLogs by viewModel.showLogs.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val saveMessage by viewModel.saveMessage.collectAsStateWithLifecycle()
    val debugEnabled by Logger.isEnabled.collectAsStateWithLifecycle()
    val showPipeline by viewModel.showPipeline.collectAsStateWithLifecycle()
    val pipeline by viewModel.pipeline.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    LaunchedEffect(showLogs) {
        if (showLogs) viewModel.refreshLogs()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text("设置") }, colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface), scrollBehavior = scrollBehavior)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("AI 模型配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("当前 Provider: ${uiState.llmConfig.provider}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(uiState.llmConfig.baseUrl, viewModel::updateBaseUrl, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(uiState.llmConfig.apiKey, viewModel::updateApiKey, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(uiState.llmConfig.model, viewModel::updateModel, label = { Text("模型名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::saveConfig,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("保存配置")
            }

            Spacer(Modifier.height(32.dp))
            Text("调试", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("调试日志", style = MaterialTheme.typography.bodyLarge)
                    Text("开启后记录 App 运行日志", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = debugEnabled, onCheckedChange = { viewModel.toggleDebugEnabled() })
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.toggleLogs() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Text(if (showLogs) "隐藏日志" else "查看日志", modifier = Modifier.padding(start = 8.dp))
            }

            AnimatedVisibility(visible = showLogs) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                IconButton(onClick = { viewModel.refreshLogs() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                                }
                                IconButton(onClick = {
                                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = Logger.getFormattedLogs()
                                    val clip = ClipData.newPlainText("logs", text)
                                    clipManager.setPrimaryClip(clip)
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "复制全部")
                                }
                            }
                            IconButton(onClick = { viewModel.clearLogs() }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                        if (logs.isEmpty()) {
                            Text("暂无日志", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                                val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                logs.forEach { entry ->
                                    Text(
                                        text = "[${sdf.format(java.util.Date(entry.timestamp))}] ${entry.level.name}/${entry.tag}: ${entry.message}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        ),
                                        color = when (entry.level) {
                                            com.lantianhcgp.readlater.util.LogLevel.ERROR -> MaterialTheme.colorScheme.error
                                            com.lantianhcgp.readlater.util.LogLevel.WARN -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.togglePipeline() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Text(if (showPipeline) "隐藏 Pipeline" else "查看 Pipeline 数据", modifier = Modifier.padding(start = 8.dp))
            }

            AnimatedVisibility(visible = showPipeline) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pipeline 数据", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Row {
                                IconButton(onClick = { viewModel.refreshPipeline() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                                }
                                IconButton(onClick = {
                                    val text = pipeline?.let { p ->
                                        buildString {
                                            appendLine("=== URL ===")
                                            appendLine(p.url)
                                            appendLine("
=== TITLE ===")
                                            appendLine(p.title)
                                            appendLine("
=== RAW HTML (${p.rawHtml.length} chars) ===")
                                            appendLine(p.rawHtml.take(2000))
                                            appendLine("
=== FORMATTED (${p.formattedContent.length} chars) ===")
                                            appendLine(p.formattedContent.take(2000))
                                            appendLine("
=== CLEAN (${p.cleanContent.length} chars) ===")
                                            appendLine(p.cleanContent.take(2000))
                                            appendLine("
=== SUMMARY ===")
                                            appendLine(p.summary)
                                            appendLine("
=== TAGS ===")
                                            appendLine(p.tags.toString())
                                            if (p.error != null) {
                                                appendLine("
=== ERROR ===")
                                                appendLine(p.error)
                                            }
                                        }
                                    } ?: "No pipeline data"
                                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipManager.setPrimaryClip(ClipData.newPlainText("pipeline", text))
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "复制全部")
                                }
                            }
                        }

                        if (pipeline == null) {
                            Text("暂无数据，请先处理一篇文章", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val p = pipeline!!
                            // URL
                            Text("URL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(p.url, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), maxLines = 2)
                            Spacer(Modifier.height(8.dp))

                            // Title
                            Text("标题", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(p.title.ifEmpty { "(空)" }, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
                            Spacer(Modifier.height(8.dp))

                            // Content lengths
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("HTML: ${p.rawHtml.length}", style = MaterialTheme.typography.labelSmall)
                                Text("格式化: ${p.formattedContent.length}", style = MaterialTheme.typography.labelSmall)
                                Text("清洗: ${p.cleanContent.length}", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.height(8.dp))

                            // Tags
                            if (p.tags.isNotEmpty()) {
                                Text("标签", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(p.tags.joinToString(", "), style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
                                Spacer(Modifier.height(8.dp))
                            }

                            // Error
                            if (p.error != null) {
                                Text("错误", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                Text(p.error, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.height(8.dp))
                            }

                            // Summary
                            if (p.summary.isNotEmpty()) {
                                Text("摘要", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(p.summary, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), maxLines = 3)
                                Spacer(Modifier.height(8.dp))
                            }

                            // Raw HTML preview
                            Text("原始 HTML (前500字)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.height(100.dp).verticalScroll(rememberScrollState())) {
                                Text(p.rawHtml.take(500), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp))
                            }
                            Spacer(Modifier.height(8.dp))

                            // Formatted content preview
                            Text("AI 格式化 (前500字)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.height(100.dp).verticalScroll(rememberScrollState())) {
                                Text(p.formattedContent.take(500), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp))
                            }
                            Spacer(Modifier.height(8.dp))

                            // Clean content preview
                            Text("清洗后 (前500字)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.height(100.dp).verticalScroll(rememberScrollState())) {
                                Text(p.cleanContent.take(500), style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("ReadLater v1.1.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("开源 AI 驱动的稍后阅读 App", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
