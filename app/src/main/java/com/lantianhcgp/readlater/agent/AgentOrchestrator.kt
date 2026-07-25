package com.lantianhcgp.readlater.agent

import android.util.Log
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.data.model.ArticleStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

data class AgentResult(
    val title: String? = null,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val sourceDomain: String? = null,
    val readingTimeMinutes: Int? = null,
    val error: String? = null
)

@Singleton
class AgentOrchestrator @Inject constructor(
    private val fetchContentTool: FetchContentTool,
    private val toolExecutor: ToolExecutor,
    private val llmProvider: LlmProvider
) {

    companion object {
        private const val TAG = "AgentOrchestrator"
    }

    suspend fun processUrl(url: String): AgentResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing URL: $url")

            val fetchResult = fetchContentTool.execute(url)
            val fetchJson = org.json.JSONObject(fetchResult)

            if (fetchJson.has("error")) {
                return@withContext AgentResult(
                    error = "Failed to fetch: ${fetchJson.getString("error")}"
                )
            }

            val title = fetchJson.optString("title", null)
            val plainText = fetchJson.optString("plainText", "")
            val imageUrl = fetchJson.optString("imageUrl", null)
            val sourceDomain = fetchJson.optString("sourceDomain", null)
            val readingTime = fetchJson.optInt("readingTimeMinutes", 0)

            val summary = summarizeToolExecute(plainText, title)
            val tags = autoTagToolExecute(plainText, title)

            Log.d(TAG, "Processing complete: title=$title, tags=$tags")

            AgentResult(
                title = title,
                summary = summary,
                tags = tags,
                imageUrl = imageUrl,
                sourceDomain = sourceDomain,
                readingTimeMinutes = if (readingTime > 0) readingTime else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing URL", e)
            AgentResult(error = e.message ?: "Unknown error")
        }
    }

    private suspend fun summarizeToolExecute(content: String, title: String?): String? {
        return try {
            val toolCall = FunctionCall(
                name = "summarize",
                arguments = org.json.JSONObject().apply {
                    put("content", content)
                    title?.let { put("title", it) }
                }.toString()
            )
            val result = toolExecutor.execute(toolCall)
            val json = org.json.JSONObject(result)
            json.optString("summary", null)
        } catch (e: Exception) {
            Log.e(TAG, "Summarize failed", e)
            null
        }
    }

    private suspend fun autoTagToolExecute(content: String, title: String?): List<String> {
        return try {
            val toolCall = FunctionCall(
                name = "auto_tag",
                arguments = org.json.JSONObject().apply {
                    put("content", content)
                    title?.let { put("title", it) }
                }.toString()
            )
            val result = toolExecutor.execute(toolCall)
            val arr = JSONArray(result)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Auto-tag failed", e)
            emptyList()
        }
    }
}
