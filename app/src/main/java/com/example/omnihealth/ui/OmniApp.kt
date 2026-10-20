package com.example.omnihealth.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.omnihealth.ui.components.QuickLogBottomSheet
import com.example.omnihealth.ui.components.QuickLogType
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.i18n.AppLanguage
import kotlinx.coroutines.launch
import com.example.omnihealth.ui.navigation.AdaptiveBottomBar
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.navigation.OmniDest
import com.example.omnihealth.ui.screens.ai_advisor.AiAdvisorScreen
import com.example.omnihealth.ui.screens.diet_sport.DietSportScreen
import com.example.omnihealth.ui.screens.home.HomeScreen
import com.example.omnihealth.ui.screens.settings.SettingsScreen
import com.example.omnihealth.ui.screens.stats.ChartAnalyticsScreen
import com.example.omnihealth.ui.theme.MeshGradientBackground
import com.example.omnihealth.ui.theme.rememberHazeState
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.ThemeMode
import com.example.omnihealth.ui.theme.UiFramework
import com.example.omnihealth.ui.theme.LocalLiquidGlass

@Composable
fun OmniApp(
    reduceMotion: Boolean,
    themeMode: ThemeMode,
    layoutMode: NavLayoutMode,
    language: AppLanguage,
    framework: UiFramework,
    darkModeId: String,
    onThemeChange: (ThemeMode) -> Unit,
    onLayoutChange: (NavLayoutMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFrameworkChange: (UiFramework) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
) {
    val navController = rememberNavController()
    val palette = OmniTheme.palette
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var quickLogType by remember { mutableStateOf<QuickLogType?>(null) }
    val hazeState = rememberHazeState()
    val liquid = framework == UiFramework.MIUIX

    CompositionLocalProvider(LocalLiquidGlass provides if (liquid) hazeState else null) {
    MeshGradientBackground(
        palette = palette,
        modifier = if (liquid) Modifier.hazeSource(hazeState) else Modifier,
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AdaptiveBottomBar(
                    layout = layoutMode,
                    currentRoute = currentRoute,
                    hazeState = hazeState,
                    onSelect = { dest -> navController.navigateSingleTop(dest) },
                    liquid = liquid,
                    onQuickLog = { quickLogType = QuickLogType.WATER },
                )
            },
        ) { innerPadding ->
            // 底部安全区只由 Scaffold 计算一次：页面不再自行加底部补偿，
            // 避免出现「谁的 padding 生效」这类边界错位。
            // 底部安全区只由 Scaffold 计算一次，页面不再自行补偿。
            NavHost(
                navController = navController,
                startDestination = OmniDest.Home.route,
                // 底栏左右切换：纯水平位移，不做透明度过渡。
                // 淡入淡出会让两个页面在半透明状态下重叠（重影），位移则是一进一出、互不干扰。
                enterTransition = if (reduceMotion) {
                    { EnterTransition.None }
                } else {
                    {
                        val forward = pageIndex(targetState.destination.route) >=
                            pageIndex(initialState.destination.route)
                        slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { full ->
                            if (forward) full else -full
                        }
                    }
                },
                exitTransition = if (reduceMotion) {
                    { ExitTransition.None }
                } else {
                    {
                        val forward = pageIndex(targetState.destination.route) >=
                            pageIndex(initialState.destination.route)
                        // 旧页面同样整屏滑出：两页并排像翻页，不会出现「新页盖在旧页上」的重叠。
                        slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { full ->
                            if (forward) -full else full
                        }
                    }
                },
                popEnterTransition = if (reduceMotion) {
                    { EnterTransition.None }
                } else {
                    {
                        val forward = pageIndex(targetState.destination.route) >=
                            pageIndex(initialState.destination.route)
                        slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { full ->
                            if (forward) full else -full
                        }
                    }
                },
                popExitTransition = if (reduceMotion) {
                    { ExitTransition.None }
                } else {
                    {
                        val forward = pageIndex(targetState.destination.route) >=
                            pageIndex(initialState.destination.route)
                        slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { full ->
                            if (forward) -full else full
                        }
                    }
                },
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            ) {
                composable(OmniDest.Home.route) {
                    HomeScreen(
                        onOpenSettings = { navController.navigateSingleTop(OmniDest.Settings) },
                        onOpenDiet = { navController.navigateSingleTop(OmniDest.Diet) },
                        onOpenStats = { navController.navigateSingleTop(OmniDest.Stats) },
                        onOpenAi = { navController.navigateSingleTop(OmniDest.Ai) },
                        onQuickLog = { type -> quickLogType = type },
                    )
                }
                composable(OmniDest.Diet.route) {
                    DietSportScreen(
                        initialTab = 0,
                        onQuickLog = { type -> quickLogType = type },
                        onOpenSport = { navController.navigateSingleTop(OmniDest.Sport) },
                    )
                }
                composable(OmniDest.Sport.route) {
                    DietSportScreen(
                        initialTab = 1,
                        onQuickLog = { type -> quickLogType = type },
                        onOpenSport = {},
                    )
                }
                composable(OmniDest.Stats.route) { ChartAnalyticsScreen() }
                composable(OmniDest.Ai.route) { AiAdvisorScreen() }
                composable(OmniDest.Settings.route) {
                    SettingsScreen(
                        themeMode = themeMode,
                        layoutMode = layoutMode,
                        language = language,
                        framework = framework,
                        darkModeId = darkModeId,
                        onThemeChange = onThemeChange,
                        onLayoutChange = onLayoutChange,
                        onLanguageChange = onLanguageChange,
                        onFrameworkChange = onFrameworkChange,
                        onDarkModeChange = onDarkModeChange,
                        onOpenAi = { navController.navigateSingleTop(OmniDest.Ai) },
                        reduceMotion = reduceMotion,
                        onReduceMotionChange = onReduceMotionChange,
                    )
                }
            }
        }
    }
    }

    quickLogType?.let { initialType ->
        val repository = LocalHealthRepository.current
        val scope = rememberCoroutineScope()
        QuickLogBottomSheet(
            initialType = initialType,
            onDismiss = { quickLogType = null },
            onSave = { type, value ->
                scope.launch {
                    when (type) {
                        QuickLogType.WEIGHT -> repository.logWeight(value)
                        QuickLogType.WATER -> repository.logWater(value)
                        QuickLogType.SLEEP -> repository.logSleep(value)
                        QuickLogType.STEPS -> repository.saveSteps(value.toInt())
                        QuickLogType.MOOD -> repository.logMood(value)
                    }
                }
                quickLogType = null
            },
        )
    }
}


/** Tab 顺序，用于判断切换方向（右移还是左移）。 */
private fun pageIndex(route: String?): Int {
    val index = OmniDest.entries.indexOfFirst { it.route == route }
    return if (index < 0) 0 else index
}

private fun NavHostController.navigateSingleTop(dest: OmniDest) {
    navigate(dest.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
