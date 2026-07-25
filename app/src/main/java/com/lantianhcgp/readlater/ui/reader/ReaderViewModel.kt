package com.lantianhcgp.readlater.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    private val articleId: String = savedStateHandle["articleId"] ?: ""
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val article = articleRepository.getArticleById(articleId)
            _uiState.update { it.copy(article = article, isLoading = false, isFavorite = article?.isFavorite ?: false) }
            tagRepository.getTagsForArticle(articleId).collect { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }
}
