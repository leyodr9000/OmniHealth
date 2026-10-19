package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {

    /* ---------------- 食物字典 ---------------- */

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun foodCount(): Int

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' ORDER BY calories ASC")
    suspend fun searchFoods(query: String): List<FoodEntity>

    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun observeFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name ASC")
    suspend fun foodsByCategory(category: String): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun foodById(id: Long): FoodEntity?

    @Query("DELETE FROM foods WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomFood(id: Long)

    /* ---------------- 饮食记录 ---------------- */

    @Insert
    suspend fun insertLog(log: DietLogEntity): Long

    @Delete
    suspend fun deleteLog(log: DietLogEntity)

    /** 撤销删除：按原 id 写回，保证各页面的聚合结果一致。 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreLog(log: DietLogEntity): Long

    @Query("SELECT * FROM diet_logs WHERE epochDay = :epochDay ORDER BY timestamp ASC")
    fun observeLogs(epochDay: Long): Flow<List<DietLogEntity>>

    @Update
    suspend fun updateLog(log: DietLogEntity)

    @Query("SELECT * FROM diet_logs WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay ASC, timestamp ASC")
    fun observeLogsBetween(from: Long, to: Long): Flow<List<DietLogEntity>>

    @Query("SELECT * FROM diet_logs WHERE epochDay BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun logsBetween(from: Long, to: Long): List<DietLogEntity>

    @Query("SELECT SUM(calories) FROM diet_logs WHERE epochDay = :epochDay")
    fun observeCalories(epochDay: Long): Flow<Float?>

    @Query("SELECT SUM(calories) FROM diet_logs WHERE epochDay BETWEEN :from AND :to")
    suspend fun caloriesBetween(from: Long, to: Long): Float?
}
