package com.example.omnihealth.ui.chart

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.omnihealth.ui.theme.OmniPalette
import com.example.omnihealth.ui.theme.OmniTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/* ------------------------------------------------------------------ */
/* 折线图                                                              */
/* ------------------------------------------------------------------ */

@Composable
fun LineTrendChart(
    labels: List<String>,
    values: List<Float>,
    color: ComposeColor,
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            LineChart(context).apply {
                ModernChartStyler.axes(this, palette)
                setDrawMarkers(false)
            }
        },
        update = { chart ->
            ModernChartStyler.axes(chart, palette)
            val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            val set = LineDataSet(entries, "").apply {
                this.color = color.toArgb()
                lineWidth = 2.6f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawCircles(true)
                circleRadius = 3.4f
                setCircleColor(color.toArgb())
                setDrawCircleHole(false)
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = color.toArgb()
                fillAlpha = 38
                highLightColor = color.copy(alpha = 0.35f).toArgb()
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = LineData(set)
            if (chart.data?.dataSetCount ?: 0 > 0) chart.animateX(260)
            chart.setVisibleXRangeMaximum(labels.size.toFloat().coerceAtLeast(1f))
            chart.invalidate()
        },
    )
}

/* ------------------------------------------------------------------ */
/* 组合图：柱状步数 + 折线摄入                                          */
/* ------------------------------------------------------------------ */

@Composable
fun CombinedStepsChart(
    labels: List<String>,
    steps: List<Int>,
    calories: List<Int>,
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
) {
    val barColor = palette.accent
    val lineColor = palette.warning

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            CombinedChart(context).apply {
                ModernChartStyler.axes(this, palette)
                setDrawOrder(
                    arrayOf(
                        CombinedChart.DrawOrder.BAR,
                        CombinedChart.DrawOrder.LINE,
                    ),
                )
            }
        },
        update = { chart ->
            ModernChartStyler.axes(chart, palette)

            val barSet = BarDataSet(
                steps.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) },
                "",
            ).apply {
                color = barColor.copy(alpha = 0.85f).toArgb()
                setDrawValues(false)
                highLightAlpha = 0
            }
            val lineSet = LineDataSet(
                calories.mapIndexed { index, value -> Entry(index.toFloat(), value.toFloat()) },
                "",
            ).apply {
                color = lineColor.toArgb()
                lineWidth = 2.2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawCircles(true)
                circleRadius = 2.8f
                setCircleColor(lineColor.toArgb())
                setDrawCircleHole(false)
                setDrawValues(false)
            }

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = CombinedData().apply {
                setData(BarData(barSet).apply { barWidth = 0.5f })
                setData(LineData(lineSet))
            }
            chart.invalidate()
        },
    )
}

/* ------------------------------------------------------------------ */
/* 饼图                                                                */
/* ------------------------------------------------------------------ */

@Composable
fun MacroPieChart(
    protein: Float,
    carbs: Float,
    fat: Float,
    labels: List<String>,
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
) {
    val colors = listOf(palette.accent, palette.warning, palette.accentAlt)

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            PieChart(context).apply {
                ModernChartStyler.base(this, palette)
                isDrawHoleEnabled = true
                holeRadius = 64f
                transparentCircleRadius = 68f
                setHoleColor(Color.TRANSPARENT)
                setDrawEntryLabels(false)
                setUsePercentValues(false)
                setCenterTextSize(15f)
                setCenterTextColor(palette.textPrimary.toArgb())
                setExtraOffsets(6f, 6f, 6f, 6f)
                isRotationEnabled = false
            }
        },
        update = { chart ->
            val entries = listOf(
                PieEntry(protein.coerceAtLeast(0.1f), labels.getOrElse(0) { "P" }),
                PieEntry(carbs.coerceAtLeast(0.1f), labels.getOrElse(1) { "C" }),
                PieEntry(fat.coerceAtLeast(0.1f), labels.getOrElse(2) { "F" }),
            )
            val set = PieDataSet(entries, "").apply {
                this.colors = colors.map { it.toArgb() }
                setDrawValues(false)
                sliceSpace = 4f
                valueTextColor = paneTextColor(palette)
            }
            chart.setCenterText("${(protein + carbs + fat).toInt()} g")
            chart.data = PieData(set)
            chart.animateY(280)
            chart.invalidate()
        },
    )
}

private fun paneTextColor(palette: OmniPalette): Int = palette.textSecondary.toArgb()

/* ------------------------------------------------------------------ */
/* 柱状图                                                              */
/* ------------------------------------------------------------------ */

@Composable
fun StepsBarChart(
    labels: List<String>,
    steps: List<Int>,
    goal: Int,
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            BarChart(context).apply {
                ModernChartStyler.axes(this, palette)
                setDrawValueAboveBar(false)
                setFitBars(true)
            }
        },
        update = { chart ->
            ModernChartStyler.axes(chart, palette)
            ModernChartStyler.goalLine(chart, goal.toFloat(), palette.danger.copy(alpha = 0.55f).toArgb(), "goal")

            val set = BarDataSet(
                steps.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) },
                "",
            ).apply {
                color = palette.accent.toArgb()
                setDrawValues(false)
                highLightAlpha = 0
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = BarData(set).apply { barWidth = 0.52f }
            chart.animateY(280)
            chart.invalidate()
        },
    )
}

/* ------------------------------------------------------------------ */
/* 雷达图                                                              */
/* ------------------------------------------------------------------ */

@Composable
fun HealthRadarChart(
    labels: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            RadarChart(context).apply {
                ModernChartStyler.base(this, palette)
                webLineWidth = 1f
                webLineWidthInner = 0.8f
                webColor = palette.textTertiary.copy(alpha = 0.25f).toArgb()
                webColorInner = palette.textTertiary.copy(alpha = 0.15f).toArgb()
                webAlpha = 120
                setTouchEnabled(false)
                yAxis.apply {
                    axisMinimum = 0f
                    axisMaximum = 100f
                    setDrawLabels(false)
                    granularity = 25f
                }
                xAxis.textColor = palette.textSecondary.toArgb()
                xAxis.textSize = 9f
            }
        },
        update = { chart ->
            val set = RadarDataSet(
                values.mapIndexed { index, value -> RadarEntry(value, index) },
                "",
            ).apply {
                color = palette.accent.toArgb()
                fillColor = palette.accent.toArgb()
                setDrawFilled(true)
                fillAlpha = 70
                lineWidth = 2f
                setDrawValues(false)
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.data = RadarData(set)
            chart.animateY(280)
            chart.invalidate()
        },
    )
}

/* ------------------------------------------------------------------ */
/* 尺寸辅助                                                            */
/* ------------------------------------------------------------------ */

val ChartHeight = 210.dp
