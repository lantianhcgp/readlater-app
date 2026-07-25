package com.lantianhcgp.readlater.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.model.LlmConfig
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLinkViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddLinkUiState())
    val uiState: StateFlow<AddLinkUiState> = _uiState.asStateFlow()

    private val llmConfig = LlmConfig(
        provider = "openai",
        baseUrl = "https://api.openai.com/v1",
        apiKey = "",
        model = "gpt-4o"
    )

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    fun saveLink() {
        val url = _uiState.value.url.trim()
        if (url.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val articleId = articleRepository.addArticle(url)
                _uiState.update { it.copy(isSaved = true, isProcessing = false) }
                articleRepository.processArticle(articleId, llmConfig)
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }

    fun reset() {
        _uiState.value = AddLinkUiState()
    }
}
