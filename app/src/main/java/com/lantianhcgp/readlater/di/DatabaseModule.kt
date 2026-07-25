package com.lantianhcgp.readlater.di

import android.content.Context
import androidx.room.Room
import com.lantianhcgp.readlater.data.db.AppDatabase
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "read_later_db"
        ).build()
    }

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideHighlightDao(database: AppDatabase): HighlightDao {
        return database.highlightDao()
    }
}
