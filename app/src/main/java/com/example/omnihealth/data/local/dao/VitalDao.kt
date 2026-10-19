package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.omnihealth.data.local.entity.VitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalDao {

    @Insert
    suspend fun insert(vital: VitalEntity): Long

    @Delete
    suspend fun delete(vital: VitalEntity)

    @Query("SELECT * FROM vitals ORDER BY recordedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 60): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE type = :type ORDER BY recordedAt DESC LIMIT :limit")
    fun observeByType(type: String, limit: Int = 40): Flow<List<VitalEntity>>

    @Query("SELECT * FROM vitals WHERE type = :type ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(type: String): Flow<VitalEntity?>

    @Query("SELECT * FROM vitals WHERE type = :type AND recordedAt BETWEEN :from AND :to ORDER BY recordedAt ASC")
    suspend fun between(type: String, from: Long, to: Long): List<VitalEntity>

    @Query("SELECT * FROM vitals WHERE recordedAt BETWEEN :from AND :to ORDER BY recordedAt ASC")
    suspend fun allBetween(from: Long, to: Long): List<VitalEntity>

    @Query("SELECT SUM(value) FROM vitals WHERE type = :type AND recordedAt BETWEEN :from AND :to")
    suspend fun sumBetween(type: String, from: Long, to: Long): Float?

    @Query("SELECT SUM(value) FROM vitals WHERE type = :type AND recordedAt BETWEEN :from AND :to")
    fun observeSumBetween(type: String, from: Long, to: Long): Flow<Float?>

    @Query("SELECT * FROM vitals WHERE type = :type ORDER BY recordedAt DESC LIMIT 1")
    suspend fun latest(type: String): VitalEntity?

    @Query("SELECT * FROM vitals ORDER BY recordedAt ASC")
    suspend fun all(): List<VitalEntity>

    @Query("DELETE FROM vitals")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VitalEntity>)
}
