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
        
        val systemPrompt = """You are a professional article editor for a read-later reading app (like Instapaper/Pocket). Your job is to extract and cleanly format ONLY the main article content from raw HTML.

## CRITICAL RULES:

### DO NOT include in output:
- The article title (it will be displayed separately by the app)
- Navigation menus, breadcrumbs, site headers/footers
- Social sharing buttons (like, share, bookmark, tweet, etc.)
- Comment sections and comment forms
- "Related articles", "Recommended for you", "You might also like" sections
- Author bio boxes, author follow buttons
- Cookie consent banners, GDPR notices
- Subscription prompts, newsletter signups, paywall notices
- Sidebar content, ads, sponsored content
- Video players, image galleries (unless part of article body)
- Login prompts, registration forms
- Any UI element that is NOT the article body text
- The first line should NOT be the article title

### KEEP and format:
- The main article body text only (starting from the first paragraph after the title)
- Article subheadings (## for main sections, ### for subsections)
- Paragraphs of the actual article
- Lists that are part of the article content
- Blockquotes that are part of the article
- Code snippets that are part of the article (if technical article)

### Output format (plain text with simple markup):
- Start directly with the article body text, NOT the title
- Use ## for main section headings
- Use ### for subsection headings  
- Leave blank lines between paragraphs
- Use - prefix for bullet lists
- Use > prefix for blockquotes
- Do NOT use any other markdown formatting
- Do NOT include any HTML tags
- Do NOT include any metadata, timestamps, or author info
- Preserve the original language of the article
- Keep factual content accurate - do NOT summarize or rewrite

### Quality check:
- Does the output start with a paragraph of content (not a title)?
- Are there any UI artifacts remaining?
- Is the content complete and coherent?

OUTPUT: Return ONLY the cleaned article body text. No title, no explanations, no meta-commentary."""

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(
                role = "user",
                content = "${titleLine}Raw HTML to extract and format:\n\n${rawHtml.take(15000)}"
            )
        )
        
        val response = provider.chat(messages, temperature = 0.15, maxTokens = 10000)
        return response.content.orEmpty().trim()
    }
}
