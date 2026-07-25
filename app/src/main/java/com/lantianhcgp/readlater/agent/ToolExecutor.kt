package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.FormatContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutor @Inject constructor(
    private val fetchContentTool: FetchContentTool,
    private val summarizeTool: SummarizeTool,
    private val autoTagTool: AutoTagTool,
    private val formatContentTool: FormatContentTool
) {
    suspend fun executeSummarize(content: String, title: String?, provider: LlmProvider): String {
        return summarizeTool.execute(content, title, provider)
    }

    suspend fun executeAutoTag(content: String, title: String?, provider: LlmProvider): String {
        return autoTagTool.execute(content, title, provider)
    }

    suspend fun executeFormatContent(rawHtml: String, title: String?, provider: LlmProvider): String {
        return formatContentTool.execute(rawHtml, title, provider)
    }
}
