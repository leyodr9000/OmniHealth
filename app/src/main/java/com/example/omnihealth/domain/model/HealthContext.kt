package com.example.omnihealth.domain.model

/** 生成 AI 报告 / 离线建议所需的 7 日聚合数据。 */
data class HealthContext(
    val nickname: String = "Kai",
    val useChinese: Boolean = true,
    val days: Int = 7,
    val avgSteps: Int = 0,
    val stepGoal: Int = 10_000,
    val avgSleepHours: Float = 0f,
    val sleepGoalHours: Float = 8f,
    val avgCalories: Int = 0,
    val calorieGoal: Int = 2_000,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val waterMl: Float = 0f,
    val waterGoalMl: Int = 2_500,
    val weightKg: Float = 0f,
    val bmi: Float = 0f,
    val score: Int = 0,
    val habitsDone: Int = 0,
    val habitsTotal: Int = 0,
) {

    val hasAnyData: Boolean
        get() = avgSteps > 0 || avgSleepHours > 0f || avgCalories > 0

    /** 提供给 LLM 的结构化上下文。 */
    fun promptBlock(): String = if (useChinese) {
        """
        |用户：${nickname}
        |统计周期：最近 ${days} 天
        |日均步数：$avgSteps（目标 $stepGoal）
        |日均睡眠：${fmt1(avgSleepHours)} 小时（目标 ${fmt1(sleepGoalHours)}）
        |日均摄入：$avgCalories kcal（目标 $calorieGoal）
        |日均蛋白/碳水/脂肪：${proteinG} g / ${carbsG} g / ${fatG} g
        |今日饮水：${waterMl.toInt()} ml（目标 $waterGoalMl）
        |体重：${fmt1(weightKg)} kg，BMI ${fmt1(bmi)}
        |综合健康分：$score / 100
        |习惯打卡：$habitsDone / $habitsTotal
        """.trimMargin()
    } else {
        """
        |User: $nickname
        |Window: last $days days
        |Avg steps: $avgSteps (goal $stepGoal)
        |Avg sleep: ${fmt1(avgSleepHours)} h (goal ${fmt1(sleepGoalHours)})
        |Avg intake: $avgCalories kcal (goal $calorieGoal)
        |Avg protein/carbs/fat: ${proteinG} g / ${carbsG} g / ${fatG} g
        |Water today: ${waterMl.toInt()} ml (goal $waterGoalMl)
        |Weight: ${fmt1(weightKg)} kg, BMI ${fmt1(bmi)}
        |Health score: $score / 100
        |Habits: $habitsDone / $habitsTotal
        """.trimMargin()
    }

    private fun fmt1(value: Float): String = String.format(java.util.Locale.US, "%.1f", value)
}
