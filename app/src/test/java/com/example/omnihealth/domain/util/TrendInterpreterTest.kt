package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrendInterpreterTest {

    @Test
    fun compares_two_full_weeks() {
        val series = List(7) { 70f } + List(7) { 69f }
        val insight = TrendInterpreter.compareWeeks(series, "kg", betterWhenLower = true)
        assertEquals(TrendInterpreter.Status.READY, insight.status)
        assertEquals(TrendInterpreter.Direction.DOWN, insight.direction)
        assertTrue(insight.improving)
        assertEquals(7, insight.recentDays)
        assertEquals(69f, insight.recentAvg, 0.01f)
    }

    @Test
    fun missing_days_are_not_treated_as_zero() {
        val recent: List<Float?> = listOf(70f, null, 69f, null, 69f, 68f, 69f)
        val previous: List<Float?> = List(7) { 70f }
        val insight = TrendInterpreter.compareWeeks(previous + recent, "kg", true)
        assertEquals(TrendInterpreter.Status.READY, insight.status)
        assertEquals(5, insight.recentDays)
        // 若把 null 当 0，均值会被拉到 49 上下；这里必须仍在 69 附近
        assertTrue(insight.recentAvg in 68f..70f)
    }

    @Test
    fun too_few_days_is_reported_instead_of_concluded() {
        val series: List<Float?> = List(7) { null } + listOf(70f, 69f, null, null, null, null, null)
        val insight = TrendInterpreter.compareWeeks(series, "kg", true)
        assertEquals(TrendInterpreter.Status.INSUFFICIENT, insight.status)
        assertFalse(insight.reliable)
        assertTrue(insight.hasData)
    }

    @Test
    fun no_recent_data_is_stated_plainly() {
        val insight = TrendInterpreter.compareWeeks(List<Float?>(14) { null }, "kg", true)
        assertEquals(TrendInterpreter.Status.NO_RECENT_DATA, insight.status)
        assertFalse(insight.hasData)
    }

    @Test
    fun flat_change_is_not_reported_as_a_trend() {
        val series = List(7) { 70f } + List(7) { 70.02f }
        val insight = TrendInterpreter.compareWeeks(series, "kg", true)
        assertEquals(TrendInterpreter.Direction.FLAT, insight.direction)
        assertFalse(insight.improving)
    }

    @Test
    fun higher_is_better_flips_improving() {
        val steps = List(7) { 6000f } + List(7) { 9000f }
        val insight = TrendInterpreter.compareWeeks(steps, "步", betterWhenLower = false)
        assertEquals(TrendInterpreter.Direction.UP, insight.direction)
        assertTrue(insight.improving)
    }
}
