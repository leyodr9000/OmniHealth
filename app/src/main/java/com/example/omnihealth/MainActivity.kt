package com.example.omnihealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.omnihealth.ui.OmniApp
import com.example.omnihealth.ui.i18n.AppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.theme.OmniHealthTheme
import com.example.omnihealth.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // M0 骨架阶段：主题 / 布局 / 语言状态暂由 Compose 内存持有，M8 接入 DataStore 持久化。
            var themeMode by remember { mutableStateOf(ThemeMode.MIUIX_GLASS) }
            var layoutMode by remember { mutableStateOf(NavLayoutMode.A_FAB) }
            var language by remember { mutableStateOf(AppLanguage.SYSTEM) }

            LocalizedApp(language = language) {
                OmniHealthTheme(mode = themeMode) {
                    OmniApp(
                        themeMode = themeMode,
                        layoutMode = layoutMode,
                        language = language,
                        onThemeChange = { themeMode = it },
                        onLayoutChange = { layoutMode = it },
                        onLanguageChange = { language = it },
                    )
                }
            }
        }
    }
}
