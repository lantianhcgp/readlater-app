package com.lantianhcgp.readlater.ui.inbox

import com.lantianhcgp.readlater.data.db.entity.Article

data class InboxUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
