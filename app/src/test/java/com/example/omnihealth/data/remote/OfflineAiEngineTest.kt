package com.example.omnihealth.data.remote

import com.example.omnihealth.domain.model.HealthContext
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineAiEngineTest {

    private val chinese = HealthContext(
        nickname = "Kai",
        useChinese = true,
        avgSteps = 6_400,
        stepGoal = 10_000,
        avgSleepHours = 7.1f,
        sleepGoalHours = 8f,
        avgCalories = 2_050,
        calorieGoal = 2_150,
        proteinG = 96,
        carbsG = 210,
        fatG = 62,
        waterMl = 1_800f,
        waterGoalMl = 2_500,
        weightKg = 62.4f,
        bmi = 19.7f,
        score = 74,
    )

    @Test
    fun weekly_report_contains_core_metrics_and_three_actions() {
        val report = OfflineAiEngine.weeklyReport(chinese)
        assertTrue(report.contains("健康分 74"))
        assertTrue(report.contains("6,400"))
        assertTrue(report.contains("下周 3 个动作"))
        assertTrue(report.contains("1. "))
        assertTrue(report.contains("3. "))
    }

    @Test
    fun sleep_question_returns_sleep_specific_answer() {
        val answer = OfflineAiEngine.answer("我最近睡眠不好怎么办", chinese)
        assertTrue(answer.contains("7.1"))
        assertTrue(answer.contains("就寝") || answer.contains("起床"))
    }

    @Test
    fun protein_question_uses_body_weight_range() {
        val answer = OfflineAiEngine.answer("蛋白质够吗", chinese)
        assertTrue(answer.contains("96"))
        assertTrue(answer.contains("g"))
    }

    @Test
    fun english_context_produces_english_report() {
        val english = chinese.copy(useChinese = false)
        val report = OfflineAiEngine.weeklyReport(english)
        assertTrue(report.contains("Health score"))
        assertTrue(report.contains("Three actions"))
    }

    @Test
    fun missing_sleep_data_is_called_out_instead_of_negative_number() {
        val noSleep = chinese.copy(avgSleepHours = 0f)
        val report = OfflineAiEngine.weeklyReport(noSleep)
        assertTrue(report.contains("还没有睡眠记录"))
    }
}
