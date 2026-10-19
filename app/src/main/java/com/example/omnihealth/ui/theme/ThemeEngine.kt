package com.example.omnihealth.ui.theme

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
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
 * 界面框架：决定组件语言，而不是配色。
 *
 * [MATERIAL3] 默认。实心卡片、标准圆角、Material 列表。
 * [MIUIX]  HyperOS 风格：更大圆角、液态玻璃卡片与底栏、弹性按压。
 */
enum class UiFramework(
    val id: String,
    @StringRes val labelRes: Int,
) {
    MATERIAL3("material3", R.string.framework_md3),
    MIUIX("miuix", R.string.framework_miuix),
    ;

    companion object {
        fun from(id: String?): UiFramework = entries.firstOrNull { it.id == id } ?: MATERIAL3
    }
}

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
    bgTop = Color(0xFFF8F9FB),
    bgMid = Color(0xFFF4F6F9),
    bgBottom = Color(0xFFEFF2F6),
    blobA = Color(0x2E7FA8E8),
    blobB = Color(0x249BB8D9),
    blobC = Color(0x1FC9D6E8),
    accent = Color(0xFF2B6CF6),
    accentAlt = Color(0xFF5B6B82),
    success = Color(0xFF178F63),
    warning = Color(0xFFD98324),
    danger = Color(0xFFD64545),
    textPrimary = Color(0xFF14171C),
    textSecondary = Color(0xFF5A6270),
    textTertiary = Color(0xFF8D94A0),
    cardFill = Color(0xFFFFFFFF),
    cardStroke = Color(0xFFE5E8EE),
    cardHighlight = Color(0xFFFFFFFF),
    barFill = Color(0xF7FFFFFF),
    barStroke = Color(0xFFE5E8EE),
    chartColors = listOf(
        Color(0xFF2B6CF6), Color(0xFF5B6B82), Color(0xFFD98324),
        Color(0xFF178F63), Color(0xFF8D94A0),
    ),
    corner = 18.dp,
    elevation = 2.dp,
    glass = true,
)

private val MorandiPalette = OmniPalette(
    mode = ThemeMode.MD3_MORANDI,
    style = SurfaceStyle.SOFT,
    dark = false,
    bgTop = Color(0xFFF4F1EA),
    bgMid = Color(0xFFEFEBE3),
    bgBottom = Color(0xFFE9E4DB),
    blobA = Color(0x26A9B7A1),
    blobB = Color(0x21D9C3B0),
    blobC = Color(0x1CB9C6CB),
    accent = Color(0xFF5E7359),
    accentAlt = Color(0xFF8A6A4F),
    success = Color(0xFF5E7A62),
    warning = Color(0xFFB07C42),
    danger = Color(0xFFA85B52),
    textPrimary = Color(0xFF26251F),
    textSecondary = Color(0xFF5C594F),
    textTertiary = Color(0xFF8B8578),
    cardFill = Color(0xFFFCFBF8),
    cardStroke = Color(0xFFE4DDD0),
    cardHighlight = Color(0xFFFFFFFF),
    barFill = Color(0xF7FCFBF8),
    barStroke = Color(0xFFE4DDD0),
    chartColors = listOf(
        Color(0xFF5E7359), Color(0xFF8A6A4F), Color(0xFF7C8B93),
        Color(0xFFB8A98F), Color(0xFF6E7D74),
    ),
    corner = 16.dp,
    elevation = 2.dp,
    glass = false,
)

private val CyberPalette = OmniPalette(
    mode = ThemeMode.CYBER_DARK,
    style = SurfaceStyle.NEON,
    dark = true,
    bgTop = Color(0xFF07080C),
    bgMid = Color(0xFF0A0E16),
    bgBottom = Color(0xFF0E1420),
    blobA = Color(0x2600C2D6),
    blobB = Color(0x1FFF2D95),
    blobC = Color(0x266C4DFF),
    accent = Color(0xFF00C2D6),
    accentAlt = Color(0xFF9BA9C0),
    success = Color(0xFF2FBF8F),
    warning = Color(0xFFD9A441),
    danger = Color(0xFFE05572),
    textPrimary = Color(0xFFE6EDF7),
    textSecondary = Color(0xFF9AA8BC),
    textTertiary = Color(0xFF6C7A8E),
    cardFill = Color(0xFF121926),
    cardStroke = Color(0xFF243044),
    cardHighlight = Color(0xFFFFFFFF),
    barFill = Color(0xF70D131E),
    barStroke = Color(0xFF243044),
    chartColors = listOf(
        Color(0xFF00C2D6), Color(0xFFE05572), Color(0xFF6C4DFF),
        Color(0xFF2FBF8F), Color(0xFFD9A441),
    ),
    corner = 16.dp,
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
val LocalUiFramework = staticCompositionLocalOf { UiFramework.MATERIAL3 }

/** 便捷访问当前调色板。 */
object OmniTheme {
    val palette: OmniPalette
        @Composable get() = LocalOmniPalette.current

    val mode: ThemeMode
        @Composable get() = LocalThemeMode.current

    val framework: UiFramework
        @Composable get() = LocalUiFramework.current
}

@Composable
fun OmniHealthTheme(
    mode: ThemeMode = ThemeMode.MIUIX_GLASS,
    framework: UiFramework = UiFramework.MATERIAL3,
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
        LocalUiFramework provides framework,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = shapes,
        ) {
            // 全站数字使用等宽字形（tnum），大数字与单位对齐不再跳动。
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                    fontFeatureSettings = "tnum",
                ),
                content = content,
            )
        }
    }
}
