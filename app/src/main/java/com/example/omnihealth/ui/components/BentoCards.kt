package com.example.omnihealth.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniPalette
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val palette = OmniTheme.palette
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        action?.invoke()
    }
}

/**
 * 环形进度：轨道 + 渐变扫掠弧，数值变化时带阻尼动画。
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = OmniTheme.palette.accent,
    trackColor: Color = OmniTheme.palette.textTertiary.copy(alpha = 0.16f),
    strokeWidth: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "ringProgress",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(ringColor.copy(alpha = 0.55f), ringColor, ringColor.copy(alpha = 0.9f)),
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/**
 * Bento 指标卡：emoji + 标题 + 大数字 + 进度条 + 可点击。
 */
@Composable
fun MetricTile(
    emoji: String,
    title: String,
    value: String,
    unit: String,
    accent: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    hint: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val palette = OmniTheme.palette
    GlassCard(
        modifier = modifier,
        palette = palette,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = palette.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = unit,
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        if (progress != null) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.textTertiary.copy(alpha = 0.15f)),
            ) {
                val fraction by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "tileProgress",
                )
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.55f)))),
                )
            }
        }
        if (hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = hint, color = palette.textTertiary, fontSize = 10.sp)
        }
    }
}

/** 小号数据徽章。 */
@Composable
fun StatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val palette = OmniTheme.palette
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, color = palette.textTertiary, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/** 呼吸感的状态点。 */
@Composable
fun PulsingDot(color: Color, size: Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "dotAlpha",
    )
    val diameter by animateDpAsState(targetValue = size, label = "dotSize")
    Box(
        Modifier
            .size(diameter)
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = alpha)),
    )
}

/** 通用 Bento 壳：可指定高度与点击。 */
@Composable
fun BentoBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        content()
    }
}
