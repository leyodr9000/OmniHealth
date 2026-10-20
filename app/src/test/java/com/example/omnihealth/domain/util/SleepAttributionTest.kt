package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SleepAttributionTest {

    /** 睡眠归属日：用「该天中午」的时间戳写入，epochDayOf 必须还原同一天。 */
    @Test
    fun noon_of_day_maps_back_to_that_day() {
        val epochDay = 20_300L
        assertEquals(epochDay, TimeUtils.epochDayOf(TimeUtils.noonOfDayMillis(epochDay)))
    }

    @Test
    fun last_night_is_one_day_before_today() {
        val today = TimeUtils.epochDayNow()
        val lastNight = today - 1
        assertEquals(lastNight, TimeUtils.epochDayOf(TimeUtils.noonOfDayMillis(lastNight)))
        assertEquals(today - 1, lastNight)
    }

    @Test
    fun consecutive_days_do_not_collide() {
        val a = TimeUtils.epochDayOf(TimeUtils.noonOfDayMillis(20_300L))
        val b = TimeUtils.epochDayOf(TimeUtils.noonOfDayMillis(20_301L))
        assertEquals(1L, b - a)
    }
}