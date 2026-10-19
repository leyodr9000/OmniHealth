package com.example.omnihealth.ui.chart

import android.graphics.Color
import com.example.omnihealth.ui.theme.OmniPalette
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import androidx.compose.ui.graphics.toArgb

/**
 * 图表样式统一器：剥掉 MPAndroidChart 的默认“报表感”，换成应用主题。
 *
 *  - 去描述、去图例、去网格背景
 *  - 轴线透明，只保留极淡的水平参考线
 *  - 字体统一为主题三级文本色
 */
object ModernChartStyler {

    fun base(chart: Chart<*>, palette: OmniPalette) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setBackgroundColor(Color.TRANSPARENT)
        chart.setNoDataText("")
        chart.setTouchEnabled(true)
    }

    fun axes(chart: BarLineChartBase<*>, palette: OmniPalette, showLabels: Boolean = true) {
        base(chart, palette)
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)
        chart.isDragEnabled = true
        chart.setScaleEnabled(false)

        chart.axisRight.isEnabled = false
        chart.axisLeft.apply {
            setDrawAxisLine(false)
            setDrawGridLines(true)
            gridColor = palette.textTertiary.copy(alpha = 0.14f).toArgb()
            gridLineWidth = 0.7f
            textColor = palette.textTertiary.toArgb()
            textSize = 9f
            setDrawLabels(showLabels)
            setLabelCount(4, false)
        }
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textColor = palette.textTertiary.toArgb()
            textSize = 9f
            granularity = 1f
            setAvoidFirstLastClipping(true)
            yOffset = 6f
        }
    }

    /** 目标参考线。 */
    fun goalLine(chart: BarLineChartBase<*>, value: Float, color: Int, label: String = "") {
        if (value <= 0f) return
        chart.axisLeft.removeAllLimitLines()
        chart.axisLeft.addLimitLine(
            com.github.mikephil.charting.components.LimitLine(value, label).apply {
                lineColor = color
                lineWidth = 1.2f
                enableDashedLine(10f, 8f, 0f)
                labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 9f
            },
        )
    }

    fun axisRange(chart: BarLineChartBase<*>, min: Float, max: Float) {
        chart.axisLeft.axisMinimum = min
        chart.axisLeft.axisMaximum = max
    }

    fun styleY(y: YAxis, palette: OmniPalette) {
        y.textColor = palette.textTertiary.toArgb()
    }
}
