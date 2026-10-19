package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.omnihealth.data.local.entity.HabitEntity
import com.example.omnihealth.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY createdAt ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    suspend fun allHabits(): List<HabitEntity>

    @Query("SELECT * FROM habit_logs WHERE epochDay = :epochDay")
    fun observeLogs(epochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE epochDay BETWEEN :from AND :to")
    suspend fun logsBetween(from: Long, to: Long): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun clearLog(habitId: Long, epochDay: Long)

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND epochDay BETWEEN :from AND :to")
    suspend fun countInRange(habitId: Long, from: Long, to: Long): Int

    @Query("SELECT COUNT(*) FROM habit_logs WHERE epochDay BETWEEN :from AND :to")
    suspend fun totalChecks(from: Long, to: Long): Int

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun clearLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(items: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(items: List<HabitLogEntity>)
}
