package com.example.omnihealth.data.remote

import com.example.omnihealth.domain.model.HealthContext
import java.util.Locale

/** AI 回答结果；[offline] 表示走了本地引擎。 */
data class AiAnswer(
    val text: String,
    val offline: Boolean,
    val error: String? = null,
)

/**
 * 离线智能兜底引擎。
 *
 * 没有 API Key、断网、或请求失败时使用：基于规则 + 真实数据拼出可执行的建议，
 * 保证应用 100% 开箱可用。
 */
object OfflineAiEngine {

    private fun n(value: Int): String = String.format(Locale.US, "%,d", value)
    private fun f1(value: Float): String = String.format(Locale.US, "%.1f", value)

    fun weeklyReport(ctx: HealthContext): String {
        val stepRate = ratio(ctx.avgSteps.toFloat(), ctx.stepGoal.toFloat())
        val kcalDiff = ctx.avgCalories - ctx.calorieGoal
        val sleepDiff = ctx.avgSleepHours - ctx.sleepGoalHours
        val waterRate = ratio(ctx.waterMl, ctx.waterGoalMl.toFloat())

        return if (ctx.useChinese) {
            buildString {
                appendLine("📊 近 ${ctx.days} 天概览")
                appendLine("健康分 ${ctx.score} / 100")
                appendLine("步数 ${n(ctx.avgSteps)} 步/天（目标 ${n(ctx.stepGoal)}，达成 ${(stepRate * 100).toInt()}%）")
                appendLine("睡眠 ${f1(ctx.avgSleepHours)} h/天（目标 ${f1(ctx.sleepGoalHours)} h）")
                appendLine("摄入 ${n(ctx.avgCalories)} kcal/天（目标 ${n(ctx.calorieGoal)}）")
                appendLine("蛋白 ${ctx.proteinG} g · 碳水 ${ctx.carbsG} g · 脂肪 ${ctx.fatG} g")
                appendLine("饮水 ${ctx.waterMl.toInt()} ml（目标 ${ctx.waterGoalMl}，达成 ${(waterRate * 100).toInt()}%）")
                appendLine("体重 ${f1(ctx.weightKg)} kg · BMI ${f1(ctx.bmi)}")
                appendLine()
                appendLine("✅ 做得好的")
                if (stepRate >= 0.8f) appendLine("· 步数稳定达标，活动量很不错。")
                if (sleepDiff >= -0.5f) appendLine("· 睡眠时长接近目标，恢复节奏没乱。")
                if (waterRate >= 0.8f) appendLine("· 饮水习惯保持得很好。")
                if (stepRate < 0.8f && sleepDiff < -0.5f && waterRate < 0.8f) {
                    appendLine("· 数据记录本身坚持下来了，这是改善的前提。")
                }
                appendLine()
                appendLine("🎯 下周 3 个动作")
                appendLine("1. ${suggestionSteps(ctx, stepRate)}")
                appendLine("2. ${suggestionSleep(ctx, sleepDiff)}")
                appendLine("3. ${suggestionNutrition(ctx, kcalDiff)}")
            }.trim()
        } else {
            buildString {
                appendLine("📊 Last ${ctx.days} days")
                appendLine("Health score ${ctx.score} / 100")
                appendLine("Steps ${n(ctx.avgSteps)}/day (goal ${n(ctx.stepGoal)}, ${(stepRate * 100).toInt()}%)")
                appendLine("Sleep ${f1(ctx.avgSleepHours)} h/day (goal ${f1(ctx.sleepGoalHours)} h)")
                appendLine("Intake ${n(ctx.avgCalories)} kcal/day (goal ${n(ctx.calorieGoal)})")
                appendLine("Protein ${ctx.proteinG} g · Carbs ${ctx.carbsG} g · Fat ${ctx.fatG} g")
                appendLine("Water ${ctx.waterMl.toInt()} ml (goal ${ctx.waterGoalMl}, ${(waterRate * 100).toInt()}%)")
                appendLine("Weight ${f1(ctx.weightKg)} kg · BMI ${f1(ctx.bmi)}")
                appendLine()
                appendLine("✅ What went well")
                if (stepRate >= 0.8f) appendLine("· Your step count is consistently on target.")
                if (sleepDiff >= -0.5f) appendLine("· Sleep duration is close to goal.")
                if (waterRate >= 0.8f) appendLine("· Hydration habit looks solid.")
                if (stepRate < 0.8f && sleepDiff < -0.5f && waterRate < 0.8f) {
                    appendLine("· You kept logging — that is the foundation.")
                }
                appendLine()
                appendLine("🎯 Three actions for next week")
                appendLine("1. ${suggestionSteps(ctx, stepRate)}")
                appendLine("2. ${suggestionSleep(ctx, sleepDiff)}")
                appendLine("3. ${suggestionNutrition(ctx, kcalDiff)}")
            }.trim()
        }
    }

