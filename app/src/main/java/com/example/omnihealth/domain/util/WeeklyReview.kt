package com.example.omnihealth.domain.util

/**
 * 周复盘的**确定性**生成器。
 *
 * 对应方案第六节：
 * - 只讲三件事：本周发生了什么、依据有多少、下周做一件事；
 * - 「未记录早餐」不等于「没吃早餐」，文案必须区分；
 * - 行动要能连接到真实功能，不是一个按钮都不给的文字；
 * - 不自动改目标、不创建已完成记录、不做惩罚式表达。
 */
object WeeklyReview {

    enum class ActionType {
        /** 打开记录页补录 */
        OPEN_RECORDS,

        /** 打开提醒设置 */
        OPEN_REMINDERS,

        /** 不需要动作 */
        NONE,
    }

    data class Action(
        val title: String,
        val type: ActionType,
    )

    data class Review(
        val summary: String,
        val evidence: String,
        val actions: List<Action>,
        /** 是否值得展示（完全没数据时不硬编一段复盘） */
        val hasData: Boolean,
    )

    /**
     * @param dietDays 近 7 天里「有饮食记录」的天数
     * @param breakfastDays 其中记录了早餐的天数
     * @param sleepDays 有睡眠记录的天数
     * @param stepDays 有步数记录的天数
     */
    fun build(
        dietDays: Int,
        breakfastDays: Int,
        sleepDays: Int,
        stepDays: Int,
    ): Review {
        val maxDays = 7
        if (dietDays == 0 && sleepDays == 0 && stepDays == 0) {
            return Review(
                summary = "本周还没有可用记录",
                evidence = "饮食 0 天 · 睡眠 0 天 · 步数 0 天",
                actions = listOf(Action("先记录今天的一餐", ActionType.OPEN_RECORDS)),
                hasData = false,
            )
        }

        val missingBreakfast = (dietDays - breakfastDays).coerceAtLeast(0)
        val summary = when {
            dietDays < 4 ->
                "本周有 $dietDays 天记录了饮食，记录本身还不够稳定"
            missingBreakfast >= 3 ->
                "本周有 $dietDays 天记录了饮食，其中 $missingBreakfast 天没有早餐记录"
            else ->
                "本周记录了 $dietDays 天饮食，节奏基本稳定"
        }

        val evidence = "饮食 $dietDays/$maxDays 天（含早餐 $breakfastDays 天）· " +
            "睡眠 $sleepDays/$maxDays 天 · 步数 $stepDays/$maxDays 天"

        val actions = buildList {
            if (dietDays < 4) {
                add(Action("固定一个记录时间，先把记录补起来", ActionType.OPEN_REMINDERS))
            } else if (missingBreakfast >= 3) {
                add(Action("看看缺早餐的那几天，是没记录还是没吃", ActionType.OPEN_RECORDS))
            }
            if (sleepDays == 0) {
                add(Action("补一条睡眠记录，趋势才有依据", ActionType.OPEN_RECORDS))
            }
            if (isEmpty()) {
                add(Action("保持当前节奏，暂不调整目标", ActionType.NONE))
            }
        }

        return Review(summary = summary, evidence = evidence, actions = actions, hasData = true)
    }
}
