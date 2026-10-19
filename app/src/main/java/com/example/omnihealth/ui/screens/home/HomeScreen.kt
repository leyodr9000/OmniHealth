package com.example.omnihealth.ui.screens.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.components.StatChip
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDiet: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAi: () -> Unit,
    onQuickLog: () -> Unit,
) {
    val palette = OmniTheme.palette
    val today = LocalDate.now()
    val dateText = today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "下午好，Kai",
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$dateText · 保持节奏",
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(palette.accent, palette.accentAlt)),
                        )
                        .bounceClick(pressScale = 0.9f, onClick = onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }

        item { HealthScoreCard(score = 78) }

        item {
            SectionHeader(
                title = "今日概览",
                subtitle = "目标完成度",
                action = {
                    Text(
                        text = "查看统计 →",
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.bounceClick(pressScale = 0.94f, haptics = false, onClick = onOpenStats),
                    )
                },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    emoji = "👟",
                    title = "步数",
                    value = "6,842",
                    unit = "步",
                    accent = palette.accent,
                    progress = 0.68f,
                    hint = "目标 10,000 步",
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
                MetricTile(
                    emoji = "💧",
                    title = "饮水",
                    value = "1.35",
                    unit = "L",
                    accent = Color(0xFF4FB3FF),
                    progress = 0.54f,
                    hint = "目标 2.5 L",
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    emoji = "🌙",
                    title = "睡眠",
                    value = "7.2",
                    unit = "h",
                    accent = Color(0xFF8B7CFF),
                    progress = 0.9f,
                    hint = "深睡 1.8 h",
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
                MetricTile(
                    emoji = "🔥",
                    title = "热量",
                    value = "1,486",
                    unit = "kcal",
                    accent = Color(0xFFFF8A5B),
                    progress = 0.74f,
                    hint = "剩余 514 kcal",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDiet,
                )
            }
        }

        item { AiTipCard(onClick = onOpenAi) }

        item {
            SectionHeader(title = "快捷操作", subtitle = "点一下就能记")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("体重", "62.4 kg", palette.accentAlt, Modifier.weight(1f))
                StatChip("心率", "72 bpm", palette.danger, Modifier.weight(1f))
                StatChip("心情", "不错", palette.success, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HealthScoreCard(score: Int) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = score / 100f,
                modifier = Modifier.size(104.dp),
                ringColor = palette.accent,
                strokeWidth = 9.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        color = palette.textPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("健康分", color = palette.textTertiary, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "综合健康指数",
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "比上周 +4 分，继续保持",
                    color = palette.success,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Spacer(Modifier.height(10.dp))
                ScoreBar("营养", 0.82f, palette.accent)
                ScoreBar("运动", 0.66f, palette.accentAlt)
                ScoreBar("睡眠", 0.90f, Color(0xFF8B7CFF))
            }
        }
    }
}

@Composable
private fun ScoreBar(label: String, fraction: Float, color: Color) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.textTertiary, fontSize = 10.sp, modifier = Modifier.width(34.dp))
        Box(
            Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.textTertiary.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun AiTipCard(onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(palette.corner))
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.accent.copy(alpha = 0.92f),
                        palette.accentAlt.copy(alpha = 0.92f),
                    ),
                ),
            )
            .bounceClick(pressScale = 0.97f, onClick = onClick)
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "AI 健康顾问",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text("生成 7 日报告 →", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "你今天蛋白质还差 28 g，晚餐建议鸡胸肉 120 g + 无糖酸奶 200 ml；\n睡前 40 分钟放下手机可提升深睡比例。",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
