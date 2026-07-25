package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor() {

    val definitions: List<ToolDefinition> = listOf(
        ToolDefinition(
            function = FunctionDefinition(
                name = FetchContentTool.NAME,
                description = "Fetch and extract main content from a web URL",
                parameters = mapOf("type" to "object", "properties" to emptyMap<String, Any>())
            )
        ),
        ToolDefinition(
            function = FunctionDefinition(
                name = SummarizeTool.NAME,
                description = "Generate a concise summary of article content",
                parameters = mapOf("type" to "object", "properties" to emptyMap<String, Any>())
            )
        ),
        ToolDefinition(
            function = FunctionDefinition(
                name = AutoTagTool.NAME,
                description = "Generate relevant tags for article content",
                parameters = mapOf("type" to "object", "properties" to emptyMap<String, Any>())
            )
        )
    )

    fun getDefinition(name: String): ToolDefinition? {
        return definitions.find { it.function.name == name }
    }
}
