package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor() {

    val definitions: List<ToolDefinition> = listOf(
        FetchContentTool.DEFINITION,
        SummarizeTool.DEFINITION,
        AutoTagTool.DEFINITION
    )

    fun getDefinition(name: String): ToolDefinition? {
        return definitions.find { it.function.name == name }
    }
}
