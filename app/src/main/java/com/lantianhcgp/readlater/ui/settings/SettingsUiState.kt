package com.lantianhcgp.readlater.ui.settings

import com.lantianhcgp.readlater.data.model.LlmConfig

data class SettingsUiState(
    val llmConfig: LlmConfig = LlmConfig(),
    val providerOptions: List<String> = listOf("openai", "deepseek", "ollama", "qwen", "openrouter")
)
