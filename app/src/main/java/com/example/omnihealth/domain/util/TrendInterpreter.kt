package com.example.omnihealth.domain.util

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 趋势解读的**确定性**部分。
 *
 * 对应方案「趋势自动解读」的关键规则：
 * - 均值、变化量、覆盖度全部由这里的代码算出，模型只负责组织语言；
 * - 缺失记录不当作 0，也不推断用户没吃/没动；
 * - 只描述变化，不解释因果；
 * - 覆盖不足时只说明「缺什么」，不硬给结论。
 */
object TrendInterpreter {

    /** 至少要有这么多天的记录，才值得给结论。 */
    private const val MIN_DAYS = 4

    data class Insight(
        /** 结论：发生了什么 */
        val conclusion: String,
        /** 依据：区间、有效天数、计算结果 */
        val evidence: String,
        /** 限制：数据缺口或不该过度解读的地方 */
        val limitation: String,
        /** 是否足以支撑结论 */
        val reliable: Boolean,
        val hasData: Boolean,
    )

    /**
     * 对比最近 7 天与前 7 天。
     *
     * @param series 从旧到新、每天一个值；无记录的天传 null（**不能传 0**）。
     * @param unit 展示单位，例如 "kg"、"kcal"。
     * @param betterWhenLower 体重下降算改善，步数上升算改善。
     */
    fun compareWeeks(
        series: List<Float?>,
        unit: String,
        betterWhenLower: Boolean,
        label: String,
    ): Insight {
        val recent = series.takeLast(7)
        val previous = series.dropLast(7).takeLast(7)
        val recentDays = recent.filterNotNull()
        val previousDays = previous.filterNotNull()

        if (recentDays.isEmpty()) {
            return Insight(
                conclusion = "$label 最近 7 天没有记录",
                evidence = "统计区间 7 天，有效记录 0 天",
                limitation = "没有记录不等于没有发生，先补几天再解读",
                reliable = false,
                hasData = false,
            )
        }

        if (previousDays.isEmpty() || recentDays.size < MIN_DAYS || previousDays.size < MIN_DAYS) {
            return Insight(
                conclusion = "$label 数据不足，暂不比较趋势",
                evidence = "最近 7 天有效记录 ${recentDays.size} 天，前 7 天 ${previousDays.size} 天",
                limitation = "两周各至少 $MIN_DAYS 天记录才给结论；缺失的天没有被当作 0",
                reliable = false,
                hasData = true,
            )
        }

        val recentAvg = recentDays.average().toFloat()
        val previousAvg = previousDays.average().toFloat()
        val delta = recentAvg - previousAvg
        val improved = if (betterWhenLower) delta < 0 else delta > 0
        val direction = when {
            delta > 0 -> "上升"
            delta < 0 -> "下降"
            else -> "持平"
        }
        val magnitude = abs(delta)

        val conclusion = if (magnitude < 0.05f) {
            "$label 近 7 天与前 7 天基本持平"
        } else {
            "$label 近 7 天 $direction ${format(magnitude)} $unit" + if (improved) "，方向符合目标" else ""
        }

        return Insight(
            conclusion = conclusion,
            evidence = "最近 7 天均值 ${format(recentAvg)} $unit（${recentDays.size} 天记录），" +
                "前 7 天均值 ${format(previousAvg)} $unit（${previousDays.size} 天记录）",
            limitation = "均值受记录天数影响，单日波动不代表整体趋势；缺记录的天已排除",
            reliable = true,
            hasData = true,
        )
    }

    private fun format(value: Float): String {
        val scaled = (value * 10f).roundToInt() / 10f
        return if (scaled == scaled.toInt().toFloat()) scaled.toInt().toString() else scaled.toString()
    }
}
