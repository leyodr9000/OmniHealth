package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendInterpreterTest {

    @Test
    fun compares_two_full_weeks() {
        val series = List(7) { 70f } + List(7) { 69f }
        val insight = TrendInterpreter.compareWeeks(series, "kg", betterWhenLower = true, label = "体重")
        assertTrue(insight.reliable)
        assertTrue(insight.conclusion.contains("下降"))
        assertTrue(insight.conclusion.contains("方向符合目标"))
        assertTrue(insight.evidence.contains("7 天记录"))
    }

    @Test
    fun missing_days_are_not_treated_as_zero() {
        // 最近一周只有 5 天有记录，中间缺口用 null 表示
        val recent: List<Float?> = listOf(70f, null, 69f, null, 69f, 68f, 69f)
        val previous: List<Float?> = List(7) { 70f }
        val insight = TrendInterpreter.compareWeeks(previous + recent, "kg", true, "体重")
        assertTrue(insight.reliable)
        assertTrue(insight.evidence.contains("5 天记录"))
        // 若把 null 当 0，均值会被拉到 49 左右，这里必须仍接近 69
        assertTrue(insight.evidence.contains("69"))
    }

    @Test
    fun too_few_days_is_reported_instead_of_concluded() {
        val series: List<Float?> = List(7) { null } + listOf(70f, 69f, null, null, null, null, null)
        val insight = TrendInterpreter.compareWeeks(series, "kg", true, "体重")
        assertFalse(insight.reliable)
        assertTrue(insight.hasData)
        assertTrue(insight.conclusion.contains("数据不足"))
    }

    @Test
    fun no_recent_data_is_stated_plainly() {
        val insight = TrendInterpreter.compareWeeks(List<Float?>(14) { null }, "kg", true, "体重")
        assertFalse(insight.hasData)
        assertTrue(insight.conclusion.contains("没有记录"))
        assertTrue(insight.limitation.contains("不等于"))
    }
}
