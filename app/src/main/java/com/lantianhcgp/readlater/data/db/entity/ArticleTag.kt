package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity

@Entity(tableName = "article_tags", primaryKeys = ["articleId", "tagId"])
data class ArticleTag(val articleId: String, val tagId: String)
