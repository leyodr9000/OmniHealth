package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyReviewTest {

    @Test
    fun few_records_suggests_fixing_the_habit_first() {
        val review = WeeklyReview.build(dietDays = 2, breakfastDays = 1, sleepDays = 0, stepDays = 7)
        assertEquals(WeeklyReview.Summary.FEW_DAYS, review.summary)
        assertTrue(review.actions.any { it.type == WeeklyReview.ActionType.OPEN_REMINDERS })
    }

    @Test
    fun missing_breakfast_is_a_summary_kind_not_a_claim_about_eating() {
        val review = WeeklyReview.build(dietDays = 5, breakfastDays = 1, sleepDays = 5, stepDays = 5)
        assertEquals(WeeklyReview.Summary.MISSING_BREAKFAST, review.summary)
        assertEquals(4, review.missingBreakfast)
        assertTrue(review.actions.any { it.kind == WeeklyReview.ActionKind.CHECK_BREAKFAST })
    }

    @Test
    fun stable_week_recommends_no_change() {
        val review = WeeklyReview.build(dietDays = 6, breakfastDays = 5, sleepDays = 6, stepDays = 7)
        assertEquals(WeeklyReview.Summary.STABLE, review.summary)
        assertEquals(WeeklyReview.ActionKind.NO_CHANGE, review.actions.single().kind)
        assertEquals(WeeklyReview.ActionType.NONE, review.actions.single().type)
    }

    @Test
    fun empty_week_is_stated_plainly() {
        val review = WeeklyReview.build(0, 0, 0, 0)
        assertEquals(WeeklyReview.Summary.NO_DATA, review.summary)
        assertFalse(review.hasData)
        assertEquals(WeeklyReview.ActionType.OPEN_RECORDS, review.actions.single().type)
    }

    @Test
    fun missing_sleep_adds_its_own_action() {
        val review = WeeklyReview.build(dietDays = 5, breakfastDays = 5, sleepDays = 0, stepDays = 5)
        assertTrue(review.actions.any { it.kind == WeeklyReview.ActionKind.LOG_SLEEP })
    }
}