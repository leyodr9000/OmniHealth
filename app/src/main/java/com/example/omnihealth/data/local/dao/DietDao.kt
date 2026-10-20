package com.example.omnihealth.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.MealTemplateEntity
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

    /** 最近常吃：按出现次数、再按最近时间排序，只取能对应到食物库的记录。 */
    @Query(
        """
        SELECT foodId AS foodId, foodName AS foodName, COUNT(*) AS times, MAX(timestamp) AS lastAt,
               MAX(grams) AS grams
        FROM diet_logs
        WHERE foodId IS NOT NULL
        GROUP BY foodId
        ORDER BY times DESC, lastAt DESC
        LIMIT :limit
        """,
    )
    fun observeRecentFoods(limit: Int): Flow<List<RecentFood>>

    /** 上一次同类餐的记录（按时间倒序，取最近的一天）。 */
    @Query(
        """
        SELECT * FROM diet_logs
        WHERE mealType = :mealType AND epochDay = (
            SELECT MAX(epochDay) FROM diet_logs WHERE mealType = :mealType
        )
        ORDER BY timestamp ASC
        """,
    )
    suspend fun lastMeal(mealType: String): List<DietLogEntity>

    @Query("SELECT * FROM meal_templates ORDER BY createdAt DESC LIMIT 8")
    fun observeTemplates(): Flow<List<MealTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MealTemplateEntity): Long

    @Query("DELETE FROM meal_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

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

    @Query("SELECT * FROM diet_logs ORDER BY timestamp ASC")
    suspend fun allLogs(): List<DietLogEntity>

    @Query("DELETE FROM diet_logs")
    suspend fun clearLogs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(items: List<DietLogEntity>)
}

/** 最近常吃的投影：次数、最近时间、最近一次克数。 */
data class RecentFood(
    val foodId: Long,
    val foodName: String,
    val times: Int,
    val lastAt: Long,
    val grams: Float,
)
