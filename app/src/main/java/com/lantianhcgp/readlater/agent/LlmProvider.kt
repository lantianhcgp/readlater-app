package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.data.model.LlmConfig

data class ChatMessage(
    val role: String,
    val content: String,
    val name: String? = null,
    val toolCallId: String? = null,
    val toolCalls: List<ToolCall>? = null
)

data class ToolDefinition(
    val type: String = "function",
    val function: FunctionDefinition
)

data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: FunctionCall
)

data class FunctionCall(
    val name: String,
    val arguments: String
)

data class LlmResponse(
    val content: String?,
    val toolCalls: List<ToolCall>?,
    val usage: Usage?
)

data class Usage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

interface LlmProvider {
    suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>? = null,
        temperature: Double = 0.7,
        maxTokens: Int? = null
    ): LlmResponse

    fun getConfig(): LlmConfig
}
