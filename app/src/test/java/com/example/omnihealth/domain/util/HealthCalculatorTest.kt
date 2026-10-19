package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCalculatorTest {

    @Test
    fun bmi_is_computed_from_height_and_weight() {
        // 62.4 kg / 1.78^2 = 19.69
        val value = HealthCalculator.bmi(62.4f, 178f)
        assertEquals(19.7f, value, 0.1f)
        assertEquals(HealthCalculator.BmiCategory.NORMAL, HealthCalculator.bmiCategory(value))
    }

    @Test
    fun bmi_category_thresholds_follow_chinese_standard() {
        assertEquals(HealthCalculator.BmiCategory.UNDERWEIGHT, HealthCalculator.bmiCategory(18.4f))
        assertEquals(HealthCalculator.BmiCategory.NORMAL, HealthCalculator.bmiCategory(18.5f))
        assertEquals(HealthCalculator.BmiCategory.OVERWEIGHT, HealthCalculator.bmiCategory(24f))
        assertEquals(HealthCalculator.BmiCategory.OBESE, HealthCalculator.bmiCategory(28f))
    }

    @Test
    fun bmr_uses_mifflin_st_jeor() {
        // 鐢? 10*70 + 6.25*175 - 5*30 + 5 = 700 + 1093.75 - 150 + 5
        assertEquals(1648.75f, HealthCalculator.bmr(70f, 175f, 30, true), 0.01f)
        // 濂? 鍚屽弬鏁?-161
        assertEquals(1482.75f, HealthCalculator.bmr(70f, 175f, 30, false), 0.01f)
    }

    @Test
    fun tdee_scales_with_activity_factor() {
        assertEquals(2555.56f, HealthCalculator.tdee(1648.75f, 1.55f), 0.05f)
    }

    @Test
    fun calorie_target_applies_goal_delta_and_floor() {
        assertEquals(2_156, HealthCalculator.calorieTarget(2_555.56f, -400))
        // 缁撴灉浣庝簬 1200 kcal 鏃舵姮鍒板畨鍏ㄤ笅闄?        assertEquals(1_200, HealthCalculator.calorieTarget(900f, -400))
    }

    @Test
    fun macros_are_consistent_with_calorie_target() {
        val target = 2_400
        val macros = HealthCalculator.macroTargets(70f, target, 0)
        assertEquals(112, macros.proteinG) // 1.6 g/kg
        val total = macros.proteinKcal + macros.carbsKcal + macros.fatKcal
        assertTrue("macro kcal $total should be near $target", kotlin.math.abs(total - target) <= 5)
    }

    @Test
    fun perfect_day_scores_high() {
        val score = HealthCalculator.fiveDimensionScore(
            caloriesIn = 2_000f,
            calorieTarget = 2_000,
            steps = 10_000,
            stepGoal = 10_000,
            sleepHours = 7.5f,
            sleepGoalHours = 7.5f,
            waterMl = 2_500f,
            waterGoalMl = 2_500f,
            habitChecks = 4,
            habitTarget = 4,
        )
        assertEquals(100, score.nutrition)
        assertEquals(100, score.activity)
        assertEquals(100, score.sleep)
        assertEquals(100, score.hydration)
        assertEquals(100, score.consistency)
        assertEquals(100, score.overall)
    }

    @Test
    fun empty_day_scores_zero() {
        val score = HealthCalculator.fiveDimensionScore(
            caloriesIn = 0f,
            calorieTarget = 2_000,
            steps = 0,
            stepGoal = 10_000,
            sleepHours = 0f,
            sleepGoalHours = 7.5f,
            waterMl = 0f,
            waterGoalMl = 2_500f,
            habitChecks = 0,
            habitTarget = 4,
        )
        assertEquals(0, score.overall)
    }

    @Test
    fun steps_convert_to_distance_and_calories() {
        val distance = HealthCalculator.distanceMeters(10_000, 178f)
        assertEquals(7_385f, distance, 20f)
        assertTrue(HealthCalculator.stepsToCalories(10_000, 62.4f) in 350..400)
    }
}
