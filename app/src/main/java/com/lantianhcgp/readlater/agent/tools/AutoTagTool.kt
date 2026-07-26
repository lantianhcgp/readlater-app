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
        
        val systemPrompt = """You are a professional article tagger for a read-later app. Analyze the article and generate relevant, specific tags.

## Rules:
1. Generate 3-5 tags that accurately describe the article's topic
2. Tags should be SPECIFIC to the article's actual content, not generic like "article"
3. Use lowercase, use hyphens for multi-word tags (e.g., "machine-learning")
4. Consider these categories:
   - Topic/subject (e.g., "artificial-intelligence", "fintech", "game-industry")
   - Content type (e.g., "analysis", "news", "tutorial", "interview", "deep-dive")
   - Industry/domain (e.g., "tech", "entertainment", "finance", "healthcare")
5. If the article is in Chinese, use Chinese tags (e.g., "人工智能", "游戏行业", "深度分析")
6. If in English, use English tags
7. NEVER return generic tags like "article", "content", "reading", "text"

## Examples of good tags:
- For a WeChat article about a game company: ["游戏行业", "阅文集团", "付费内容", "短视频"]
- For an English tech article: ["machine-learning", "python", "tutorial", "data-science"]
- For a finance news: ["fintech", "digital-banking", "regulation", "news"]

## Output format:
Return ONLY a JSON array of tag strings. No explanation, no markdown, just the raw JSON array.
Example: ["人工智能", "深度学习", "技术趋势"]"""

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
            }.filter { it.isNotBlank() && it != "article" && it != "content" && it.length > 1 }.take(5)
            if (tags.isEmpty()) """["未分类"]""" else JSONArray(tags).toString()
        } catch (_: Exception) {
            """["未分类"]"""
        }
    }
}
