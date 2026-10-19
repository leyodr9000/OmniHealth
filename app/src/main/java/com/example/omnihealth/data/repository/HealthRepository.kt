package com.example.omnihealth.data.repository

import com.example.omnihealth.data.datastore.ApiConfig
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.data.datastore.SettingsPreferences
import com.example.omnihealth.data.datastore.UserProfile
import com.example.omnihealth.data.local.AppDatabase
import com.example.omnihealth.data.local.entity.AiReportEntity
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.HabitEntity
import com.example.omnihealth.data.local.entity.HabitLogEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.VitalEntity
import com.example.omnihealth.data.local.entity.VitalType
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.domain.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 单一数据源：把 5 个 DAO 与 DataStore 设置聚合成 UI 可直接消费的流。
 */
class HealthRepository(
    private val db: AppDatabase,
    private val preferences: SettingsPreferences,
) {

    val settings: Flow<AppSettings> = preferences.settings

    /* ---------------- 体征 ---------------- */

    fun latestWeight(): Flow<VitalEntity?> = db.vitalDao().observeLatest(VitalType.WEIGHT)

    fun latestHeartRate(): Flow<VitalEntity?> = db.vitalDao().observeLatest(VitalType.HEART_RATE)

    fun latestSleep(): Flow<VitalEntity?> = db.vitalDao().observeLatest(VitalType.SLEEP)

    fun weightTrend(limit: Int = 30): Flow<List<VitalEntity>> =
        db.vitalDao().observeByType(VitalType.WEIGHT, limit)

    fun waterToday(): Flow<Float> {
        val day = TimeUtils.epochDayNow()
        return db.vitalDao().observeByType(VitalType.WATER, 200)
            .map { list -> list.filter { TimeUtils.epochDayOf(it.recordedAt) == day }.sumOf { it.value.toDouble() }.toFloat() }
    }

    suspend fun logWeight(kg: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.WEIGHT, value = kg, unit = "kg"),
    )

    suspend fun logWater(ml: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.WATER, value = ml, unit = "ml"),
    )

    suspend fun logSleep(hours: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.SLEEP, value = hours, unit = "h"),
    )

    suspend fun logMood(score: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.MOOD, value = score, unit = "pt"),
    )

    suspend fun logHeartRate(bpm: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.HEART_RATE, value = bpm, unit = "bpm"),
    )

    /* ---------------- 步数 / 训练 ---------------- */

    fun todaySteps(): Flow<StepEntity?> = db.stepDao().observeDay(TimeUtils.epochDayNow())

    fun stepsRange(days: Int = 7): Flow<List<StepEntity>> {
        val end = TimeUtils.epochDayNow()
        return db.stepDao().observeRange(end - days + 1, end)
    }

    suspend fun saveSteps(steps: Int, distanceMeters: Float = steps * 0.72f, activeMinutes: Int = steps / 110) {
        val day = TimeUtils.epochDayNow()
        val existing = db.stepDao().byDay(day)
        db.stepDao().upsertSteps(
            StepEntity(
                id = existing?.id ?: 0L,
                epochDay = day,
                steps = steps,
                goal = existing?.goal ?: 10_000,
                distanceMeters = distanceMeters,
                activeMinutes = activeMinutes,
                calories = (steps * 0.04f).toInt(),
            ),
        )
    }

    fun todayWorkouts(): Flow<List<WorkoutEntity>> = db.stepDao().observeWorkouts(TimeUtils.epochDayNow())

    suspend fun logWorkout(type: String, durationSec: Int, calories: Int, note: String = "") {
        db.stepDao().insertWorkout(
            WorkoutEntity(
                type = type,
                durationSec = durationSec,
                calories = calories,
                note = note,
                epochDay = TimeUtils.epochDayNow(),
            ),
        )
    }

    /* ---------------- 饮食 ---------------- */

    fun todayDietLogs(): Flow<List<DietLogEntity>> = db.dietDao().observeLogs(TimeUtils.epochDayNow())

    fun todayCalories(): Flow<Float> =
        db.dietDao().observeCalories(TimeUtils.epochDayNow()).map { it ?: 0f }

    fun dietLogsBetween(days: Int = 7): Flow<List<DietLogEntity>> {
        val end = TimeUtils.epochDayNow()
        return db.dietDao().observeLogsBetween(end - days + 1, end)
    }

    suspend fun searchFoods(query: String): List<FoodEntity> =
        if (query.isBlank()) emptyList() else db.dietDao().searchFoods(query.trim())

    fun allFoods(): Flow<List<FoodEntity>> = db.dietDao().observeFoods()

    suspend fun logFood(food: FoodEntity, grams: Float, mealType: String = MealType.LUNCH) {
        val ratio = grams / 100f
        db.dietDao().insertLog(
            DietLogEntity(
                foodId = food.id,
                foodName = food.name,
                grams = grams,
                mealType = mealType,
                calories = food.calories * ratio,
                protein = food.protein * ratio,
                carbs = food.carbs * ratio,
                fat = food.fat * ratio,
                epochDay = TimeUtils.epochDayNow(),
            ),
        )
    }

    suspend fun deleteDietLog(log: DietLogEntity) = db.dietDao().deleteLog(log)

    /* ---------------- 习惯 ---------------- */

    fun habits(): Flow<List<HabitEntity>> = db.habitDao().observeHabits()

    fun habitLogsToday(): Flow<List<HabitLogEntity>> = db.habitDao().observeLogs(TimeUtils.epochDayNow())

    suspend fun toggleHabit(habitId: Long) {
        val day = TimeUtils.epochDayNow()
        val existing = db.habitDao().logsBetween(day, day).firstOrNull { it.habitId == habitId }
        if (existing == null) {
            db.habitDao().upsertLog(HabitLogEntity(habitId = habitId, epochDay = day, count = 1))
        } else {
            db.habitDao().clearLog(habitId, day)
        }
    }

    /* ---------------- AI 报告 ---------------- */

    fun aiReports(): Flow<List<AiReportEntity>> = db.aiReportDao().observeRecent()

    suspend fun saveAiReport(report: AiReportEntity) = db.aiReportDao().insert(report)

    suspend fun latestWeeklyReport(): AiReportEntity? = db.aiReportDao().latest("WEEKLY")

    /* ---------------- 首页聚合 ---------------- */

    data class TodaySnapshot(
        val steps: Int = 0,
        val stepGoal: Int = 10_000,
        val waterMl: Float = 0f,
        val sleepHours: Float = 0f,
        val caloriesIn: Float = 0f,
        val caloriesOut: Int = 0,
        val weightKg: Float = 0f,
    )

    private data class DayCore(
        val settings: AppSettings,
        val steps: StepEntity?,
        val waterMl: Float,
        val sleep: VitalEntity?,
        val caloriesIn: Float,
    )

    fun todaySnapshot(): Flow<TodaySnapshot> {
        val core = combine(
            settings,
            todaySteps(),
            waterToday(),
            latestSleep(),
            todayCalories(),
        ) { s, steps, water, sleep, kcal ->
            DayCore(s, steps, water, sleep, kcal)
        }
        return core.combine(latestWeight()) { c, weight ->
            TodaySnapshot(
                steps = c.steps?.steps ?: 0,
                stepGoal = c.settings.stepGoal,
                waterMl = c.waterMl,
                sleepHours = c.sleep?.value ?: 0f,
                caloriesIn = c.caloriesIn,
                caloriesOut = c.steps?.calories ?: 0,
                weightKg = weight?.value ?: c.settings.profile.weightKg,
            )
        }
    }

    /* ---------------- 设置写入口 ---------------- */

    suspend fun setTheme(id: String) = preferences.setThemeId(id)

    suspend fun setLayout(id: String) = preferences.setLayoutId(id)

    suspend fun setLanguage(id: String) = preferences.setLanguageId(id)

    suspend fun updateProfile(profile: UserProfile) = preferences.updateProfile(profile)

    suspend fun updateApi(api: ApiConfig) = preferences.updateApi(api)
}
