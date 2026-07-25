package com.lantianhcgp.readlater.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

data class ParsedContent(
    val title: String,
    val description: String,
    val content: String,
    val plainText: String,
    val imageUrl: String?,
    val sourceDomain: String,
    val readingTimeMinutes: Int
)

object HtmlParser {

    private const val WORDS_PER_MINUTE = 200

    fun parse(html: String, url: String): ParsedContent {
        val doc = Jsoup.parse(html, url)

        val title = doc.title().ifBlank { extractMeta(doc, "og:title").orEmpty() }
        val description = extractMeta(doc, "og:description")
            ?: extractMeta(doc, "description").orEmpty()

        val imageUrl = extractMeta(doc, "og:image")
            ?: doc.selectFirst("article img, .post-content img, .entry-content img, main img")
                ?.attr("abs:src")

        val article = doc.selectFirst("article") ?: doc.selectFirst("main")
            ?: doc.selectFirst("[role=main]") ?: doc.body()

        val cleanArticle = article.clone().apply {
            select("script, style, nav, header, footer, aside, noscript, iframe").remove()
        }

        val content = cleanArticle.html()
        val plainText = cleanArticle.text()
        val wordCount = plainText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val readingTime = maxOf(1, (wordCount.toDouble() / WORDS_PER_MINUTE).toInt())

        val sourceDomain = try {
            URI(url).host.orEmpty()
        } catch (_: Exception) {
            url
        }

        return ParsedContent(
            title = title,
            description = description,
            content = content,
            plainText = plainText,
            imageUrl = imageUrl,
            sourceDomain = sourceDomain,
            readingTimeMinutes = readingTime
        )
    }

    private fun extractMeta(doc: Document, property: String): String? {
        return doc.selectFirst("meta[property=$property], meta[name=$property]")
            ?.attr("content")
            ?.takeIf { it.isNotBlank() }
    }
}
