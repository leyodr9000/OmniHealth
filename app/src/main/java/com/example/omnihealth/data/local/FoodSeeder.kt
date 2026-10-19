package com.example.omnihealth.data.local

import android.content.Context
import com.example.omnihealth.data.local.dao.DietDao
import com.example.omnihealth.data.local.entity.FoodEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FoodSeed(
    val name: String,
    val nameEn: String = "",
    val category: String,
    val calories: Float,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    val serving: Float = 100f,
)

/**
 * 食物字典预置器。
 *
 * 首次建库时把 `assets/foods.json`（200+ 条）写入 foods 表；
 * 已存在数据则跳过，避免重复插入。
 */
object FoodSeeder {

    private const val ASSET_NAME = "foods.json"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun seed(context: Context, dietDao: DietDao) {
        if (dietDao.foodCount() > 0) return

        val raw = runCatching {
            context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return

        val seeds = runCatching { json.decodeFromString<List<FoodSeed>>(raw) }.getOrNull() ?: return

        val rows = seeds.map { seed ->
            FoodEntity(
                name = seed.name,
                nameEn = seed.nameEn,
                category = seed.category,
                calories = seed.calories,
                protein = seed.protein,
                carbs = seed.carbs,
                fat = seed.fat,
                fiber = seed.fiber,
                servingGrams = seed.serving,
                isCustom = false,
            )
        }
        dietDao.insertFoods(rows)
    }
}
