package com.lantianhcgp.readlater.data.repository

import com.lantianhcgp.readlater.agent.AgentOrchestrator
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.ArticleTag
import com.lantianhcgp.readlater.data.db.entity.Tag
import com.lantianhcgp.readlater.data.model.ArticleStatus
import com.lantianhcgp.readlater.data.model.LlmConfig
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val tagDao: TagDao,
    private val agentOrchestrator: AgentOrchestrator
) {

    // ── Query methods ──

    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    fun getArticlesByStatus(status: ArticleStatus): Flow<List<Article>> =
        articleDao.getArticlesByStatus(status)

    fun getFavoriteArticles(): Flow<List<Article>> = articleDao.getFavoriteArticles()

    fun searchArticles(query: String): Flow<List<Article>> = articleDao.searchArticles(query)

    suspend fun getArticleById(articleId: String): Article? = articleDao.getArticleById(articleId)

    // ── Write methods ──

    /**
     * Save a new article in PENDING state. Returns the article ID.
     */
    suspend fun addArticle(url: String): String {
        val article = Article(id = UUID.randomUUID().toString(), url = url)
        articleDao.insertArticle(article)
        return article.id
    }

    /**
     * Process an article: call agent to extract content, summary, tags.
     * Updates the article in DB on success; sets ERROR status on failure.
     */
    suspend fun processArticle(articleId: String, config: LlmConfig) {
        val article = articleDao.getArticleById(articleId) ?: return

        articleDao.updateStatus(articleId, ArticleStatus.PROCESSING)

        try {
            val result = agentOrchestrator.processArticle(article.url, config)

            if (result.error != null) {
                articleDao.updateStatus(articleId, ArticleStatus.ERROR)
                return
            }

            val now = System.currentTimeMillis()
            articleDao.updateArticle(
                article.copy(
                    title = result.title,
                    summary = result.summary,
                    plainText = result.plainText,
                    imageUrl = result.imageUrl,
                    sourceDomain = result.sourceDomain,
                    readingTimeMinutes = result.readingTimeMinutes,
                    status = ArticleStatus.READY,
                    updatedAt = now
                )
            )

            // Attach tags
            for (tagName in result.tags) {
                val tag = tagDao.getTagByName(tagName)
                    ?: Tag(id = UUID.randomUUID().toString(), name = tagName).also {
                        tagDao.insertTag(it)
                    }
                tagDao.insertArticleTag(ArticleTag(articleId = articleId, tagId = tag.id))
            }
        } catch (e: Exception) {
            articleDao.updateStatus(articleId, ArticleStatus.ERROR)
        }
    }

    /**
     * Toggle the favorite status of an article.
     */
    suspend fun toggleFavorite(articleId: String) {
        val article = articleDao.getArticleById(articleId) ?: return
        articleDao.updateFavorite(articleId, !article.isFavorite)
    }

    /**
     * Delete an article and its tag associations.
     */
    suspend fun deleteArticle(article: Article) {
        articleDao.deleteArticle(article)
    }
}
