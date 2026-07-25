package com.lantianhcgp.readlater.ui.reader

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.entity.Highlight
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TocItem(val title: String, val level: Int, val index: Int)

data class ReaderUiState(
    val article: com.lantianhcgp.readlater.data.db.entity.Article? = null,
    val tags: List<com.lantianhcgp.readlater.data.db.entity.Tag> = emptyList(),
    val highlights: List<Highlight> = emptyList(),
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val showNoteDialog: Boolean = false,
    val pendingHighlightText: String = "",
    val showToc: Boolean = false,
    val tocItems: List<TocItem> = emptyList(),
    val fontSize: Float = 16f,
    val selectedTextForHighlight: String = ""
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val tagRepository: TagRepository,
    private val highlightDao: HighlightDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val articleId: String = savedStateHandle["articleId"] ?: ""
    private val prefs: SharedPreferences = context.getSharedPreferences("readlater_settings", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        val savedFontSize = prefs.getFloat("reader_font_size", 16f)
        _uiState.update { it.copy(fontSize = savedFontSize) }
        
        viewModelScope.launch {
            val article = articleRepository.getArticleById(articleId)
            _uiState.update { it.copy(article = article, isLoading = false, isFavorite = article?.isFavorite ?: false) }
            article?.plainText?.let { extractToc(it) }
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

    private fun extractToc(text: String) {
        val lines = text.split("\n")
        val tocItems = mutableListOf<TocItem>()
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("## ") -> {
                    tocItems.add(TocItem(trimmed.removePrefix("## "), 2, index))
                }
                trimmed.startsWith("### ") -> {
                    tocItems.add(TocItem(trimmed.removePrefix("### "), 3, index))
                }
            }
        }
        _uiState.update { it.copy(tocItems = tocItems) }
    }

    fun toggleToc() {
        _uiState.update { it.copy(showToc = !it.showToc) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    fun onTextSelected(text: String) {
        if (text.isNotBlank() && text.length >= 2) {
            _uiState.update { it.copy(showNoteDialog = true, pendingHighlightText = text, selectedTextForHighlight = text) }
        }
    }

    fun dismissNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, pendingHighlightText = "", selectedTextForHighlight = "") }
    }

    fun saveHighlight(note: String = "") {
        val text = _uiState.value.pendingHighlightText
        if (text.isBlank()) return
        viewModelScope.launch {
            highlightDao.insertHighlight(
                Highlight(articleId = articleId, selectedText = text, note = note.ifBlank { null })
            )
            _uiState.update { it.copy(showNoteDialog = false, pendingHighlightText = "", selectedTextForHighlight = "") }
        }
    }

    fun deleteHighlight(highlight: Highlight) {
        viewModelScope.launch {
            highlightDao.deleteHighlight(highlight)
        }
    }

    fun updateFontSize(size: Float) {
        val clamped = size.coerceIn(12f, 24f)
        _uiState.update { it.copy(fontSize = clamped) }
        prefs.edit().putFloat("reader_font_size", clamped).apply()
    }
}
