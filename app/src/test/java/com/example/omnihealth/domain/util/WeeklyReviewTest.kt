package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyReviewTest {

    @Test
    fun few_records_suggests_fixing_the_habit_first() {
        val review = WeeklyReview.build(dietDays = 2, breakfastDays = 1, sleepDays = 0, stepDays = 7)
        assertTrue(review.summary.contains("还不够稳定"))
        assertTrue(review.actions.any { it.type == WeeklyReview.ActionType.OPEN_REMINDERS })
    }

    @Test
    fun missing_breakfast_is_phrased_as_missing_records_not_skipped_meals() {
        val review = WeeklyReview.build(dietDays = 5, breakfastDays = 1, sleepDays = 5, stepDays = 5)
        assertTrue(review.summary.contains("没有早餐记录"))
        // 不能说成"没吃早餐"
        assertFalse(review.summary.contains("没吃"))
        assertTrue(review.actions.any { it.type == WeeklyReview.ActionType.OPEN_RECORDS })
    }

    @Test
    fun stable_week_recommends_no_change() {
        val review = WeeklyReview.build(dietDays = 6, breakfastDays = 5, sleepDays = 6, stepDays = 7)
        assertTrue(review.summary.contains("基本稳定"))
        assertEquals(WeeklyReview.ActionType.NONE, review.actions.single().type)
    }

    @Test
    fun empty_week_is_stated_plainly() {
        val review = WeeklyReview.build(0, 0, 0, 0)
        assertFalse(review.hasData)
        assertTrue(review.summary.contains("还没有可用记录"))
        assertEquals(WeeklyReview.ActionType.OPEN_RECORDS, review.actions.single().type)
    }

    @Test
    fun missing_sleep_adds_its_own_action() {
        val review = WeeklyReview.build(dietDays = 5, breakfastDays = 5, sleepDays = 0, stepDays = 5)
        assertTrue(review.actions.any { it.title.contains("睡眠") })
    }
}
