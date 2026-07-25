package com.lantianhcgp.readlater.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(articleId: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("阅读器") }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "阅读器页面开发中...\n文章 ID: $articleId",
                modifier = Modifier.padding(16.dp),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }
    }
}
