package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lantianhcgp.readlater.data.model.ArticleStatus
import java.util.UUID

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String? = null,
    val content: String? = null,
    val plainText: String? = null,
    val summary: String? = null,
    val imageUrl: String? = null,
    val sourceDomain: String = "",
    val readingTimeMinutes: Int? = null,
    val status: ArticleStatus = ArticleStatus.PENDING,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
