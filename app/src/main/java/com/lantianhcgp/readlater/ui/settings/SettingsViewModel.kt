package com.lantianhcgp.readlater.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.lantianhcgp.readlater.data.model.LlmConfig
import com.lantianhcgp.readlater.util.LogEntry
import com.lantianhcgp.readlater.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("readlater_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _showLogs = MutableStateFlow(false)
    val showLogs: StateFlow<Boolean> = _showLogs.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        val config = LlmConfig(
            provider = prefs.getString("provider", "openai") ?: "openai",
            baseUrl = prefs.getString("baseUrl", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
            apiKey = prefs.getString("apiKey", "") ?: "",
            model = prefs.getString("model", "gpt-4o") ?: "gpt-4o"
        )
        _uiState.update { it.copy(llmConfig = config) }
        Logger.i("Settings", "Loaded config: provider=${config.provider}, model=${config.model}")
    }

    fun saveConfig() {
        val config = _uiState.value.llmConfig
        prefs.edit().apply {
            putString("provider", config.provider)
            putString("baseUrl", config.baseUrl)
            putString("apiKey", config.apiKey)
            putString("model", config.model)
            apply()
        }
        _saveMessage.value = "配置已保存"
        Logger.i("Settings", "Saved config: provider=${config.provider}, model=${config.model}")
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

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
        Logger.d("Settings", "Provider changed to: $provider")
    }

    fun updateBaseUrl(baseUrl: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(baseUrl = baseUrl)) } }
    fun updateApiKey(apiKey: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(apiKey = apiKey)) } }
    fun updateModel(model: String) { _uiState.update { it.copy(llmConfig = it.llmConfig.copy(model = model)) } }

    fun toggleLogs() {
        _showLogs.update { !it }
        Logger.d("Settings", "Logs panel toggled: ${_showLogs.value}")
    }

    fun refreshLogs() {
        _logs.value = Logger.logs.value
    }

    fun clearLogs() {
        Logger.clear()
        _logs.value = emptyList()
    }

    fun toggleDebugEnabled() {
        Logger.setEnabled(!Logger.isEnabled.value)
        Logger.i("Settings", "Debug logging: ${if (Logger.isEnabled.value) "enabled" else "disabled"}")
    }
}
