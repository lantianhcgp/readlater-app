package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.LlmProvider
import org.json.JSONArray
import javax.inject.Inject

class AutoTagTool @Inject constructor() {

    companion object {
        const val NAME = "auto_tag"
    }

    suspend fun execute(content: String, title: String? = null, provider: LlmProvider): String {
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
        val response = provider.chat(messages, temperature = 0.3, maxTokens = 100)
        val raw = response.content.orEmpty().trim()
        return try {
            val arr = JSONArray(raw)
            val tags = (0 until arr.length()).map { arr.getString(it) }
            JSONArray(tags.take(5)).toString()
        } catch (_: Exception) {
            """["article"]"""
        }
    }
}
