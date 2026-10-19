package com.example.omnihealth.ui.theme

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.omnihealth.R

/**
 * 三套可实时切换的主题。
 *
 *  - [MIUIX_GLASS]  HyperOS / MIUIX 风格：柔光网格渐变 + 毛玻璃 + 24dp 超椭圆 + 弹性点击
 *  - [MD3_MORANDI] Material Design 3 · 莫兰迪低饱和：实心卡片 + 柔和阴影 + 高可读性
 *  - [CYBER_DARK]  赛博暗黑：霓虹描边 + 冷色光晕 + 数据可视化感
 */
enum class ThemeMode(
    val id: String,
    @StringRes val labelRes: Int,
    val emoji: String,
    @StringRes val taglineRes: Int,
    @StringRes val blurbRes: Int,
) {
    MIUIX_GLASS(
        id = "miuix_glass",
        labelRes = R.string.theme_miui_label,
        emoji = "🫧",
        taglineRes = R.string.theme_miui_tagline,
        blurbRes = R.string.theme_miui_blurb,
    ),
    MD3_MORANDI(
        id = "md3_morandi",
        labelRes = R.string.theme_morandi_label,
        emoji = "🎨",
        taglineRes = R.string.theme_morandi_tagline,
        blurbRes = R.string.theme_morandi_blurb,
    ),
    CYBER_DARK(
        id = "cyber_dark",
        labelRes = R.string.theme_cyber_label,
        emoji = "🌃",
        taglineRes = R.string.theme_cyber_tagline,
        blurbRes = R.string.theme_cyber_blurb,
    ),
    ;

    companion object {
        fun from(id: String?): ThemeMode = entries.firstOrNull { it.id == id } ?: MIUIX_GLASS
    }
}

/** 表面材质风格，供组件决定描边、阴影、光泽的绘制方式。 */
enum class SurfaceStyle { FROSTED, SOFT, NEON }

/**
 * 主题调色板：在 Material3 ColorScheme 之外，补充骨架所需的渐变、玻璃、图表色。
 */
@Immutable
data class OmniPalette(
    val mode: ThemeMode,
    val style: SurfaceStyle,
    val dark: Boolean,
    // 背景网格渐变
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val blobA: Color,
    val blobB: Color,
    val blobC: Color,
    // 品牌色
    val accent: Color,
    val accentAlt: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    // 文本
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // 卡片 / 栏
    val cardFill: Color,
    val cardStroke: Color,
    val cardHighlight: Color,
    val barFill: Color,
    val barStroke: Color,
    // 图表
    val chartColors: List<Color>,
    val corner: Dp,
    val elevation: Dp,
    val glass: Boolean,
)

private val MiuiPalette = OmniPalette(
    mode = ThemeMode.MIUIX_GLASS,
    style = SurfaceStyle.FROSTED,
    dark = false,
    bgTop = Color(0xFFE9F1FF),
    bgMid = Color(0xFFF6F0FF),
    bgBottom = Color(0xFFFFF3F0),
    blobA = Color(0x8C6FA8FF),
    blobB = Color(0x80B79CFF),
    blobC = Color(0x7AFFA8C0),
    accent = Color(0xFF2F6BFF),
    accentAlt = Color(0xFF7B5CFF),
    success = Color(0xFF1FAE7D),
    warning = Color(0xFFF0A24A),
    danger = Color(0xFFF2545B),
    textPrimary = Color(0xFF151821),
    textSecondary = Color(0xFF5A6172),
    textTertiary = Color(0xFF8B93A4),
    cardFill = Color(0x6BFFFFFF),
    cardStroke = Color(0xB3FFFFFF),
    cardHighlight = Color(0x8CFFFFFF),
    barFill = Color(0xA6FFFFFF),
    barStroke = Color(0xCCFFFFFF),
    chartColors = listOf(
        Color(0xFF2F6BFF), Color(0xFF7B5CFF), Color(0xFFFF8FA3),
        Color(0xFF3DC7A2), Color(0xFFFFC46B),
    ),
    corner = 24.dp,
    elevation = 10.dp,
    glass = true,
)

private val MorandiPalette = OmniPalette(
    mode = ThemeMode.MD3_MORANDI,
    style = SurfaceStyle.SOFT,
    dark = false,
    bgTop = Color(0xFFF4F0E8),
    bgMid = Color(0xFFEDE8DE),
    bgBottom = Color(0xFFE6E1D6),
    blobA = Color(0x4DA9B7A1),
    blobB = Color(0x4DD9C3B0),
    blobC = Color(0x40B9C6CB),
    accent = Color(0xFF687C66),
    accentAlt = Color(0xFFA9714B),
    success = Color(0xFF6F8F73),
    warning = Color(0xFFC08A4E),
    danger = Color(0xFFB5655C),
    textPrimary = Color(0xFF2B2A26),
    textSecondary = Color(0xFF5F5C55),
    textTertiary = Color(0xFF8D887E),
    cardFill = Color(0xFFFBF9F4),
    cardStroke = Color(0xFFE1DACB),
    cardHighlight = Color(0xFFFFFFFF),
    barFill = Color(0xF2FBF9F4),
    barStroke = Color(0xFFE1DACB),
    chartColors = listOf(
        Color(0xFF687C66), Color(0xFFA9714B), Color(0xFF8C9BA5),
        Color(0xFFC2B49A), Color(0xFF7D8F86),
    ),
    corner = 20.dp,
    elevation = 6.dp,
    glass = false,
)

