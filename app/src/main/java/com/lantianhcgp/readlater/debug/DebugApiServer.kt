package com.lantianhcgp.readlater.debug

import android.util.Log
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.util.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder

class DebugApiServer(
    private val port: Int = 8080,
    private val articleDao: ArticleDao,
    private val tagDao: TagDao
) {
    private var serverSocket: ServerSocket? = null
    private var running = false
    fun start() {
        try {
            serverSocket = ServerSocket(port)
            running = true
            Thread {
                while (running) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        Thread { handleClient(client) }.start()
                    } catch (e: Exception) {
                        if (running) Log.e("DebugApi", "Accept error: ${e.message}")
                    }
                }
            }.start()
            Log.i("DebugApi", "Debug API server started on port $port")
            Logger.i("DebugApi", "Debug API server started on http://localhost:$port")
        } catch (e: Exception) {
            Log.e("DebugApi", "Failed to start debug server: ${e.message}")
        }
    }

    fun stop() {
        running = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        Log.i("DebugApi", "Debug API server stopped")
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]

            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
            }

            val response = runBlocking { when {
                path == "/logs" && method == "GET" -> handleLogs()
                path == "/articles" && method == "GET" -> handleArticles()
                path.startsWith("/article/") && method == "GET" -> handleArticle(path)
                path == "/config" && method == "GET" -> handleConfig()
                path == "/pipeline" && method == "GET" -> handlePipeline()
                path == "/pipeline/history" && method == "GET" -> handlePipelineHistory()
                path.startsWith("/raw/") && method == "GET" -> handleRawContent(path)
                path == "/clear" && method == "POST" -> handleClear()
                else -> """{"error": "Unknown endpoint. Available: /logs, /articles, /article/{id}, /config, /pipeline, /pipeline/history, /raw/{type}, /clear"}"""
            } }

            sendResponse(socket, response)
        } catch (e: Exception) {
            Log.e("DebugApi", "Handle error: ${e.message}")
            try { sendResponse(socket, """{"error": "${e.message?.replace("\"", "'")}"}""") } catch (_: Exception) {}
        } finally {
            try { socket.close() } catch (_: Exception) {}
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
        val tags = tagDao.getTagsForArticleList(article.id)
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
            put("message", "Debug API is running")
            put("endpoints", JSONArray().apply {
                put("GET /logs - View all logs")
                put("GET /articles - List all articles")
                put("GET /article/{id} - View article details + plainText")
                put("GET /pipeline - View last processing pipeline")
                put("GET /pipeline/history - View pipeline history")
                put("GET /raw/html - Raw HTML from last pipeline")
                put("GET /raw/formatted - AI formatted content")
                put("GET /raw/clean - Clean content after title strip")
                put("POST /clear - Clear debug data")
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

    private fun sendResponse(socket: Socket, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val response = buildString {
            append("HTTP/1.1 200 OK\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        val os: OutputStream = socket.getOutputStream()
        os.write(response.toByteArray())
        os.write(bytes)
        os.flush()
    }
}
