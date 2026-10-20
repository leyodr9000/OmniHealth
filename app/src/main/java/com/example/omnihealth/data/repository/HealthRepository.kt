package com.example.omnihealth.data.repository

import com.example.omnihealth.data.datastore.ApiConfig
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.datastore.SettingsPreferences
import com.example.omnihealth.data.datastore.UserProfile
import com.example.omnihealth.data.local.AppDatabase
import com.example.omnihealth.data.local.entity.AiReportEntity
import com.example.omnihealth.data.local.entity.AiReportKind
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.HabitEntity
import com.example.omnihealth.data.local.entity.HabitLogEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.VitalEntity
import com.example.omnihealth.data.local.entity.VitalType
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.data.remote.AiAdvisor
import com.example.omnihealth.data.remote.AiAnswer
import com.example.omnihealth.data.remote.ChatMessage
import com.example.omnihealth.domain.model.HealthContext
import com.example.omnihealth.domain.util.HealthCalculator
import com.example.omnihealth.domain.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

    fun sleepRange(days: Int = 7): Flow<List<VitalEntity>> {
        val end = TimeUtils.epochDayNow()
        val from = TimeUtils.startOfDayMillis(end - days + 1)
        val to = TimeUtils.endOfDayMillis(end)
        return flow { emit(db.vitalDao().between(VitalType.SLEEP, from, to)) }
    }

    fun waterToday(): Flow<Float> {
        val day = TimeUtils.epochDayNow()
        return db.vitalDao().observeSumBetween(
            VitalType.WATER,
            TimeUtils.startOfDayMillis(day),
            TimeUtils.endOfDayMillis(day),
        ).map { it ?: 0f }
    }

    suspend fun logWeight(kg: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.WEIGHT, value = kg, unit = "kg", tzOffsetMinutes = tzOffset()),
    )

    suspend fun logWater(ml: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.WATER, value = ml, unit = "ml", tzOffsetMinutes = tzOffset()),
    )

    suspend fun logSleep(hours: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.SLEEP, value = hours, unit = "h", tzOffsetMinutes = tzOffset()),
    )

    suspend fun logMood(score: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.MOOD, value = score, unit = "pt", tzOffsetMinutes = tzOffset()),
    )

    suspend fun logHeartRate(bpm: Float) = db.vitalDao().insert(
        VitalEntity(type = VitalType.HEART_RATE, value = bpm, unit = "bpm", tzOffsetMinutes = tzOffset()),
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

    /** 指定日期的饮食记录（历史补录/查看）。 */
    fun dietLogsForDay(epochDay: Long): Flow<List<DietLogEntity>> = db.dietDao().observeLogs(epochDay)

    fun todayCalories(): Flow<Float> =
        db.dietDao().observeCalories(TimeUtils.epochDayNow()).map { it ?: 0f }

    fun dietLogsBetween(days: Int = 7): Flow<List<DietLogEntity>> {
        val end = TimeUtils.epochDayNow()
        return db.dietDao().observeLogsBetween(end - days + 1, end)
    }

    suspend fun searchFoods(query: String): List<FoodEntity> =
        if (query.isBlank()) emptyList() else db.dietDao().searchFoods(query.trim())

    fun allFoods(): Flow<List<FoodEntity>> = db.dietDao().observeFoods()

    suspend fun logFood(
        food: FoodEntity,
        grams: Float,
        mealType: String = MealType.LUNCH,
        epochDay: Long = TimeUtils.epochDayNow(),
    ) {
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
                epochDay = epochDay,
                tzOffsetMinutes = tzOffset(),
            ),
        )
    }

    suspend fun deleteDietLog(log: DietLogEntity) = db.dietDao().deleteLog(log)

    suspend fun restoreDietLog(log: DietLogEntity) = db.dietDao().restoreLog(log)

    /** 修改克数或餐次：营养值按比例重算，保留原时间与日期。 */
    suspend fun updateDietLog(log: DietLogEntity, grams: Float, mealType: String) {
        val ratio = if (log.grams > 0f) grams / log.grams else 1f
        db.dietDao().updateLog(
            log.copy(
                grams = grams,
                mealType = mealType,
                calories = log.calories * ratio,
                protein = log.protein * ratio,
                carbs = log.carbs * ratio,
                fat = log.fat * ratio,
            ),
        )
    }

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

    /* ---------------- AI 顾问 ---------------- */

    private val advisor = AiAdvisor()

    fun aiReports(): Flow<List<AiReportEntity>> = db.aiReportDao().observeRecent()

    suspend fun saveAiReport(report: AiReportEntity) = db.aiReportDao().insert(report)

    suspend fun latestWeeklyReport(): AiReportEntity? = db.aiReportDao().latest("WEEKLY")

    /** 聚合近 7 天数据，供 AI 报告与提示使用。 */
    suspend fun healthContext(useChinese: Boolean): HealthContext {
        val settings = preferences.settings.first()
        val profile = settings.profile
        val end = TimeUtils.epochDayNow()
        val from = end - 6
        val dayCount = 7f

        val stepRows = db.stepDao().range(from, end)
        val dietRows = db.dietDao().logsBetween(from, end)
        val sleepRows = db.vitalDao().between(
            VitalType.SLEEP,
            TimeUtils.startOfDayMillis(from),
            TimeUtils.endOfDayMillis(end),
        )
        val weight = db.vitalDao().latest(VitalType.WEIGHT)?.value ?: profile.weightKg
        val water = db.vitalDao().sumBetween(
            VitalType.WATER,
            TimeUtils.startOfDayMillis(end),
            TimeUtils.endOfDayMillis(end),
        ) ?: 0f

        val habits = db.habitDao().allHabits()
        val habitLogs = db.habitDao().logsBetween(from, end)

        val avgSteps = (stepRows.sumOf { it.steps } / dayCount).toInt()
        val avgCalories = (dietRows.sumOf { it.calories.toDouble() } / dayCount).toInt()
        val avgProtein = (dietRows.sumOf { it.protein.toDouble() } / dayCount).toInt()
        val avgCarbs = (dietRows.sumOf { it.carbs.toDouble() } / dayCount).toInt()
        val avgFat = (dietRows.sumOf { it.fat.toDouble() } / dayCount).toInt()
        val avgSleep = (sleepRows.sumOf { it.value.toDouble() } / dayCount).toFloat()

        val isMale = profile.gender != Gender.FEMALE
        val bmr = HealthCalculator.bmr(weight, profile.heightCm, profile.age, isMale)
        val tdee = HealthCalculator.tdee(bmr, profile.activityLevel.factor)
        val calorieGoal = HealthCalculator.calorieTarget(tdee, profile.goal.calorieDelta)
        val bmi = HealthCalculator.bmi(weight, profile.heightCm)

        val score = HealthCalculator.fiveDimensionScore(
            caloriesIn = avgCalories.toFloat(),
            calorieTarget = calorieGoal,
            steps = avgSteps,
            stepGoal = settings.stepGoal,
            sleepHours = avgSleep,
            sleepGoalHours = settings.sleepGoalMin / 60f,
            waterMl = water,
            waterGoalMl = settings.waterGoalMl.toFloat(),
            habitChecks = habitLogs.map { it.habitId }.distinct().size,
            habitTarget = habits.size.coerceAtLeast(1),
            sleepRecorded = avgSleep > 0f,
            waterRecorded = water > 0f,
            nutritionRecorded = avgCalories > 0,
            habitsRecorded = habits.isNotEmpty(),
        )

        return HealthContext(
            nickname = profile.nickname,
            useChinese = useChinese,
            days = 7,
            avgSteps = avgSteps,
            stepGoal = settings.stepGoal,
            avgSleepHours = avgSleep,
            sleepGoalHours = settings.sleepGoalMin / 60f,
            avgCalories = avgCalories,
            calorieGoal = calorieGoal,
            proteinG = avgProtein,
            carbsG = avgCarbs,
            fatG = avgFat,
            waterMl = water,
            waterGoalMl = settings.waterGoalMl,
            weightKg = weight,
            bmi = bmi,
            score = score.overall,
            habitsDone = habitLogs.size,
            habitsTotal = habits.size,
        )
    }

    suspend fun generateWeeklyReport(useChinese: Boolean): AiAnswer {
        val settings = preferences.settings.first()
        val context = healthContext(useChinese)
        val answer = advisor.report(context, settings.api)
        db.aiReportDao().insert(
            AiReportEntity(
                kind = AiReportKind.WEEKLY,
                prompt = context.promptBlock(),
                content = answer.text,
                score = context.score,
            ),
        )
        return answer
    }

    suspend fun askAdvisor(
        question: String,
        useChinese: Boolean,
        history: List<ChatMessage> = emptyList(),
    ): AiAnswer {
        val settings = preferences.settings.first()
        return advisor.chat(question, healthContext(useChinese), settings.api, history)
    }

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

    suspend fun setFramework(id: String) = preferences.setFrameworkId(id)

    suspend fun setDarkMode(id: String) = preferences.setDarkMode(id)

    suspend fun acceptAiDisclosure() = preferences.acceptAiDisclosure()

    suspend fun setReduceMotion(enabled: Boolean) = preferences.setReduceMotion(enabled)

    suspend fun setWaterReminder(enabled: Boolean) = preferences.setWaterReminder(enabled)

    suspend fun setWaterWindow(startHour: Int, endHour: Int, intervalHours: Int) =
        preferences.setWaterWindow(startHour, endHour, intervalHours)

    suspend fun setSleepReminder(enabled: Boolean) = preferences.setSleepReminder(enabled)

    suspend fun setSleepTime(hour: Int, minute: Int) = preferences.setSleepTime(hour, minute)

    /** 导出全部记录为 CSV，返回文件路径。 */
    suspend fun exportCsv(context: android.content.Context): java.io.File? =
        com.example.omnihealth.data.export.CsvExporter.export(context, db)


    suspend fun updateProfile(profile: UserProfile) = preferences.updateProfile(profile)

    suspend fun updateApi(api: ApiConfig) = preferences.updateApi(api)

    suspend fun updateGoals(steps: Int, waterMl: Int, sleepMin: Int, calories: Int) =
        preferences.updateGoals(steps, waterMl, sleepMin, calories)

    /* ---------------- 习惯维护 ---------------- */

    suspend fun addHabit(name: String, emoji: String, targetPerDay: Int = 1) {
        db.habitDao().insertHabit(
            HabitEntity(
                name = name,
                emoji = emoji,
                targetPerDay = targetPerDay,
            ),
        )
    }

    suspend fun deleteHabit(habit: HabitEntity) = db.habitDao().deleteHabit(habit)

    /* ---------------- backup / restore ---------------- */

    suspend fun exportBackup(context: android.content.Context): java.io.File? =
        com.example.omnihealth.data.backup.BackupManager.export(context, db)

    suspend fun inspectBackup(
        context: android.content.Context,
        uri: android.net.Uri,
    ): com.example.omnihealth.data.backup.BackupBundle? =
        com.example.omnihealth.data.backup.BackupManager.inspect(context, uri)

    suspend fun restoreBackup(
        context: android.content.Context,
        uri: android.net.Uri,
    ): com.example.omnihealth.data.backup.RestoreResult =
        com.example.omnihealth.data.backup.BackupManager.restore(context, db, uri)

/** 当前时区偏移（分钟），随记录一起保存。 */
private fun tzOffset(): Int =
    java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60_000
}
