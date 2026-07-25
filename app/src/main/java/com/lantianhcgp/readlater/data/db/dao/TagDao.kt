package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lantianhcgp.readlater.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): Tag?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticleTag(articleTag: com.lantianhcgp.readlater.data.db.entity.ArticleTag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM article_tags WHERE tagId = :tagId")
    suspend fun deleteArticleTags(tagId: String)

    @Query("SELECT tags.* FROM tags INNER JOIN article_tags ON tags.id = article_tags.tagId WHERE article_tags.articleId = :articleId")
    fun getTagsForArticle(articleId: String): Flow<List<Tag>>
}
