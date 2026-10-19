package com.example.omnihealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.ui.OmniApp
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.i18n.AppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.theme.OmniHealthTheme
import com.example.omnihealth.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as OmniHealthApp).repository

        setContent {
            val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val themeMode = remember(settings.themeId) { ThemeMode.from(settings.themeId) }
            val layoutMode = remember(settings.layoutId) { NavLayoutMode.from(settings.layoutId) }
            val language = remember(settings.languageId) { AppLanguage.from(settings.languageId) }
            val scope = rememberCoroutineScope()

            LocalizedApp(language = language) {
                OmniHealthTheme(mode = themeMode) {
                    CompositionLocalProvider(LocalHealthRepository provides repository) {
                        OmniApp(
                            themeMode = themeMode,
                            layoutMode = layoutMode,
                            language = language,
                            onThemeChange = { scope.launch { repository.setTheme(it.id) } },
                            onLayoutChange = { scope.launch { repository.setLayout(it.id) } },
                            onLanguageChange = { scope.launch { repository.setLanguage(it.id) } },
                        )
                    }
                }
            }
        }
    }
}
