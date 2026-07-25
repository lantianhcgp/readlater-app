package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.LlmProvider
import javax.inject.Inject

class FormatContentTool @Inject constructor() {

    companion object {
        const val NAME = "format_content"
    }

    suspend fun execute(rawHtml: String, title: String? = null, provider: LlmProvider): String {
        val titleLine = title?.let { "Title: $it\n\n" }.orEmpty()
        
        val systemPrompt = """You are a professional content editor for a read-later app. Your task is to clean up and format raw HTML article content.

RULES:
1. REMOVE all non-article content: navigation menus, headers, footers, sidebars, ads, like/share/bookmark buttons, comment sections, "related articles", author bios, cookie banners, social media widgets, login prompts, subscription forms, popups, video players
2. KEEP the main article body text only
3. Format the cleaned content as clean, readable text with:
   - Proper paragraph breaks (blank line between paragraphs)
   - Headings preserved with ## or ### prefix
   - Lists formatted with bullet points
   - Blockquotes formatted with > prefix
   - Code blocks formatted with ``` fences
   - NO HTML tags in the output
   - NO markdown formatting for emphasis (no ** or __)
4. Preserve the original language of the article
5. Keep the factual content accurate - do not summarize or rewrite, just clean up
6. If content is in Chinese, keep it in Chinese. Same for other languages.

OUTPUT: Return ONLY the cleaned, formatted article text. No explanations, no meta-commentary."""

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(
                role = "user",
                content = "${titleLine}Raw HTML content to clean up:\n\n${rawHtml.take(15000)}"
            )
        )
        
        val response = provider.chat(messages, temperature = 0.2, maxTokens = 8000)
        return response.content.orEmpty().trim()
    }
}
