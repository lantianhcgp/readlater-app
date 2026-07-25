package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lantianhcgp.readlater.data.db.entity.Highlight
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    @Query("SELECT * FROM highlights WHERE articleId = :articleId ORDER BY startOffset ASC")
    fun getHighlightsForArticle(articleId: String): Flow<List<Highlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: Highlight)

    @Delete
    suspend fun deleteHighlight(highlight: Highlight)

    @Query("DELETE FROM highlights WHERE id = :highlightId")
    suspend fun deleteHighlightById(highlightId: String)
}
