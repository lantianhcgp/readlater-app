package com.lantianhcgp.readlater.ui.tags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagArticlesUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TagArticlesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository
) : ViewModel() {
    private val tagId: String = savedStateHandle["tagId"] ?: ""
    private val _uiState = MutableStateFlow(TagArticlesUiState())
    val uiState: StateFlow<TagArticlesUiState> = _uiState.asStateFlow()

    init {
        if (tagId.isNotBlank()) {
            viewModelScope.launch {
                tagRepository.getArticlesByTag(tagId).collect { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
