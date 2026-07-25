package com.lantianhcgp.readlater.ui.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lantianhcgp.readlater.ui.components.ArticleCard
import com.lantianhcgp.readlater.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onArticleClick: (String) -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text("收藏夹") }, colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface), scrollBehavior = scrollBehavior)
        }
    ) { padding ->
        if (uiState.articles.isEmpty() && !uiState.isLoading) {
            EmptyState(title = "收藏夹是空的", subtitle = "阅读文章时点击 ❤️ 收藏", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(items = uiState.articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onClick = { onArticleClick(article.id) })
                }
            }
        }
    }
}
