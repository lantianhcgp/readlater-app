package com.lantianhcgp.readlater.ui.reader

import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.Tag

data class ReaderUiState(
    val article: Article? = null,
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false
)
