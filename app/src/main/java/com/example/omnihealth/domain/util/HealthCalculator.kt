package com.example.omnihealth.domain.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 纯函数健康计算引擎：BMI / BMR / TDEE / 宏量分配 / 5D 健康评分。
 *
 * 不依赖 Android，方便单元测试。
 */
object HealthCalculator {

    /* ------------------------------------------------------------------ */
    /* BMI                                                                */
    /* ------------------------------------------------------------------ */

    enum class BmiCategory { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE }

    fun bmi(weightKg: Float, heightCm: Float): Float {
        if (heightCm <= 0f) return 0f
        val m = heightCm / 100f
        return weightKg / (m * m)
    }

    /** 中国成人标准：<18.5 偏瘦 / 18.5–23.9 正常 / 24–27.9 超重 / ≥28 肥胖。 */
    fun bmiCategory(bmi: Float): BmiCategory = when {
        bmi < 18.5f -> BmiCategory.UNDERWEIGHT
        bmi < 24f -> BmiCategory.NORMAL
        bmi < 28f -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }

    /** Deurenberg 体脂估算。 */
    fun bodyFatEstimate(bmi: Float, age: Int, isMale: Boolean): Float {
        val sex = if (isMale) 1f else 0f
        return (1.20f * bmi + 0.23f * age - 10.8f * sex - 5.4f).coerceIn(3f, 60f)
    }

    /** 健康体重区间（BMI 18.5–23.9）。 */
    fun healthyWeightRange(heightCm: Float): ClosedFloatingPointRange<Float> {
        val m = heightCm / 100f
        return (18.5f * m * m)..(23.9f * m * m)
    }

    /* ------------------------------------------------------------------ */
    /* BMR / TDEE                                                         */
    /* ------------------------------------------------------------------ */

    /**
     * Mifflin-St Jeor：
     *  男 = 10·kg + 6.25·cm − 5·age + 5
     *  女 = 10·kg + 6.25·cm − 5·age − 161
     */
    fun bmr(weightKg: Float, heightCm: Float, age: Int, isMale: Boolean): Float {
        val base = 10f * weightKg + 6.25f * heightCm - 5f * age
        return if (isMale) base + 5f else base - 161f
    }

    fun tdee(bmr: Float, activityFactor: Float): Float = bmr * activityFactor

    /** 目标热量 = TDEE + 目标偏移（减脂 −400 / 维持 0 / 增肌 +300）。 */
    fun calorieTarget(tdee: Float, goalDelta: Int): Int =
        (tdee + goalDelta).coerceAtLeast(1_200f).roundToInt()

    /* ------------------------------------------------------------------ */
    /* 宏量营养                                                            */
    /* ------------------------------------------------------------------ */

    data class MacroTargets(
        val proteinG: Int,
        val carbsG: Int,
        val fatG: Int,
        val proteinKcal: Int,
        val carbsKcal: Int,
        val fatKcal: Int,
    )

    /**
     * 蛋白质按体重给（增肌 2.0 g/kg，维持 1.6，减脂 1.8），
     * 脂肪占总热量 27%，剩余给碳水。
     */
    fun macroTargets(
        weightKg: Float,
        calorieTarget: Int,
        goalDelta: Int,
    ): MacroTargets {
        val proteinPerKg = when {
            goalDelta > 0 -> 2.0f
            goalDelta < 0 -> 1.8f
            else -> 1.6f
        }
        val proteinG = (weightKg * proteinPerKg).roundToInt()
        val proteinKcal = proteinG * 4
        val fatKcal = (calorieTarget * 0.27f).roundToInt()
        val fatG = (fatKcal / 9f).roundToInt()
        val carbsKcal = (calorieTarget - proteinKcal - fatKcal).coerceAtLeast(0)
        val carbsG = (carbsKcal / 4f).roundToInt()
        return MacroTargets(proteinG, carbsG, fatG, proteinKcal, carbsKcal, fatKcal)
    }

    /* ------------------------------------------------------------------ */
    /* 5D 健康评分                                                         */
    /* ------------------------------------------------------------------ */

    data class ScoreBreakdown(
        val nutrition: Int,
        val activity: Int,
        val sleep: Int,
        val hydration: Int,
        val consistency: Int,
    ) {
        val overall: Int
            get() = (nutrition * 0.28f + activity * 0.26f + sleep * 0.20f +
                hydration * 0.14f + consistency * 0.12f).roundToInt().coerceIn(0, 100)
    }

    fun fiveDimensionScore(
        caloriesIn: Float,
        calorieTarget: Int,
        steps: Int,
        stepGoal: Int,
        sleepHours: Float,
        sleepGoalHours: Float,
        waterMl: Float,
        waterGoalMl: Float,
        habitChecks: Int,
        habitTarget: Int,
    ): ScoreBreakdown = ScoreBreakdown(
        nutrition = nutritionScore(caloriesIn, calorieTarget),
        activity = ratioScore(steps.toFloat(), stepGoal.toFloat()),
        sleep = sleepScore(sleepHours, sleepGoalHours),
        hydration = ratioScore(waterMl, waterGoalMl.toFloat()),
        consistency = ratioScore(habitChecks.toFloat(), habitTarget.toFloat()),
    )

    /** 摄入越接近目标越高；偏差 50% 以上记 0 分。 */
    private fun nutritionScore(caloriesIn: Float, target: Int): Int {
        if (target <= 0) return 0
        if (caloriesIn <= 0f) return 0
        val deviation = abs(caloriesIn - target) / target
        return (100f - deviation * 200f).coerceIn(0f, 100f).roundToInt()
    }

    private fun ratioScore(value: Float, goal: Float): Int {
        if (goal <= 0f) return 0
        return (value / goal * 100f).coerceIn(0f, 110f).roundToInt().coerceAtMost(100)
    }

    /** 以 7.5h 为最优，每偏离 1 小时扣 18 分。 */
    private fun sleepScore(hours: Float, goalHours: Float): Int {
        if (hours <= 0f) return 0
        val ideal = if (goalHours > 0f) goalHours else 7.5f
        return (100f - abs(hours - ideal) * 18f).coerceIn(0f, 100f).roundToInt()
    }

    /* ------------------------------------------------------------------ */
    /* 运动换算                                                            */
    /* ------------------------------------------------------------------ */

    fun distanceMeters(steps: Int, heightCm: Float): Float {
        val stride = (heightCm * 0.415f) / 100f
        return steps * stride
    }

    fun stepsToCalories(steps: Int, weightKg: Float): Int =
        (steps * weightKg * 0.00057f).roundToInt()

    fun activeMinutes(steps: Int): Int = (steps / 110f).roundToInt()

    /** 每周体重变化预估（7700 kcal ≈ 1 kg 脂肪）。 */
    fun weeklyWeightDeltaKg(dailyDeficit: Int): Float = dailyDeficit * 7f / 7_700f

    fun clampProgress(value: Float): Float = min(1f, max(0f, value))
}
