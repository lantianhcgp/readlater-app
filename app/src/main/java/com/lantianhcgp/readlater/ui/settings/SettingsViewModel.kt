package com.lantianhcgp.readlater.ui.settings

import androidx.lifecycle.ViewModel
import com.lantianhcgp.readlater.data.model.LlmConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateProvider(provider: String) {
        val defaults = when (provider) {
            "openai" -> LlmConfig(provider = "openai", baseUrl = "https://api.openai.com/v1", model = "gpt-4o")
            "deepseek" -> LlmConfig(provider = "deepseek", baseUrl = "https://api.deepseek.com/v1", model = "deepseek-chat")
            "ollama" -> LlmConfig(provider = "ollama", baseUrl = "http://localhost:11434/v1", model = "llama3")
            "qwen" -> LlmConfig(provider = "qwen", baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1", model = "qwen-plus")
            "openrouter" -> LlmConfig(provider = "openrouter", baseUrl = "https://openrouter.ai/api/v1", model = "openai/gpt-4o")
            else -> LlmConfig()
        }
        _uiState.update { it.copy(llmConfig = defaults) }
    }

    fun updateBaseUrl(baseUrl: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(baseUrl = baseUrl)) } }
    fun updateApiKey(apiKey: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(apiKey = apiKey)) } }
    fun updateModel(model: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(model = model)) } }
}
