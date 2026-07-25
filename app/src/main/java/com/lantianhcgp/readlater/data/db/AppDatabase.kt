package com.lantianhcgp.readlater.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.ArticleTag
import com.lantianhcgp.readlater.data.db.entity.Highlight
import com.lantianhcgp.readlater.data.db.entity.Tag

@Database(
    entities = [Article::class, Tag::class, ArticleTag::class, Highlight::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun tagDao(): TagDao
    abstract fun highlightDao(): HighlightDao
}
