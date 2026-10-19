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
import androidx.compose.runtime.remember
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
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
    content: @Composable BoxScope.() -> Unit,
) {
    // 单个线性渐变即可：原来 3 个覆盖全屏的径向渐变在低端机上是实打实的填充率开销，
    // 而 12% 透明度的色斑肉眼几乎不可见，删掉不损失观感。
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(palette.bgTop, palette.bgMid, palette.bgBottom),
                ),
            ),
    ) {
        content()
    }
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
        modifier = if (onClick != null) base.quietClick(onClick = onClick) else base,
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/**
 * 底部 / 顶部毛玻璃栏的背景修饰符（含顶部细描边）。
 *
 * 说明：真正的「液态玻璃」走 [liquidGlass]，这里保留一个不依赖背景采样的轻量版本，
 * 供不支持 RenderEffect 的场景或预览使用。
 */
fun Modifier.frostBar(palette: OmniPalette, shape: Shape = RoundedCornerShape(palette.corner)): Modifier =
    this
        .clip(shape)
        .background(color = palette.barFill, shape = shape)
        .border(1.dp, palette.barStroke, shape)

/**
 * Liquid Glass：采样背景 → 高斯模糊 → 叠色 + 边缘高光。
 *
 * 与 `Kyant0/AndroidLiquidGlass`（Backdrop）同一套原理：把下层内容录进离屏层再模糊，
 * 这里用 haze 实现（兼容 Compose 1.7，不需要升级到 1.12）。
 * Android 12 以下没有 RenderEffect 时会自动退化为半透明着色（fallbackTint）。
 */
@Composable
fun Modifier.liquidGlass(
    state: HazeState,
    palette: OmniPalette = OmniTheme.palette,
    shape: Shape = RoundedCornerShape(palette.corner),
    blurRadius: Dp = 22.dp,
): Modifier = this
    .clip(shape)
    .hazeEffect(state) {
        style = HazeStyle(
            backgroundColor = palette.barFill,
            tints = listOf(
                HazeTint(palette.cardFill.copy(alpha = 0.50f)),
                HazeTint(palette.accent.copy(alpha = 0.05f)),
            ),
            blurRadius = blurRadius,
            noiseFactor = 0.02f,
            fallbackTint = HazeTint(palette.barFill),
        )
        blurEnabled = true
        alpha = 1f
    }
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.70f),
                Color.White.copy(alpha = 0.18f),
                palette.cardStroke.copy(alpha = 0.35f),
            ),
        ),
        shape = shape,
    )

/** 背景采样源：把整块背景 + 内容标记成「可被玻璃采样」。 */
@Composable
fun rememberHazeState(): HazeState = remember { HazeState() }
