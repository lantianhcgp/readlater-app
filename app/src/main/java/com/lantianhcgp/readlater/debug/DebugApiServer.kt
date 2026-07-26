package com.lantianhcgp.readlater.debug

import android.util.Log
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URLDecoder

class DebugApiServer(
    private val port: Int = 8080,
    private val articleDao: ArticleDao,
    private val tagDao: TagDao
) {
    private var server: com.sun.net.httpserver.HttpServer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        try {
            server = com.sun.net.httpserver.HttpServer.create(InetSocketAddress(port), 0)
            server?.createContext("/") { exchange ->
                scope.launch { handleRequest(exchange) }
            }
            server?.executor = java.util.concurrent.Executors.newSingleThreadExecutor()
            server?.start()
            Log.i("DebugApi", "Debug API server started on port $port")
            Logger.i("DebugApi", "Debug API server started on http://localhost:$port")
        } catch (e: Exception) {
            Log.e("DebugApi", "Failed to start debug server: ${e.message}")
        }
    }

    fun stop() {
        server?.stop(0)
        server = null
        Log.i("DebugApi", "Debug API server stopped")
    }

    private suspend fun handleRequest(exchange: com.sun.net.httpserver.HttpExchange) {
        try {
            val path = exchange.requestURI.path
            val method = exchange.requestMethod
            val response = when {
                path == "/logs" && method == "GET" -> handleLogs()
                path == "/articles" && method == "GET" -> handleArticles()
                path.startsWith("/article/") && method == "GET" -> handleArticle(path)
                path == "/config" && method == "GET" -> handleConfig()
                path == "/pipeline" && method == "GET" -> handlePipeline()
                path == "/pipeline/history" && method == "GET" -> handlePipelineHistory()
                path == "/raw/" && method == "GET" -> handleRawContent(path)
                path == "/clear" && method == "POST" -> handleClear()
                else -> """{"error": "Unknown endpoint. Available: /logs, /articles, /article/{id}, /config, /pipeline, /pipeline/history, /raw/{type}, /clear"}"""
            }
            sendResponse(exchange, response)
        } catch (e: Exception) {
            sendResponse(exchange, """{"error": "${e.message?.replace("\"", "'")}"}""", 500)
        }
    }

    private fun handleLogs(): String {
        val logs = Logger.logs.value
        val arr = JSONArray()
        for (log in logs) {
            arr.put(JSONObject().apply {
                put("time", log.timestamp)
                put("level", log.level.name)
                put("tag", log.tag)
                put("message", log.message)
            })
        }
        return JSONObject().apply {
            put("count", logs.size)
            put("logs", arr)
        }.toString()
    }

    private suspend fun handleArticles(): String {
        val articles = articleDao.getAllArticlesList()
        val arr = JSONArray()
        for (a in articles) {
            val tags = tagDao.getTagsForArticleList(a.id)
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("url", a.url)
                put("title", a.title)
                put("status", a.status.name)
                put("sourceDomain", a.sourceDomain)
                put("readingTimeMinutes", a.readingTimeMinutes ?: 0)
                put("isFavorite", a.isFavorite)
                put("hasPlainText", !a.plainText.isNullOrBlank())
                put("plainTextLength", a.plainText?.length ?: 0)
                put("hasSummary", !a.summary.isNullOrBlank())
                put("tags", JSONArray(tags.map { it.name }))
                put("createdAt", a.createdAt)
            })
        }
        return JSONObject().apply {
            put("count", articles.size)
            put("articles", arr)
        }.toString()
    }

    private suspend fun handleArticle(path: String): String {
        val id = path.removePrefix("/article/")
        val article = articleDao.getArticleById(id)
            ?: return """{"error": "Article not found: $id"}"""
        val tags = tagDao.getTagsForArticle(article.id)
        return JSONObject().apply {
            put("id", article.id)
            put("url", article.url)
            put("title", article.title)
            put("status", article.status.name)
            put("sourceDomain", article.sourceDomain)
            put("readingTimeMinutes", article.readingTimeMinutes ?: 0)
            put("imageUrl", article.imageUrl)
            put("isFavorite", article.isFavorite)
            put("plainText", article.plainText)
            put("summary", article.summary)
            put("tags", JSONArray(tags.map { it.name }))
            put("createdAt", article.createdAt)
            put("updatedAt", article.updatedAt)
        }.toString()
    }

    private fun handleConfig(): String {
        return JSONObject().apply {
            put("message", "Config is stored in SharedPreferences. Check Settings screen.")
            put("debugEndpoints", JSONArray().apply {
                put("/logs - View all logs")
                put("/articles - List all articles")
                put("/article/{id} - View article details + plainText")
                put("/pipeline - View last processing pipeline")
                put("/pipeline/history - View pipeline history")
                put("/clear - Clear debug data (POST)")
            })
        }.toString()
    }

    private fun handlePipeline(): String {
        val snapshot = DebugData.lastPipeline.value
            ?: return """{"message": "No pipeline data yet. Process an article first."}"""
        return snapshotToJson(snapshot).toString()
    }

    private fun handlePipelineHistory(): String {
        val history = DebugData.pipelineHistory.value
        val arr = JSONArray()
        for (snapshot in history) {
            arr.put(snapshotToJson(snapshot))
        }
        return JSONObject().apply {
            put("count", history.size)
            put("pipelines", arr)
        }.toString()
    }

    private fun handleRawContent(path: String): String {
        val type = URLDecoder.decode(path.removePrefix("/raw/"), "UTF-8")
        val snapshot = DebugData.lastPipeline.value
            ?: return """{"error": "No pipeline data"}"""
        return when (type) {
            "html" -> JSONObject().apply { put("content", snapshot.rawHtml) }.toString()
            "formatted" -> JSONObject().apply { put("content", snapshot.formattedContent) }.toString()
            "clean" -> JSONObject().apply { put("content", snapshot.cleanContent) }.toString()
            else -> """{"error": "Unknown type: $type. Use: html, formatted, clean"}"""
        }
    }

    private fun handleClear(): String {
        DebugData.clear()
        Logger.clear()
        return """{"message": "Debug data cleared"}"""
    }

    private fun snapshotToJson(s: PipelineSnapshot): JSONObject {
        return JSONObject().apply {
            put("url", s.url)
            put("title", s.title)
            put("error", s.error)
            put("tags", JSONArray(s.tags))
            put("summary", s.summary)
            put("rawHtmlLength", s.rawHtml.length)
            put("formattedContentLength", s.formattedContent.length)
            put("cleanContentLength", s.cleanContent.length)
            put("timestamp", s.timestamp)
            put("rawHtml", s.rawHtml)
            put("formattedContent", s.formattedContent)
            put("cleanContent", s.cleanContent)
        }
    }

    private fun sendResponse(exchange: com.sun.net.httpserver.HttpExchange, body: String, code: Int = 200) {
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
        val bytes = body.toByteArray(Charsets.UTF_8)
        exchange.sendResponseHeaders(code, bytes.size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(bytes)
        os.close()
    }
}
