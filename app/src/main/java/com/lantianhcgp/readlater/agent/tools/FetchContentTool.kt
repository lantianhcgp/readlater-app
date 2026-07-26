package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.util.HtmlParser
import com.lantianhcgp.readlater.util.Logger
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
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Sec-Fetch-Dest", "document")
                .addHeader("Sec-Fetch-Mode", "navigate")
                .addHeader("Sec-Fetch-Site", "none")
                .addHeader("Sec-Fetch-User", "?1")
                .addHeader("Cache-Control", "max-age=0")
                .build()

            Logger.d("FetchContent", "发送请求: $url")
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            Logger.d("FetchContent", "响应码: ${response.code}, 内容长度: ${body.length}")

            if (!response.isSuccessful) {
                val errorMsg = "HTTP ${response.code}: ${body.take(200)}"
                Logger.e("FetchContent", errorMsg)
                throw Exception(errorMsg)
            }

            if (body.isEmpty()) {
                throw Exception("响应内容为空")
            }

            val parsed = HtmlParser.parse(body, url)
            Logger.d("FetchContent", "解析完成: title=${parsed.title.take(50)}, contentLength=${parsed.content.length}")

            JSONObject().apply {
                put("title", parsed.title)
                put("description", parsed.description)
                put("content", parsed.content)
                put("imageUrl", parsed.imageUrl ?: JSONObject.NULL)
                put("sourceDomain", parsed.sourceDomain)
                put("readingTimeMinutes", parsed.readingTimeMinutes)
            }.toString()
        } catch (e: Exception) {
            val msg = e.message ?: e.javaClass.simpleName
            Logger.e("FetchContent", "抓取失败: $msg")
            JSONObject().apply {
                put("error", msg)
            }.toString()
        }
    }
}
