package com.example.omnihealth.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeZoneAttributionTest {

    private val noonUtc = 20_300L * 86_400_000L + 12 * 3_600_000L

    @Test
    fun offset_is_ignored_when_absent() {
        // 偏移 0 视为未记录，退回当前时区
        assertEquals(TimeUtils.epochDayOf(noonUtc), TimeUtils.epochDayAtOffset(noonUtc, 0))
    }

    @Test
    fun recorded_offset_wins_over_current_zone() {
        // UTC+8 的记录：本地时间比 UTC 晚 8 小时
        val day = TimeUtils.epochDayAtOffset(noonUtc, 8 * 60)
        assertEquals(20_300L, day)
    }

    @Test
    fun negative_offset_shifts_back_a_day() {
        // UTC-11 时，同一天中午 UTC 已被算作前一天
        val day = TimeUtils.epochDayAtOffset(20_300L * 86_400_000L + 3_600_000L, -11 * 60)
        assertEquals(20_299L, day)
    }
}