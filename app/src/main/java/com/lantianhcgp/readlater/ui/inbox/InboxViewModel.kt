package com.lantianhcgp.readlater.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    articleRepository: ArticleRepository
) : ViewModel() {

    val uiState: StateFlow<InboxUiState> = articleRepository
        .getAllArticles()
        .map { articles ->
            InboxUiState(
                articles = articles,
                isLoading = false
            )
        }
        .catch { e ->
            emit(InboxUiState(error = e.message, isLoading = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InboxUiState()
        )
}
