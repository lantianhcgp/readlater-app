package com.lantianhcgp.readlater.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.entity.Highlight
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val article: com.lantianhcgp.readlater.data.db.entity.Article? = null,
    val tags: List<com.lantianhcgp.readlater.data.db.entity.Tag> = emptyList(),
    val highlights: List<Highlight> = emptyList(),
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val showNoteDialog: Boolean = false,
    val pendingHighlightText: String = ""
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val tagRepository: TagRepository,
    private val highlightDao: HighlightDao
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
        viewModelScope.launch {
            highlightDao.getHighlightsForArticle(articleId).collect { highlights ->
                _uiState.update { it.copy(highlights = highlights) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    fun onTextSelected(text: String) {
        if (text.isNotBlank() && text.length >= 2) {
            _uiState.update { it.copy(showNoteDialog = true, pendingHighlightText = text) }
        }
    }

    fun dismissNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, pendingHighlightText = "") }
    }

    fun saveHighlight(note: String = "") {
        val text = _uiState.value.pendingHighlightText
        if (text.isBlank()) return
        viewModelScope.launch {
            highlightDao.insertHighlight(
                Highlight(articleId = articleId, selectedText = text, note = note.ifBlank { null })
            )
            _uiState.update { it.copy(showNoteDialog = false, pendingHighlightText = "") }
        }
    }

    fun deleteHighlight(highlight: Highlight) {
        viewModelScope.launch {
            highlightDao.deleteHighlight(highlight)
        }
    }
}
