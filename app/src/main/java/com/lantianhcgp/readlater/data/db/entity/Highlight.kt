package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "highlights")
data class Highlight(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val articleId: String,
    val selectedText: String,
    val note: String? = null,
    val color: String = "#FF9800",
    val startOffset: Int = 0,
    val endOffset: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