private val CyberPalette = OmniPalette(
    mode = ThemeMode.CYBER_DARK,
    style = SurfaceStyle.NEON,
    dark = true,
    bgTop = Color(0xFF05060B),
    bgMid = Color(0xFF0A0F1C),
    bgBottom = Color(0xFF111A2E),
    blobA = Color(0x5900E5FF),
    blobB = Color(0x4DFF2D95),
    blobC = Color(0x596C4DFF),
    accent = Color(0xFF00E5FF),
    accentAlt = Color(0xFFFF2D95),
    success = Color(0xFF38F9A5),
    warning = Color(0xFFFFD166),
    danger = Color(0xFFFF5C7A),
    textPrimary = Color(0xFFE9F4FF),
    textSecondary = Color(0xFF93A7C0),
    textTertiary = Color(0xFF5E7189),
    cardFill = Color(0xB30E1626),
    cardStroke = Color(0x4700E5FF),
    cardHighlight = Color(0x14FFFFFF),
    barFill = Color(0xD90A1120),
    barStroke = Color(0x4000E5FF),
    chartColors = listOf(
        Color(0xFF00E5FF), Color(0xFFFF2D95), Color(0xFF7C4DFF),
        Color(0xFF38F9A5), Color(0xFFFFD166),
    ),
    corner = 18.dp,
    elevation = 0.dp,
    glass = false,
)

fun paletteFor(mode: ThemeMode): OmniPalette = when (mode) {
    ThemeMode.MIUIX_GLASS -> MiuiPalette
    ThemeMode.MD3_MORANDI -> MorandiPalette
    ThemeMode.CYBER_DARK -> CyberPalette
}

private fun colorSchemeFor(p: OmniPalette) = if (p.dark) {
    darkColorScheme(
        primary = p.accent,
        onPrimary = Color(0xFF04121A),
        primaryContainer = p.accent.copy(alpha = 0.22f),
        onPrimaryContainer = p.textPrimary,
        secondary = p.accentAlt,
        onSecondary = Color(0xFF12040A),
        secondaryContainer = p.accentAlt.copy(alpha = 0.20f),
        onSecondaryContainer = p.textPrimary,
        tertiary = p.success,
        background = p.bgMid,
        onBackground = p.textPrimary,
        surface = p.cardFill,
        onSurface = p.textPrimary,
        surfaceVariant = p.cardFill,
        onSurfaceVariant = p.textSecondary,
        outline = p.cardStroke,
        error = p.danger,
        onError = Color.White,
    )
} else {
    lightColorScheme(
        primary = p.accent,
        onPrimary = Color.White,
        primaryContainer = p.accent.copy(alpha = 0.14f),
        onPrimaryContainer = p.textPrimary,
        secondary = p.accentAlt,
        onSecondary = Color.White,
        secondaryContainer = p.accentAlt.copy(alpha = 0.14f),
        onSecondaryContainer = p.textPrimary,
        tertiary = p.success,
        background = p.bgMid,
        onBackground = p.textPrimary,
        surface = p.cardFill,
        onSurface = p.textPrimary,
        surfaceVariant = p.cardFill,
        onSurfaceVariant = p.textSecondary,
        outline = p.cardStroke,
        error = p.danger,
        onError = Color.White,
    )
}

private fun omniTypography(): Typography {
    val family = FontFamily.Default
    val base = Typography()
    return base.copy(
        displayLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-1).sp),
        headlineLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp),
        headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.2).sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
        titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
        labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp),
    )
}

private fun omniShapes(corner: Dp) = Shapes(
    extraSmall = RoundedCornerShape(corner * 0.4f),
    small = RoundedCornerShape(corner * 0.6f),
    medium = RoundedCornerShape(corner * 0.85f),
    large = RoundedCornerShape(corner),
    extraLarge = RoundedCornerShape(corner * 1.25f),
)

val LocalOmniPalette = staticCompositionLocalOf { MiuiPalette }
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.MIUIX_GLASS }

/** 便捷访问当前调色板。 */
object OmniTheme {
    val palette: OmniPalette
        @Composable get() = LocalOmniPalette.current

    val mode: ThemeMode
        @Composable get() = LocalThemeMode.current
}

@Composable
fun OmniHealthTheme(
    mode: ThemeMode = ThemeMode.MIUIX_GLASS,
    content: @Composable () -> Unit,
) {
    val palette = remember(mode) { paletteFor(mode) }
    val scheme = remember(mode) { colorSchemeFor(palette) }
    val typography = remember { omniTypography() }
    val shapes = remember(mode) { omniShapes(palette.corner) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !palette.dark
                isAppearanceLightNavigationBars = !palette.dark
            }
        }
    }

    CompositionLocalProvider(
        LocalOmniPalette provides palette,
        LocalThemeMode provides mode,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
