package com.lantianhcgp.readlater.data.repository

import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {

    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    fun getTagsForArticle(articleId: String): Flow<List<Tag>> =
        tagDao.getTagsForArticle(articleId)

    fun getArticlesByTag(tagId: String): Flow<List<Article>> =
        tagDao.getArticlesByTag(tagId)

    suspend fun createTag(name: String): Tag {
        val existing = tagDao.getTagByName(name)
        if (existing != null) return existing

        val tag = Tag(id = UUID.randomUUID().toString(), name = name)
        tagDao.insertTag(tag)
        return tag
    }

    suspend fun deleteTag(tag: Tag) {
        tagDao.deleteArticleTags(tag.id)
        tagDao.deleteTag(tag)
    }
}
