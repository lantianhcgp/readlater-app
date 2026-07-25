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
        
        val systemPrompt = """You are a professional article tagger for a read-later app. Analyze the article and generate relevant tags.

## Rules:
1. Generate 3-5 tags that accurately describe the article's topic and category
2. Tags should be specific enough to be useful but not too narrow
3. Use lowercase, use hyphens for multi-word tags (e.g., "machine-learning")
4. Consider these categories when tagging:
   - Topic/subject (e.g., "javascript", "climate-change", "finance")
   - Content type (e.g., "tutorial", "analysis", "news", "opinion", "how-to")
   - Industry/domain (e.g., "tech", "health", "politics", "science")
   - Audience level (e.g., "beginner", "advanced")
5. Prefer existing common terms over made-up ones
6. If the article is in Chinese, use Chinese tags (e.g., "人工智能", "编程教程")
7. If in English, use English tags

## Output format:
Return ONLY a JSON array of tag strings. No explanation, no markdown, just the raw JSON array.
Example: ["machine-learning", "tutorial", "python", "beginner"]"""

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(
                role = "user",
                content = "${titleLine}Article content:\n${content.take(5000)}"
            )
        )
        
        val response = provider.chat(messages, temperature = 0.3, maxTokens = 150)
        val raw = response.content.orEmpty().trim()
        
        return try {
            val arr = JSONArray(raw)
            val tags = (0 until arr.length()).map { 
                arr.getString(it).lowercase().trim()
            }.filter { it.isNotBlank() }.take(5)
            JSONArray(tags).toString()
        } catch (_: Exception) {
            """["article"]"""
        }
    }
}
