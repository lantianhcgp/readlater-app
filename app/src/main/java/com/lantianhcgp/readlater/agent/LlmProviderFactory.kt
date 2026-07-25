package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.providers.OpenAiProvider
import com.lantianhcgp.readlater.data.model.LlmConfig
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmProviderFactory @Inject constructor(
    private val client: OkHttpClient
) {
    fun create(config: LlmConfig): LlmProvider {
        return when (config.provider.lowercase()) {
            "openai", "deepseek", "qwen", "openrouter" -> OpenAiProvider(client, config)
            "ollama" -> OpenAiProvider(
                client,
                config.copy(baseUrl = config.baseUrl.ifEmpty { "http://localhost:11434/v1" })
            )
            else -> throw IllegalArgumentException("Unsupported provider: ${config.provider}")
        }
    }
}
