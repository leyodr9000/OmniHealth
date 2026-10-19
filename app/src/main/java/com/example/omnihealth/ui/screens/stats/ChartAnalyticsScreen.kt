package com.example.omnihealth.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick

@Composable
fun ChartAnalyticsScreen() {
    val palette = OmniTheme.palette
    val tabs = listOf("趋势", "分布", "对比", "雷达")
    var selected by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "数据洞察",
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "MPAndroidChart · 四种图表 × 四套指标",
                color = palette.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { label ->
                    val index = tabs.indexOf(label)
                    val active = index == selected
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (active) {
                                    Modifier.background(
                                        Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt)),
                                    )
                                } else {
                                    Modifier.background(palette.textPrimary.copy(alpha = 0.06f))
                                },
                            )
                            .bounceClick(pressScale = 0.94f, haptics = false) { selected = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                SectionHeader(
                    title = listOf("体重与体脂趋势", "热量构成", "周对比", "健康五维雷达")[selected],
                    subtitle = "最近 7 天",
                )
                Spacer(Modifier.height(12.dp))
                ChartPlaceholder(
                    label = listOf("LineChart", "PieChart", "BarChart", "RadarChart")[selected],
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("周均步数", "6,420", "+8.2%", Modifier.weight(1f))
                SummaryCard("周均睡眠", "7.1 h", "+0.4 h", Modifier.weight(1f))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("体脂率", "18.6%", "-0.3%", Modifier.weight(1f))
                SummaryCard("静息心率", "64 bpm", "-2", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChartPlaceholder(label: String) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.accent.copy(alpha = 0.10f),
                        palette.accentAlt.copy(alpha = 0.06f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = palette.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                "M6 里程碑接入 MPAndroidChart 真实渲染",
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, delta: String, modifier: Modifier = Modifier) {
    val palette = OmniTheme.palette
    GlassCard(modifier, contentPadding = PaddingValues(14.dp)) {
        Text(title, color = palette.textTertiary, fontSize = 10.sp)
        Text(
            value,
            color = palette.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 3.dp),
        )
        Text(delta, color = palette.success, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
