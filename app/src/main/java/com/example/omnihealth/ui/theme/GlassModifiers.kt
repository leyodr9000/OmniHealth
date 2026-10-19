package com.example.omnihealth.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 动态网格渐变背景（Mesh Gradient）。
 *
 * 三团高斯色斑以不同周期缓慢漂移，形成 HyperOS 风格的「光晕流动」底图。
 * 所有卡片、玻璃栏都浮在这层画布之上。
 */
@Composable
fun MeshGradientBackground(
    palette: OmniPalette = OmniTheme.palette,
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val t: Float = if (animated) {
        val transition = rememberInfiniteTransition(label = "mesh")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 26_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "meshPhase",
        )
        value
    } else {
        0f
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.bgTop, palette.bgMid, palette.bgBottom),
                ),
                size = size,
            )
            drawBlob(palette.blobA, size.width * 0.18f, size.height * 0.12f, size.minDimension * 0.95f, t)
            drawBlob(palette.blobB, size.width * 0.92f, size.height * 0.28f, size.minDimension * 0.85f, t + 1.9f)
            drawBlob(palette.blobC, size.width * 0.45f, size.height * 0.86f, size.minDimension * 0.9f, t + 3.6f)
        }
        content()
    }
}

private fun DrawScope.drawBlob(color: Color, cx: Float, cy: Float, radius: Float, phase: Float) {
    val dx = cos(phase) * radius * 0.16f
    val dy = sin(phase * 0.8f) * radius * 0.13f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = Offset(cx + dx, cy + dy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx + dx, cy + dy),
    )
}

/**
 * 毛玻璃卡片。
 *
 * 玻璃层 = 半透明填充 + 顶部高光渐变 + 细描边 + 环境阴影，
 * 叠在渐变网格之上即呈现「背景透过玻璃」的质感。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    palette: OmniPalette = OmniTheme.palette,
    shape: Shape = RoundedCornerShape(palette.corner),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // 实心卡片：纯色底 + 1px 描边，不用渐变、不用阴影。
    val base = modifier
        .clip(shape)
        .background(color = palette.cardFill, shape = shape)
        .border(
            width = 1.dp,
            color = palette.cardStroke,
            shape = shape,
        )

    Column(
        modifier = if (onClick != null) base.bounceClick(onClick = onClick) else base,
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/**
 * 底部 / 顶部毛玻璃栏的背景修饰符（含顶部细描边）。
 */
fun Modifier.frostBar(palette: OmniPalette, shape: Shape = RoundedCornerShape(palette.corner)): Modifier =
    this
        .clip(shape)
        .background(color = palette.barFill, shape = shape)
        .border(1.dp, palette.barStroke, shape)
