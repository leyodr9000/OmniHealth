package com.example.omnihealth.domain.util

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 趋势解读的**确定性**部分。
 *
 * 只产出结构化结论，不产出展示文案——文案由 UI 按语言组装。
 * 规则：均值与覆盖度由这里算；缺失不当 0；覆盖不足不给结论；不做因果解释。
 */
object TrendInterpreter {

    const val MIN_DAYS = 4

    enum class Status { NO_RECENT_DATA, INSUFFICIENT, READY }

    enum class Direction { UP, DOWN, FLAT }

    data class Insight(
        val status: Status,
        val direction: Direction = Direction.FLAT,
        val delta: Float = 0f,
        val recentAvg: Float = 0f,
        val previousAvg: Float = 0f,
        val recentDays: Int = 0,
        val previousDays: Int = 0,
        val improving: Boolean = false,
        val unit: String = "",
    ) {
        val reliable: Boolean get() = status == Status.READY
        val hasData: Boolean get() = status != Status.NO_RECENT_DATA
    }

    fun compareWeeks(series: List<Float?>, unit: String, betterWhenLower: Boolean): Insight {
        val recent = series.takeLast(7)
        val previous = series.dropLast(7).takeLast(7)
        val recentDays = recent.filterNotNull()
        val previousDays = previous.filterNotNull()

        if (recentDays.isEmpty()) return Insight(status = Status.NO_RECENT_DATA, unit = unit)

        if (previousDays.isEmpty() || recentDays.size < MIN_DAYS || previousDays.size < MIN_DAYS) {
            return Insight(
                status = Status.INSUFFICIENT,
                recentDays = recentDays.size,
                previousDays = previousDays.size,
                unit = unit,
            )
        }

        val recentAvg = recentDays.average().toFloat()
        val previousAvg = previousDays.average().toFloat()
        val delta = recentAvg - previousAvg
        val direction = when {
            abs(delta) < 0.05f -> Direction.FLAT
            delta > 0 -> Direction.UP
            else -> Direction.DOWN
        }

        return Insight(
            status = Status.READY,
            direction = direction,
            delta = delta,
            recentAvg = recentAvg,
            previousAvg = previousAvg,
            recentDays = recentDays.size,
            previousDays = previousDays.size,
            improving = when (direction) {
                Direction.FLAT -> false
                Direction.UP -> !betterWhenLower
                Direction.DOWN -> betterWhenLower
            },
            unit = unit,
        )
    }

    fun format(value: Float): String {
        val scaled = (value * 10f).roundToInt() / 10f
        return if (scaled == scaled.toInt().toFloat()) scaled.toInt().toString() else scaled.toString()
    }
}