package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.omnihealth.data.local.entity.AiReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiReportDao {

    @Insert
    suspend fun insert(report: AiReportEntity): Long

    @Query("SELECT * FROM ai_reports ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<AiReportEntity>>

    @Query("SELECT * FROM ai_reports WHERE kind = :kind ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(kind: String): AiReportEntity?

    @Query("DELETE FROM ai_reports WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM ai_reports")
    suspend fun clear()
}
