package com.example.omnihealth.ui.screens.diet_sport

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick

@Composable
fun DietSportScreen(
    initialTab: Int = 0,
    onQuickLog: () -> Unit,
    onOpenSport: () -> Unit,
) {
    val palette = OmniTheme.palette
    var tab by remember { mutableIntStateOf(initialTab) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "饮食与运动",
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "记录每一口，也记录每一步",
                color = palette.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        item {
            SegmentedControl(
                options = listOf("🍽 饮食", "🏃 运动"),
                selected = tab,
                onSelect = { tab = it },
            )
        }

        if (tab == 0) {
            item { CalorieSummaryCard() }
            item {
                SearchBar(placeholder = "搜索食物，如「鸡胸肉」「拿铁」")
            }
            item { SectionHeader(title = "常用食物", subtitle = "来自预置食物字典 · 200+ 条") }
            items(6) { index ->
                val foods = listOf(
                    Triple("鸡胸肉（生）", "高蛋白 · 低脂", "133 kcal / 100 g"),
                    Triple("燕麦片", "复合碳水 · 高纤维", "389 kcal / 100 g"),
                    Triple("希腊酸奶", "蛋白 + 益生菌", "97 kcal / 100 g"),
                    Triple("牛油果", "优质脂肪", "160 kcal / 100 g"),
                    Triple("西兰花", "维生素 C · 膳食纤维", "34 kcal / 100 g"),
                    Triple("美式咖啡", "0 糖 · 0 脂", "2 kcal / 100 ml"),
                )
                val item = foods[index]
                FoodRow(item.first, item.second, item.third)
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile(
                        emoji = "👟",
                        title = "今日步数",
                        value = "6,842",
                        unit = "步",
                        accent = palette.accent,
                        progress = 0.68f,
                        hint = "传感器已连接",
                        modifier = Modifier.weight(1f),
                    )
                    MetricTile(
                        emoji = "🔥",
                        title = "活动消耗",
                        value = "312",
                        unit = "kcal",
                        accent = Color(0xFFFF8A5B),
                        progress = 0.52f,
                        hint = "目标 600 kcal",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item { WorkoutTimerCard() }
            item {
                SectionHeader(title = "今日训练", subtitle = "计时器 · 组间休息")
            }
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("暂无训练记录", color = palette.textSecondary, fontSize = 13.sp)
                    Text(
                        "点击上方「开始训练」即可计时，完成后自动计入统计。",
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.textPrimary.copy(alpha = 0.06f))
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (active) {
                            Modifier.background(
                                Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt)),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .bounceClick(pressScale = 0.96f, haptics = false) { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
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

@Composable
private fun CalorieSummaryCard() {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = 0.74f,
                modifier = Modifier.size(86.dp),
                ringColor = Color(0xFFFF8A5B),
                strokeWidth = 8.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1,486", color = palette.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("kcal", color = palette.textTertiary, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text("今日摄入", color = palette.textSecondary, fontSize = 12.sp)
                Text(
                    "剩余 514 kcal",
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroPill("蛋白", "96 g", palette.accent)
                    MacroPill("碳水", "168 g", palette.warning)
                    MacroPill("脂肪", "52 g", palette.accentAlt)
                }
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String, color: Color) {
    val palette = OmniTheme.palette
    Column {
        Text(label, color = palette.textTertiary, fontSize = 9.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SearchBar(placeholder: String) {
    val palette = OmniTheme.palette
    var text by remember { mutableStateOf("") }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardFill)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🔍", fontSize = 13.sp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) {
                Text(placeholder, color = palette.textTertiary, fontSize = 12.sp)
            }
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FoodRow(name: String, desc: String, kcal: String) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardFill)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("🥗", fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = palette.textTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(kcal, color = palette.textSecondary, fontSize = 11.sp)
            Text("＋", color = palette.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WorkoutTimerCard() {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Text("训练计时器", color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("00:00", color = palette.textPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text("选择动作后开始计时 · 支持组间休息提醒", color = palette.textTertiary, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("力量", "跑步", "骑行", "瑜伽").forEach { label ->
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.accent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text(label, color = palette.accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
