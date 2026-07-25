package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.data.model.LlmConfig

/**
 * Result of agent processing for an article.
 */
data class ArticleProcessingResult(
    val title: String? = null,
    val summary: String? = null,
    val plainText: String? = null,
    val imageUrl: String? = null,
    val sourceDomain: String = "",
    val readingTimeMinutes: Int? = null,
    val tags: List<String> = emptyList(),
    val highlights: List<ExtractedHighlight> = emptyList(),
    val error: String? = null
)

data class ExtractedHighlight(
    val text: String,
    val note: String? = null
)

/**
 * Orchestrates LLM + tool calls to process an article URL into structured content.
 * Placeholder — implementation will be added in later tasks.
 */
interface AgentOrchestrator {
    /**
     * Process an article URL: fetch content, extract metadata, generate summary, assign tags.
     */
    suspend fun processArticle(url: String, config: LlmConfig): ArticleProcessingResult
}
