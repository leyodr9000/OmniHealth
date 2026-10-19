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
}