    fun answer(question: String, ctx: HealthContext): String {
        val q = question.lowercase(Locale.getDefault())
        val zh = ctx.useChinese

        fun has(vararg keys: String) = keys.any { q.contains(it) }

        return when {
            has("睡", "失眠", "困", "sleep", "tired", "insomnia") -> if (zh) {
                "你最近平均睡 ${f1(ctx.avgSleepHours)} 小时，目标是 ${f1(ctx.sleepGoalHours)} 小时。\n\n" +
                    "建议：\n1. 固定起床时间，比入睡时间更容易锚定生物钟；\n" +
                    "2. 睡前 40 分钟把屏幕调暗并离开手机；\n" +
                    "3. 下午 2 点后别碰咖啡因，晚餐与睡眠间隔 ≥3 小时。"
            } else {
                "You average ${f1(ctx.avgSleepHours)} h against a ${f1(ctx.sleepGoalHours)} h goal.\n\n" +
                    "Try:\n1. Anchor a fixed wake-up time;\n2. Dim screens 40 min before bed;\n" +
                    "3. No caffeine after 2 pm, and keep dinner ≥3 h before sleep."
            }

            has("蛋白", "protein", "增肌", "muscle") -> if (zh) {
                "今日蛋白约 ${ctx.proteinG} g。按体重 ${f1(ctx.weightKg)} kg 估算，建议每天 " +
                    "${(ctx.weightKg * 1.6f).toInt()}–${(ctx.weightKg * 2.0f).toInt()} g。\n\n" +
                    "做法：每餐加一份 20–30 g 蛋白（鸡胸 120 g / 希腊酸奶 200 g / 乳清 1 勺），把蛋白质分配到三餐比集中一餐更好。"
            } else {
                "You logged about ${ctx.proteinG} g protein. For ${f1(ctx.weightKg)} kg, aim for " +
                    "${(ctx.weightKg * 1.6f).toInt()}–${(ctx.weightKg * 2.0f).toInt()} g/day.\n\n" +
                    "Add 20–30 g per meal (120 g chicken, 200 g Greek yogurt, or one scoop of whey) and spread it across the day."
            }

            has("步", "走", "运动", "step", "walk", "cardio") -> if (zh) {
                "你日均 ${n(ctx.avgSteps)} 步，目标 ${n(ctx.stepGoal)}。\n\n" +
                    "把目标拆成三段：早 2,000、午 3,000、晚 5,000。饭后 15 分钟散步对血糖和困倦的改善最明显。"
            } else {
                "You average ${n(ctx.avgSteps)} steps against ${n(ctx.stepGoal)}.\n\n" +
                    "Split it: 2k morning, 3k midday, 5k evening. A 15-minute post-meal walk helps glucose and afternoon sleepiness most."
            }

            has("体重", "胖", "瘦", "weight", "fat", "bmi") -> if (zh) {
                "当前 ${f1(ctx.weightKg)} kg，BMI ${f1(ctx.bmi)}（中国标准 18.5–23.9 为正常）。\n\n" +
                    "体重变化看周均值比看单日更可靠；每天固定同一时间、同一状态称重即可。"
            } else {
                "You are ${f1(ctx.weightKg)} kg with BMI ${f1(ctx.bmi)} (normal 18.5–23.9).\n\n" +
                    "Judge progress by weekly averages, not single days; weigh yourself at the same time each day."
            }

            has("水", "喝", "water", "hydrat") -> if (zh) {
                "今日饮水 ${ctx.waterMl.toInt()} ml，目标 ${ctx.waterGoalMl} ml。\n\n" +
                    "把水杯放在视线内，每餐前 300 ml，训练前后各补 200–300 ml，达标会容易很多。"
            } else {
                "Water today is ${ctx.waterMl.toInt()} ml against ${ctx.waterGoalMl} ml.\n\n" +
                    "Keep a bottle in sight, drink 300 ml before each meal, and 200–300 ml around training."
            }

            has("热量", "吃", "卡", "calorie", "diet", "eat") -> if (zh) {
                "日均摄入 ${n(ctx.avgCalories)} kcal，目标 ${n(ctx.calorieGoal)} kcal。\n\n" +
                    "先确认蛋白质和蔬菜吃够，再调整主食和油脂；记录时用「熟重」估算会更准。"
            } else {
                "Intake averages ${n(ctx.avgCalories)} kcal against a ${n(ctx.calorieGoal)} kcal goal.\n\n" +
                    "Get protein and vegetables right first, then tune carbs and fats. Log cooked weights for better accuracy."
            }

            else -> weeklyReport(ctx)
        }
    }

