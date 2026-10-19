package com.example.omnihealth.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.ThemeMode
import com.example.omnihealth.ui.theme.UiFramework

@Composable
fun OmniApp(
    themeMode: ThemeMode,
    layoutMode: NavLayoutMode,
    language: AppLanguage,
    framework: UiFramework,
    onThemeChange: (ThemeMode) -> Unit,
    onLayoutChange: (NavLayoutMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFrameworkChange: (UiFramework) -> Unit,
) {
    val navController = rememberNavController()
    val palette = OmniTheme.palette
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var quickLogOpen by remember { mutableStateOf(false) }

    MeshGradientBackground(palette = palette) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AdaptiveBottomBar(
                    layout = layoutMode,
                    currentRoute = currentRoute,
                    onSelect = { dest -> navController.navigateSingleTop(dest) },
                    onQuickLog = { quickLogOpen = true },
                )
            },
        ) { innerPadding ->
            // 悬浮胶囊（布局 C）与中央 FAB（布局 A）都会向上溢出底栏自身高度，
            // 这里按布局补足余量，避免最后一行内容被压住。
            val extraBottom = when (layoutMode) {
                NavLayoutMode.A_FAB -> 6.dp
                NavLayoutMode.B_FIVE -> 2.dp
                NavLayoutMode.C_BENTO -> 16.dp
            }
            NavHost(
                navController = navController,
                startDestination = OmniDest.Home.route,
                // 底部 Tab 之间不做转场动画：动画期间新页首帧同时合成，
                // 切完立刻滑动会明显掉帧；国内 App 的 Tab 切换也基本都是瞬切。
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding() + extraBottom),
            ) {
                composable(OmniDest.Home.route) {
                    HomeScreen(
                        onOpenSettings = { navController.navigateSingleTop(OmniDest.Settings) },
                        onOpenDiet = { navController.navigateSingleTop(OmniDest.Diet) },
                        onOpenStats = { navController.navigateSingleTop(OmniDest.Stats) },
                        onOpenAi = { navController.navigateSingleTop(OmniDest.Ai) },
                        onQuickLog = { quickLogOpen = true },
                    )
                }
                composable(OmniDest.Diet.route) {
                    DietSportScreen(
                        initialTab = 0,
                        onQuickLog = { quickLogOpen = true },
                        onOpenSport = { navController.navigateSingleTop(OmniDest.Sport) },
                    )
                }
                composable(OmniDest.Sport.route) {
                    DietSportScreen(
                        initialTab = 1,
                        onQuickLog = { quickLogOpen = true },
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
                        onThemeChange = onThemeChange,
                        onLayoutChange = onLayoutChange,
                        onLanguageChange = onLanguageChange,
                        onFrameworkChange = onFrameworkChange,
                    )
                }
            }
        }
    }

    if (quickLogOpen) {
        val repository = LocalHealthRepository.current
        val scope = rememberCoroutineScope()
        QuickLogBottomSheet(
            onDismiss = { quickLogOpen = false },
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
                quickLogOpen = false
            },
        )
    }
}

private fun NavHostController.navigateSingleTop(dest: OmniDest) {
    navigate(dest.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
