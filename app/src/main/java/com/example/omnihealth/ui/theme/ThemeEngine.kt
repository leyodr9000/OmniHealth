package com.example.omnihealth.ui.theme

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    MONET(
        id = "monet",
        labelRes = R.string.theme_monet_label,
        emoji = "",
        taglineRes = R.string.theme_monet_tagline,
        blurbRes = R.string.theme_monet_blurb,
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
    val water: Color,
    val sleep: Color,
    val calorie: Color,
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


private fun colorSchemeFor(p: OmniPalette) = if (p.dark) {
    darkColorScheme(
        outlineVariant = p.cardStroke.copy(alpha = 0.6f),
        surfaceTint = p.accent,
        surfaceContainerLowest = p.bgBottom,
        surfaceContainerLow = p.bgMid,
        surfaceContainer = p.cardFill,
        surfaceContainerHigh = p.cardFill,
        surfaceContainerHighest = p.barFill,
        inverseSurface = p.textPrimary,
        inverseOnSurface = p.bgBottom,
        scrim = Color(0xFF000000),
        errorContainer = p.danger.copy(alpha = 0.18f),
        onErrorContainer = p.danger,
        tertiaryContainer = p.success.copy(alpha = 0.18f),
        onTertiaryContainer = p.success,

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
        outlineVariant = p.cardStroke.copy(alpha = 0.6f),
        surfaceTint = p.accent,
        surfaceContainerLowest = p.bgBottom,
        surfaceContainerLow = p.bgMid,
        surfaceContainer = p.cardFill,
        surfaceContainerHigh = p.cardFill,
        surfaceContainerHighest = p.barFill,
        inverseSurface = p.textPrimary,
        inverseOnSurface = p.bgBottom,
        scrim = Color(0xFF000000),
        errorContainer = p.danger.copy(alpha = 0.18f),
        onErrorContainer = p.danger,
        tertiaryContainer = p.success.copy(alpha = 0.18f),
        onTertiaryContainer = p.success,

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

/** 与白色混合，用于在暗背景下提亮品牌色。 */
private fun Color.lift(amount: Float): Color = Color(
    red = red + (1f - red) * amount,
    green = green + (1f - green) * amount,
    blue = blue + (1f - blue) * amount,
    alpha = alpha,
)

/**
 * 深色变体：只覆盖背景、文本、卡片与栏，品牌色做轻微提亮。
 * 集中在这里派生，避免三套配色各维护一份暗色硬编码。
 */
fun OmniPalette.toDark(): OmniPalette = copy(
    dark = true,
    bgTop = Color(0xFF0A0D14),
    bgMid = Color(0xFF0D1119),
    bgBottom = Color(0xFF080B10),
    blobA = blobA.copy(alpha = 0.16f),
    blobB = blobB.copy(alpha = 0.14f),
    blobC = blobC.copy(alpha = 0.12f),
    accent = accent.lift(0.12f),
    accentAlt = accentAlt.lift(0.10f),
    water = water.lift(0.12f),
    sleep = sleep.lift(0.12f),
    calorie = calorie.lift(0.10f),
    success = success.lift(0.10f),
    warning = warning.lift(0.08f),
    danger = danger.lift(0.08f),
    chartColors = chartColors.map { it.lift(0.12f) },
    textPrimary = Color(0xFFF1F4F9),
    textSecondary = Color(0xFFA7B2C4),
    textTertiary = Color(0xFF6B7686),
    cardFill = Color(0xFF161B24),
    cardStroke = Color(0xFF2A3341),
    cardHighlight = Color(0x14FFFFFF),
    barFill = Color(0xFF11161F),
    barStroke = Color(0xFF2A3341),
)

@Composable
fun OmniHealthTheme(
    mode: ThemeMode = ThemeMode.MIUIX_GLASS,
    framework: UiFramework = UiFramework.MATERIAL3,
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val palette = remember(mode, dark, context) {
        val fallback = paletteFor(mode)
        val themed = if (dark) fallback.toDark() else fallback
        if (mode == ThemeMode.MONET && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val dynamic = if (dark) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
            monetPalette(dynamic, themed)
        } else {
            themed
        }
    }
    val scheme = remember(palette) { colorSchemeFor(palette) }
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
