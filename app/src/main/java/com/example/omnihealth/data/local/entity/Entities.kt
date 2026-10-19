package com.example.omnihealth.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* ------------------------------------------------------------------ */
/* 体征                                                                */
/* ------------------------------------------------------------------ */

@Entity(
    tableName = "vitals",
    indices = [Index("recordedAt"), Index("type")],
)
data class VitalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** 见 [VitalType] */
    val type: String,
    val value: Float,
    /** 第二数值，例如血压的舒张压 */
    val secondary: Float? = null,
    val unit: String,
    val note: String = "",
    val recordedAt: Long = System.currentTimeMillis(),
)

object VitalType {
    const val WEIGHT = "WEIGHT"
    const val BODY_FAT = "BODY_FAT"
    const val HEART_RATE = "HEART_RATE"
    const val BLOOD_PRESSURE = "BLOOD_PRESSURE"
    const val BLOOD_OXYGEN = "BLOOD_OXYGEN"
    const val SLEEP = "SLEEP"
    const val WATER = "WATER"
    const val MOOD = "MOOD"
}

/* ------------------------------------------------------------------ */
/* 习惯                                                                */
/* ------------------------------------------------------------------ */

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val emoji: String = "✅",
    val targetPerDay: Int = 1,
    val unit: String = "次",
    val colorHex: String = "#2F6BFF",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "epochDay"], unique = true),
        Index(value = ["epochDay"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val habitId: Long,
    val epochDay: Long,
    val count: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
)

/* ------------------------------------------------------------------ */
/* 食物字典 + 饮食记录                                                  */
/* ------------------------------------------------------------------ */

@Entity(
    tableName = "foods",
    indices = [Index("name"), Index("category")],
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val nameEn: String = "",
    val category: String,
    /** 每 100 g 的热量 kcal */
    val calories: Float,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f,
    /** 一份的参考克数 */
    val servingGrams: Float = 100f,
    val isCustom: Boolean = false,
)

@Entity(
    tableName = "diet_logs",
    indices = [Index("timestamp"), Index("epochDay"), Index("mealType")],
)
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val foodId: Long? = null,
    val foodName: String,
    val grams: Float,
    /** 见 [MealType] */
    val mealType: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val epochDay: Long,
    val timestamp: Long = System.currentTimeMillis(),
)

object MealType {
    const val BREAKFAST = "BREAKFAST"
    const val LUNCH = "LUNCH"
    const val DINNER = "DINNER"
    const val SNACK = "SNACK"

    val all = listOf(BREAKFAST, LUNCH, DINNER, SNACK)
}

/* ------------------------------------------------------------------ */
/* 步数 + 训练                                                         */
/* ------------------------------------------------------------------ */

@Entity(
    tableName = "steps",
    indices = [Index(value = ["epochDay"], unique = true)],
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val epochDay: Long,
    val steps: Int,
    val goal: Int = 10_000,
    val distanceMeters: Float = 0f,
    val activeMinutes: Int = 0,
    val calories: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "workouts",
    indices = [Index("epochDay"), Index("timestamp")],
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String,
    val durationSec: Int,
    val calories: Int,
    val note: String = "",
    val epochDay: Long,
    val timestamp: Long = System.currentTimeMillis(),
)

/* ------------------------------------------------------------------ */
/* AI 报告                                                             */
/* ------------------------------------------------------------------ */

@Entity(tableName = "ai_reports", indices = [Index("createdAt")])
data class AiReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** WEEKLY / CHAT */
    val kind: String,
    val prompt: String,
    val content: String,
    val score: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

object AiReportKind {
    const val WEEKLY = "WEEKLY"
    const val CHAT = "CHAT"
}
