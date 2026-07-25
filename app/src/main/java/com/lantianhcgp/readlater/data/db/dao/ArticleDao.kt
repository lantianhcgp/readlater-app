package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.model.ArticleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE status = :status ORDER BY createdAt DESC")
    fun getArticlesByStatus(status: ArticleStatus): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchArticles(query: String): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :articleId")
    suspend fun getArticleById(articleId: String): Article?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("DELETE FROM articles WHERE id = :articleId")
    suspend fun deleteArticleById(articleId: String)

    @Query("UPDATE articles SET status = :status, updatedAt = :updatedAt WHERE id = :articleId")
    suspend fun updateStatus(articleId: String, status: ArticleStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE articles SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :articleId")
    suspend fun updateFavorite(articleId: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
}
