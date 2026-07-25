package com.lantianhcgp.readlater.agent

import android.util.Log
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.data.model.LlmConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

data class AgentResult(
    val title: String? = null,
    val summary: String? = null,
    val plainText: String? = null,
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
    private val llmProviderFactory: LlmProviderFactory
) {

    companion object {
        private const val TAG = "AgentOrchestrator"
    }

    suspend fun processUrl(url: String, config: LlmConfig): AgentResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing URL: $url")
            val provider = llmProviderFactory.create(config)

            val fetchResult = fetchContentTool.execute(url)
            val fetchJson = org.json.JSONObject(fetchResult)

            if (fetchJson.has("error")) {
                return@withContext AgentResult(error = "Failed to fetch: ${fetchJson.getString("error")}")
            }

            val title = fetchJson.optString("title", null)
            val plainText = fetchJson.optString("plainText", "")
            val imageUrl = fetchJson.optString("imageUrl", null)
            val sourceDomain = fetchJson.optString("sourceDomain", null)
            val readingTime = fetchJson.optInt("readingTimeMinutes", 0)

            val summary = toolExecutor.executeSummarize(plainText, title, provider)
            val tags = toolExecutor.executeAutoTag(plainText, title, provider)

            Log.d(TAG, "Done: title=$title, tags=$tags")

            AgentResult(
                title = title,
                summary = summary,
                plainText = plainText,
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
}