    private fun ratio(value: Float, goal: Float): Float =
        if (goal <= 0f) 0f else (value / goal).coerceIn(0f, 2f)

    private fun suggestionSteps(ctx: HealthContext, rate: Float): String = when {
        rate >= 1f -> if (ctx.useChinese) {
            "步数已达标，尝试把其中 2,000 步改成快走（心率到 110–130），提升心肺。"
        } else {
            "Steps already on target — turn 2,000 of them into brisk walking (HR 110–130) for cardio."
        }
        rate >= 0.7f -> if (ctx.useChinese) {
            "每天补 ${n(((ctx.stepGoal - ctx.avgSteps).coerceAtLeast(500)))} 步，晚饭后走 15 分钟即可。"
        } else {
            "Add ${n((ctx.stepGoal - ctx.avgSteps).coerceAtLeast(500))} steps/day — a 15-minute post-dinner walk does it."
        }
        else -> if (ctx.useChinese) {
            "先把目标降到 ${n((ctx.stepGoal * 0.7f).toInt())} 步，连续 5 天达成后再上调，避免一次跳太狠。"
        } else {
            "Lower the goal to ${n((ctx.stepGoal * 0.7f).toInt())} for 5 straight days, then ramp up."
        }
    }

    private fun suggestionSleep(ctx: HealthContext, diff: Float): String = when {
        ctx.avgSleepHours <= 0f -> if (ctx.useChinese) {
            "还没有睡眠记录，先用快捷记录补 3 天，才能判断你是时长不够还是节律乱了。"
        } else {
            "No sleep data yet — log three nights so we can tell duration from rhythm."
        }
        diff >= -0.3f -> if (ctx.useChinese) {
            "睡眠稳定，保持固定起床时间；周末补觉不超过 1 小时。"
        } else {
            "Sleep is stable — keep a fixed wake-up time and cap weekend catch-up at 1 hour."
        }
        else -> if (ctx.useChinese) {
            "本周平均少睡 ${f1(-diff)} 小时，把就寝时间提前 30 分钟，连续 4 天先做到。"
        } else {
            "You are ${f1(-diff)} h short on average — move bedtime 30 minutes earlier for four days."
        }
    }

    private fun suggestionNutrition(ctx: HealthContext, diff: Int): String = when {
        kotlin.math.abs(diff) <= 150 -> if (ctx.useChinese) {
            "热量控制得不错，把蛋白质提到 ${(ctx.weightKg * 1.8f).toInt()} g/天，训练后 1 小时内补一份。"
        } else {
            "Calories are on point — raise protein to ${(ctx.weightKg * 1.8f).toInt()} g/day, one serving within an hour of training."
        }
        diff > 150 -> if (ctx.useChinese) {
            "日均超目标 $diff kcal，先把含糖饮料与夜宵去掉，一周就能拉回 1,000+ kcal。"
        } else {
            "You are $diff kcal over target — cut sugary drinks and late snacks first; that alone recovers 1,000+ kcal a week."
        }
        else -> if (ctx.useChinese) {
            "日均低于目标 ${-diff} kcal，容易掉肌肉；加一份主食 + 一份蛋白（如米饭 100 g + 鸡蛋 2 个）。"
        } else {
            "You are ${-diff} kcal under target — add a carb and a protein serving (100 g rice + 2 eggs)."
        }
    }
}
