package com.lantianhcgp.readlater.agent.providers

import com.lantianhcgp.readlater.agent.*
import com.lantianhcgp.readlater.data.model.LlmConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class OpenAiProvider @Inject constructor(
    private val client: OkHttpClient,
    private val config: LlmConfig
) : LlmProvider {

    override fun getConfig(): LlmConfig = config

    override suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>?,
        temperature: Double,
        maxTokens: Int?
    ): LlmResponse = withContext(Dispatchers.IO) {
        val body = buildRequestBody(messages, tools, temperature, maxTokens)
        val request = Request.Builder()
            .url("${config.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: $responseBody")
        }
        parseResponse(responseBody)
    }

    private fun buildRequestBody(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>?,
        temperature: Double,
        maxTokens: Int?
    ): JSONObject {
        val json = JSONObject().apply {
            put("model", config.model)
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                        msg.name?.let { put("name", it) }
                        msg.toolCallId?.let { put("tool_call_id", it) }
                        msg.toolCalls?.let { calls ->
                            put("tool_calls", JSONArray().apply {
                                calls.forEach { tc ->
                                    put(JSONObject().apply {
                                        put("id", tc.id)
                                        put("type", tc.type)
                                        put("function", JSONObject().apply {
                                            put("name", tc.function.name)
                                            put("arguments", tc.function.arguments)
                                        })
                                    })
                                }
                            })
                        }
                    })
                }
            })
            put("temperature", temperature)
            maxTokens?.let { put("max_tokens", it) }
            tools?.let { toolList ->
                put("tools", JSONArray().apply {
                    toolList.forEach { tool ->
                        put(JSONObject().apply {
                            put("type", tool.type)
                            put("function", JSONObject().apply {
                                put("name", tool.function.name)
                                put("description", tool.function.description)
                                put("parameters", JSONObject(tool.function.parameters))
                            })
                        })
                    }
                })
            }
        }
        return json
    }

    private fun parseResponse(responseBody: String): LlmResponse {
        val json = JSONObject(responseBody)
        val choice = json.getJSONArray("choices").getJSONObject(0)
        val message = choice.getJSONObject("message")

        val content = message.optString("content", null)
        val toolCalls = message.optJSONArray("toolCalls")?.let { arr ->
            (0 until arr.length()).map { i ->
                val tc = arr.getJSONObject(i)
                ToolCall(
                    id = tc.getString("id"),
                    type = tc.optString("type", "function"),
                    function = FunctionCall(
                        name = tc.getJSONObject("function").getString("name"),
                        arguments = tc.getJSONObject("function").getString("arguments")
                    )
                )
            }
        }

        val usage = json.optJSONObject("usage")?.let { u ->
            Usage(
                promptTokens = u.optInt("prompt_tokens", 0),
                completionTokens = u.optInt("completion_tokens", 0),
                totalTokens = u.optInt("total_tokens", 0)
            )
        }

        return LlmResponse(content = content, toolCalls = toolCalls, usage = usage)
    }
}
