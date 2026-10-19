package com.example.omnihealth.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.ui.theme.OmniPalette
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.SurfaceStyle
import com.example.omnihealth.ui.theme.UiFramework
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.frostBar
import com.example.omnihealth.ui.theme.paletteFor

/** 应用内所有目的地。 */
enum class OmniDest(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Rounded.Home),
    Diet("diet", R.string.nav_diet, Icons.Rounded.Restaurant),
    Sport("sport", R.string.nav_sport, Icons.Rounded.DirectionsRun),
    Stats("stats", R.string.nav_stats, Icons.Rounded.BarChart),
    Ai("ai", R.string.nav_ai, Icons.Rounded.AutoAwesome),
    Settings("settings", R.string.nav_settings, Icons.Rounded.Person),
    ;

    companion object {
        fun itemsFor(layout: NavLayoutMode): List<OmniDest> = when (layout) {
            NavLayoutMode.A_FAB -> listOf(Home, Diet, Stats, Ai)
            NavLayoutMode.B_FIVE -> listOf(Home, Diet, Sport, Stats, Ai)
            NavLayoutMode.C_BENTO -> listOf(Home, Stats, Settings)
        }

        fun fromRoute(route: String?): OmniDest? = entries.firstOrNull { it.route == route }
    }
}

/** 三套底部导航骨架。 */
enum class NavLayoutMode(
    val id: String,
    @StringRes val labelRes: Int,
    @StringRes val descRes: Int,
) {
    A_FAB("layout_a", R.string.layout_a_label, R.string.layout_a_desc),
    B_FIVE("layout_b", R.string.layout_b_label, R.string.layout_b_desc),
    C_BENTO("layout_c", R.string.layout_c_label, R.string.layout_c_desc),
    ;

    companion object {
        fun from(id: String?): NavLayoutMode = entries.firstOrNull { it.id == id } ?: A_FAB
    }
}

@Composable
fun AdaptiveBottomBar(
    layout: NavLayoutMode,
    currentRoute: String?,
    onSelect: (OmniDest) -> Unit,
    onQuickLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OmniTheme.palette
    val items = remember(layout) { OmniDest.itemsFor(layout) }

    // MIUIX 框架：底栏交给 miuix 的 NavigationBar（HyperOS 的指示器、间距与按压反馈）。
    if (OmniTheme.framework == UiFramework.MIUIX) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
            NavigationBar(
                items.map { NavigationItem(stringResource(it.labelRes), it.icon) },
                selectedIndex,
                { index -> items.getOrNull(index)?.let(onSelect) },
                Modifier.fillMaxWidth(),
            )
            if (layout == NavLayoutMode.A_FAB) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-24).dp)
                        .size(56.dp)
                        .background(color = palette.accent, shape = CircleShape)
                        .bounceClick(pressScale = 0.88f, onClick = onQuickLog),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.nav_quick_log),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (layout) {
            NavLayoutMode.A_FAB -> FabNavBar(items, currentRoute, onSelect, onQuickLog, palette)
            NavLayoutMode.B_FIVE -> WideNavBar(items, currentRoute, onSelect, palette)
            NavLayoutMode.C_BENTO -> PillNavBar(items, currentRoute, onSelect, palette)
        }
    }
}

/* ------------------------------------------------------------------ */
/* Layout A · 4 Tab + 中央 FAB                                          */
/* ------------------------------------------------------------------ */

@Composable
private fun FabNavBar(
    items: List<OmniDest>,
    currentRoute: String?,
    onSelect: (OmniDest) -> Unit,
    onQuickLog: () -> Unit,
    palette: OmniPalette,
) {
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .frostBar(palette, RoundedCornerShape(28.dp))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.take(2).forEach { dest ->
                NavItem(dest, currentRoute == dest.route, palette, Modifier.weight(1f)) { onSelect(dest) }
            }
            Spacer(Modifier.width(72.dp))
            items.drop(2).take(2).forEach { dest ->
                NavItem(dest, currentRoute == dest.route, palette, Modifier.weight(1f)) { onSelect(dest) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(58.dp)
                .background(
                    color = palette.accent,
                    shape = CircleShape,
                )
                .bounceClick(pressScale = 0.88f, onClick = onQuickLog),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.nav_quick_log),
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Layout B · 5 Tab                                                    */
/* ------------------------------------------------------------------ */

@Composable
private fun WideNavBar(
    items: List<OmniDest>,
    currentRoute: String?,
    onSelect: (OmniDest) -> Unit,
    palette: OmniPalette,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .frostBar(palette, RoundedCornerShape(26.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { dest ->
            NavItem(dest, currentRoute == dest.route, palette, Modifier.weight(1f)) { onSelect(dest) }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Layout C · 3 Tab 胶囊                                                */
/* ------------------------------------------------------------------ */

@Composable
private fun PillNavBar(
    items: List<OmniDest>,
    currentRoute: String?,
    onSelect: (OmniDest) -> Unit,
    palette: OmniPalette,
) {
    Row(
        modifier = Modifier
            .width(280.dp)
            .height(64.dp)
            .frostBar(palette, RoundedCornerShape(32.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { dest ->
            NavItem(dest, currentRoute == dest.route, palette, Modifier.weight(1f)) { onSelect(dest) }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 单个导航项                                                           */
/* ------------------------------------------------------------------ */

@Composable
private fun NavItem(
    dest: OmniDest,
    selected: Boolean,
    palette: OmniPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tint = if (selected) palette.accent else palette.textTertiary
    val pillColor = palette.accent.copy(alpha = if (palette.style == SurfaceStyle.NEON) 0.18f else 0.12f)
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.5f),
        label = "navIconScale",
    )

    Column(
        modifier = modifier
            .bounceClick(pressScale = 0.9f, haptics = false, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) pillColor else Color.Transparent,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = dest.icon,
                contentDescription = stringResource(dest.labelRes),
                tint = tint,
                modifier = Modifier.size((21 * iconScale).dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(dest.labelRes),
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
