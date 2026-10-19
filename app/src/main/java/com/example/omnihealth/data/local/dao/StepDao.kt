package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSteps(steps: StepEntity): Long

    @Query("SELECT * FROM steps WHERE epochDay = :epochDay LIMIT 1")
    fun observeDay(epochDay: Long): Flow<StepEntity?>

    @Query("SELECT * FROM steps WHERE epochDay = :epochDay LIMIT 1")
    suspend fun byDay(epochDay: Long): StepEntity?

    @Query("SELECT * FROM steps WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay ASC")
    fun observeRange(from: Long, to: Long): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay ASC")
    suspend fun range(from: Long, to: Long): List<StepEntity>

    @Query("SELECT SUM(steps) FROM steps WHERE epochDay BETWEEN :from AND :to")
    suspend fun totalBetween(from: Long, to: Long): Int?

    /* ---------------- 训练 ---------------- */

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts WHERE epochDay = :epochDay ORDER BY timestamp DESC")
    fun observeWorkouts(epochDay: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE epochDay BETWEEN :from AND :to ORDER BY timestamp DESC")
    suspend fun workoutsBetween(from: Long, to: Long): List<WorkoutEntity>

    @Query("SELECT SUM(calories) FROM workouts WHERE epochDay = :epochDay")
    suspend fun burnedToday(epochDay: Long): Int?

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)
}
