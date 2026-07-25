package com.lantianhcgp.readlater.data.model

data class LlmConfig(
    val id: String = "default",
    val provider: String = "openai",
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o",
    val isEnabled: Boolean = true
)
