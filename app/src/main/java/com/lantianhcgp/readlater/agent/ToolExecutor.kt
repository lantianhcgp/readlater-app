package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutor @Inject constructor(
    private val fetchContentTool: FetchContentTool,
    private val summarizeTool: SummarizeTool,
    private val autoTagTool: AutoTagTool
) {

    suspend fun execute(toolCall: FunctionCall): String {
        val args = try {
            JSONObject(toolCall.arguments)
        } catch (_: Exception) {
            JSONObject()
        }

        return when (toolCall.name) {
            FetchContentTool.NAME -> {
                val url = args.optString("url", "")
                if (url.isBlank()) {
                    """{"error": "Missing url parameter"}"""
                } else {
                    fetchContentTool.execute(url)
                }
            }

            SummarizeTool.NAME -> {
                val content = args.optString("content", "")
                val title = args.optString("title", null)
                if (content.isBlank()) {
                    """{"error": "Missing content parameter"}"""
                } else {
                    val summary = summarizeTool.execute(content, title)
                    JSONObject().apply {
                        put("summary", summary)
                    }.toString()
                }
            }

            AutoTagTool.NAME -> {
                val content = args.optString("content", "")
                val title = args.optString("title", null)
                if (content.isBlank()) {
                    """{"error": "Missing content parameter"}"""
                } else {
                    autoTagTool.execute(content, title)
                }
            }

            else -> """{"error": "Unknown tool: ${toolCall.name}"}"""
        }
    }
}
