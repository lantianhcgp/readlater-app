package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.LlmProvider
import javax.inject.Inject

class SummarizeTool @Inject constructor() {

    companion object {
        const val NAME = "summarize"
    }

    suspend fun execute(content: String, title: String? = null, provider: LlmProvider): String {
        val titleLine = title?.let { "Title: $it\n\n" }.orEmpty()
        val messages = listOf(
            ChatMessage(
                role = "system",
                content = "You are a concise summarizer. Write a 2-3 sentence summary of the given article. Use the same language as the source content. Be factual and concise."
            ),
            ChatMessage(
                role = "user",
                content = "${titleLine}Content:\n${content.take(6000)}"
            )
        )
        val response = provider.chat(messages, temperature = 0.3, maxTokens = 300)
        return response.content.orEmpty()
    }
}
