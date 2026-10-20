package com.example.omnihealth.domain.util

/**
 * 周复盘的**确定性**生成器。
 *
 * 产出结构化结论（枚举 + 数字），文案由 UI 组装；
 * 规则：只说发生了什么、有多少依据、下一步做什么；未记录不等于没吃；不自动改目标。
 */
object WeeklyReview {

    enum class Summary { NO_DATA, FEW_DAYS, MISSING_BREAKFAST, STABLE }

    enum class ActionKind {
        /** 固定记录习惯 */
        FIX_LOGGING,

        /** 核对缺早餐的那几天 */
        CHECK_BREAKFAST,

        /** 补睡眠记录 */
        LOG_SLEEP,

        /** 保持现状 */
        NO_CHANGE,
    }

    enum class ActionType { OPEN_RECORDS, OPEN_REMINDERS, NONE }

    data class Action(val kind: ActionKind, val type: ActionType)

    data class Review(
        val summary: Summary,
        val dietDays: Int,
        val breakfastDays: Int,
        val sleepDays: Int,
        val stepDays: Int,
        val missingBreakfast: Int,
        val actions: List<Action>,
    ) {
        val hasData: Boolean get() = dietDays > 0 || sleepDays > 0 || stepDays > 0
    }

    fun build(dietDays: Int, breakfastDays: Int, sleepDays: Int, stepDays: Int): Review {
        val missingBreakfast = (dietDays - breakfastDays).coerceAtLeast(0)
        val hasData = dietDays > 0 || sleepDays > 0 || stepDays > 0

        val summary = when {
            !hasData -> Summary.NO_DATA
            dietDays < 4 -> Summary.FEW_DAYS
            missingBreakfast >= 3 -> Summary.MISSING_BREAKFAST
            else -> Summary.STABLE
        }

        val actions = buildList {
            when {
                !hasData -> add(Action(ActionKind.FIX_LOGGING, ActionType.OPEN_RECORDS))
                dietDays < 4 -> add(Action(ActionKind.FIX_LOGGING, ActionType.OPEN_REMINDERS))
                missingBreakfast >= 3 -> add(Action(ActionKind.CHECK_BREAKFAST, ActionType.OPEN_RECORDS))
            }
            if (hasData && sleepDays == 0) add(Action(ActionKind.LOG_SLEEP, ActionType.OPEN_RECORDS))
            if (isEmpty()) add(Action(ActionKind.NO_CHANGE, ActionType.NONE))
        }

        return Review(
            summary = summary,
            dietDays = dietDays,
            breakfastDays = breakfastDays,
            sleepDays = sleepDays,
            stepDays = stepDays,
            missingBreakfast = missingBreakfast,
            actions = actions,
        )
    }
}