package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.LlmProvider
import javax.inject.Inject

class SummarizeTool @Inject constructor(
    private val llmProvider: LlmProvider
) {

    companion object {
        const val NAME = "summarize"

        val DEFINITION = com.lantianhcgp.readlater.agent.ToolDefinition(
            function = com.lantianhcgp.readlater.agent.FunctionDefinition(
                name = NAME,
                description = "Generate a concise 2-3 sentence summary of the given article content. The summary should be written in the same language as the source content.",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "content" to mapOf(
                            "type" to "string",
                            "description" to "The plain text content of the article to summarize"
                        ),
                        "title" to mapOf(
                            "type" to "string",
                            "description" to "The title of the article for context"
                        )
                    ),
                    "required" to listOf("content")
                )
            )
        )
    }

    suspend fun execute(content: String, title: String? = null): String {
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

        val response = llmProvider.chat(messages, temperature = 0.3, maxTokens = 300)
        return response.content.orEmpty()
    }
}
