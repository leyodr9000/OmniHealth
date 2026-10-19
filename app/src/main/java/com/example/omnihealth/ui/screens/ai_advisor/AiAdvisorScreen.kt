package com.example.omnihealth.ui.screens.ai_advisor

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
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick

@Composable
fun AiAdvisorScreen() {
    val palette = OmniTheme.palette

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "AI 健康顾问",
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "基于你最近 7 天的数据给出建议 · 离线可用",
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt)))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                ) {
                    Text("DeepSeek", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(palette.corner))
                    .background(Brush.linearGradient(listOf(palette.accent, palette.accentAlt)))
                    .bounceClick(pressScale = 0.97f) { }
                    .padding(18.dp),
            ) {
                Column {
                    Text("一键生成 7 日健康报告", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "汇总饮食、运动、睡眠、体征四类数据，输出评分 + 三项可执行建议",
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("立即生成 ✨", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text("对话", color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Bubble("我最近总是下午犯困，怎么调整？", isUser = true)
                Spacer(Modifier.height(8.dp))
                Bubble(
                    "从你的记录看，午餐碳水占比 68% 偏高，血糖回落导致下午困倦。建议把午餐主食减半，增加 20 g 蛋白质，并把 15 分钟散步放到饭后。",
                    isUser = false,
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.cardFill)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("问问 AI…", color = palette.textTertiary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt)))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text("发送", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Bubble(text: String, isUser: Boolean) {
    val palette = OmniTheme.palette
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isUser) {
                        Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt))
                    } else {
                        Brush.verticalGradient(
                            listOf(palette.textPrimary.copy(alpha = 0.06f), palette.textPrimary.copy(alpha = 0.04f)),
                        )
                    },
                )
                .padding(horizontal = 13.dp, vertical = 9.dp),
        ) {
            Text(
                text = text,
                color = if (isUser) Color.White else palette.textPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
