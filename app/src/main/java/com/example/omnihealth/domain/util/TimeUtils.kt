package com.example.omnihealth.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 本地时区下的「天」与时间戳互转。 */
object TimeUtils {

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun epochDayNow(): Long = LocalDate.now(zone).toEpochDay()

    fun epochDayOf(timestamp: Long): Long =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate().toEpochDay()

    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDayMillis(epochDay: Long): Long = startOfDayMillis(epochDay + 1) - 1

    /** 以今天为终点，返回最近 [days] 天的 epochDay 列表（升序）。 */
    fun lastDays(days: Int, endEpochDay: Long = epochDayNow()): List<Long> =
        (days - 1 downTo 0).map { endEpochDay - it }

    fun localDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    /**
     * 按记录当时的时区偏移还原「本地日期」。
     *
     * 用户出差/跨时区后，历史记录应停留在它被写下时的那一天，
     * 而不是按今天的时区重新归属。偏移为 0 视为「未记录」，退回当前时区。
     */
    fun epochDayAtOffset(timestamp: Long, tzOffsetMinutes: Int): Long {
        if (tzOffsetMinutes == 0) return epochDayOf(timestamp)
        val zone = java.time.ZoneOffset.ofTotalSeconds(tzOffsetMinutes * 60)
        return Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate().toEpochDay()
    }

    /** 某天的中午（本地时区）：用于「归属某天」但不关心具体时刻的记录。 */
    fun noonOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
}
