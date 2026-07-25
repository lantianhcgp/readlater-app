package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.LlmProvider
import org.json.JSONArray
import javax.inject.Inject

class AutoTagTool @Inject constructor(
    private val llmProvider: LlmProvider
) {

    companion object {
        const val NAME = "auto_tag"

        val DEFINITION = com.lantianhcgp.readlater.agent.ToolDefinition(
            function = com.lantianhcgp.readlater.agent.FunctionDefinition(
                name = NAME,
                description = "Generate 2-5 relevant tags for the given article content. Tags should be lowercase, single words or short phrases.",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "content" to mapOf(
                            "type" to "string",
                            "description" to "The plain text content of the article"
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
                content = """You are a tag generator. Generate exactly 2-5 relevant tags for the given article.
Return ONLY a JSON array of lowercase tag strings. No explanation, no markdown, just the JSON array.
Example: ["programming", "ai", "tutorial"]"""
            ),
            ChatMessage(
                role = "user",
                content = "${titleLine}Content:\n${content.take(4000)}"
            )
        )

        val response = llmProvider.chat(messages, temperature = 0.3, maxTokens = 100)
        val raw = response.content.orEmpty().trim()

        return try {
            val arr = JSONArray(raw)
            val tags = (0 until arr.length()).map { arr.getString(it) }
            JSONArray(tags.take(5)).toString()
        } catch (_: Exception) {
            val fallback = raw.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            try {
                val arr = JSONArray(fallback)
                val tags = (0 until arr.length()).map { arr.getString(it) }
                JSONArray(tags.take(5)).toString()
            } catch (_: Exception) {
                """["article"]"""
            }
        }
    }
}
