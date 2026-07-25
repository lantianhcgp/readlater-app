package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.util.HtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named

class FetchContentTool @Inject constructor(
    @Named("plain") private val client: OkHttpClient
) {

    companion object {
        const val NAME = "fetch_content"
    }

    suspend fun execute(url: String): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty response body")

            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: $body")
            }

            val parsed = HtmlParser.parse(body, url)

            JSONObject().apply {
                put("title", parsed.title)
                put("description", parsed.description)
                put("content", parsed.content)
                put("imageUrl", parsed.imageUrl ?: JSONObject.NULL)
                put("sourceDomain", parsed.sourceDomain)
                put("readingTimeMinutes", parsed.readingTimeMinutes)
            }.toString()
        } catch (e: Exception) {
            JSONObject().apply {
                put("error", e.message ?: "Unknown error")
            }.toString()
        }
    }
}
