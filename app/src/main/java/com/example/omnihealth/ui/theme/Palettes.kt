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

/* 调色板定义与派生：三套静态配色 + 莫奈动态色映射 + 深色变体。 */

internal val MiuiPalette = OmniPalette(
    mode = ThemeMode.MIUIX_GLASS,
    style = SurfaceStyle.FROSTED,
    dark = false,
    bgTop = Color(0xFFF8F9FB),
    bgMid = Color(0xFFF4F6F9),
    bgBottom = Color(0xFFEFF2F6),
    blobA = Color(0x2E7FA8E8),
    blobB = Color(0x249BB8D9),
    blobC = Color(0x1FC9D6E8),
    accent = Color(0xFF5E6B76),
    accentAlt = Color(0xFF8A7F72),
    water = Color(0xFF4FB3FF),
    sleep = Color(0xFF8B7CFF),
    calorie = Color(0xFFFF8A5B),
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

internal val MorandiPalette = OmniPalette(
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
    water = Color(0xFF4FB3FF),
    sleep = Color(0xFF8B7CFF),
    calorie = Color(0xFFFF8A5B),
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

internal val CyberPalette = OmniPalette(
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
    water = Color(0xFF4FB3FF),
    sleep = Color(0xFF8B7CFF),
    calorie = Color(0xFFFF8A5B),
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

/**
 * 莫奈色：优先跟随系统壁纸取色（Android 12+），不支持时退化为低饱和青灰。
 *
 * 只取壁纸里的主色/次色与表面色，语义色（饮水/睡眠/热量/成功/警告）仍用固定值，
 * 避免壁纸偏色把"饮水蓝"染成别的颜色，导致含义漂移。
 */
fun monetPalette(scheme: ColorScheme, fallback: OmniPalette): OmniPalette = fallback.copy(
    mode = ThemeMode.MONET,
    dark = fallback.dark,
    bgTop = scheme.background,
    bgMid = scheme.background,
    bgBottom = scheme.surfaceContainerLowest,
    blobA = scheme.primary.copy(alpha = 0.10f),
    blobB = scheme.secondary.copy(alpha = 0.08f),
    blobC = scheme.tertiary.copy(alpha = 0.06f),
    accent = scheme.primary,
    accentAlt = scheme.secondary,
    textPrimary = scheme.onBackground,
    textSecondary = scheme.onSurfaceVariant,
    textTertiary = scheme.onSurfaceVariant.copy(alpha = 0.7f),
    cardFill = scheme.surfaceContainer,
    cardStroke = scheme.outlineVariant,
    cardHighlight = scheme.surfaceContainerHigh,
    barFill = scheme.surfaceContainerHigh,
    barStroke = scheme.outlineVariant,
    chartColors = listOf(scheme.primary, scheme.secondary, scheme.tertiary, fallback.water, fallback.calorie),
)

/** 不支持动态取色时的莫奈兜底：低饱和、偏暖，避开"AI 蓝"。 */
internal val MonetFallbackPalette = MiuiPalette.copy(
    mode = ThemeMode.MONET,
    accent = Color(0xFF6E7F72),
    accentAlt = Color(0xFF9A8574),
    bgTop = Color(0xFFF3F2EE),
    bgMid = Color(0xFFEFEEE9),
    bgBottom = Color(0xFFE9E7E0),
)

fun paletteFor(mode: ThemeMode): OmniPalette = when (mode) {
    ThemeMode.MIUIX_GLASS -> MiuiPalette
    ThemeMode.MD3_MORANDI -> MorandiPalette
    ThemeMode.MONET -> MonetFallbackPalette
    ThemeMode.CYBER_DARK -> CyberPalette
}
