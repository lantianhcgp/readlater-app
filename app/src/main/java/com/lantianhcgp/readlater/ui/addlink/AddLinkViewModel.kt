package com.lantianhcgp.readlater.ui.addlink

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.agent.AgentOrchestrator
import com.lantianhcgp.readlater.data.model.LlmConfig
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import com.lantianhcgp.readlater.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLinkViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val agentOrchestrator: AgentOrchestrator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("readlater_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(AddLinkUiState())
    val uiState: StateFlow<AddLinkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            agentOrchestrator.currentStep.collect { step ->
                _uiState.update { it.copy(currentStep = step) }
            }
        }
        viewModelScope.launch {
            agentOrchestrator.stepMessage.collect { message ->
                _uiState.update { it.copy(stepMessage = message) }
            }
        }
    }

    private fun getLlmConfig(): LlmConfig {
        val config = LlmConfig(
            provider = prefs.getString("provider", "openai") ?: "openai",
            baseUrl = prefs.getString("baseUrl", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
            apiKey = prefs.getString("apiKey", "") ?: "",
            model = prefs.getString("model", "gpt-4o") ?: "gpt-4o"
        )
        Logger.d("AddLinkVM", "使用配置: provider=${config.provider}, model=${config.model}")
        return config
    }

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
                Logger.i("AddLinkVM", "文章已保存: $articleId, 开始后台处理")
                articleRepository.processArticle(articleId, getLlmConfig())
                _uiState.update { it.copy(isSaved = true, isProcessing = false) }
            } catch (e: Exception) {
                Logger.e("AddLinkVM", "保存失败: ${e.message}")
                _uiState.update { it.copy(isProcessing = false, error = e.message) }
            }
        }
    }

    fun reset() {
        _uiState.value = AddLinkUiState()
    }
}
