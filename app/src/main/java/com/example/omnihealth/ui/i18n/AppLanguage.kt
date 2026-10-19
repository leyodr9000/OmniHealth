package com.example.omnihealth.ui.i18n

import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.omnihealth.R
import java.util.Locale

/**
 * 应用语言。
 *
 * [SYSTEM] 跟随系统；[ZH] / [EN] 强制覆盖，并通过重建 Configuration 让
 * `stringResource()` 立即按新语言解析，无需重启 Activity。
 */
enum class AppLanguage(
    val id: String,
    @StringRes val labelRes: Int,
    val locale: Locale?,
) {
    SYSTEM("system", R.string.language_system, null),
    ZH("zh", R.string.language_zh, Locale.SIMPLIFIED_CHINESE),
    EN("en", R.string.language_en, Locale.ENGLISH),
    ;

    companion object {
        fun from(id: String?): AppLanguage = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

/**
 * 用目标语言包裹整棵 Compose 树。
 *
 * 实现要点：同时覆盖 `LocalContext`（提供已本地化的 Resources）与
 * `LocalConfiguration`（让 Compose 感知语言变化并触发重组）。
 */
@Composable
fun LocalizedApp(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppLanguage provides language) {
        val locale = language.locale
        if (locale == null) {
            content()
            return@CompositionLocalProvider
        }

        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        // 覆盖 LocalContext 会切断 Compose 从 Context 推导出的宿主对象，
        // 这里把它们原样转发，避免 ActivityResult / 返回键失效。
        val resultRegistryOwner = LocalActivityResultRegistryOwner.current
        val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

        val localizedContext = remember(locale, configuration) {
            val config = Configuration(configuration).apply { setLocale(locale) }
            context.createConfigurationContext(config)
        }
        val localizedConfiguration = remember(locale, configuration) {
            Configuration(configuration).apply { setLocale(locale) }
        }

        SideEffect { Locale.setDefault(locale) }

        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration,
        ) {
            if (resultRegistryOwner != null && backDispatcherOwner != null) {
                CompositionLocalProvider(
                    LocalActivityResultRegistryOwner provides resultRegistryOwner,
                    LocalOnBackPressedDispatcherOwner provides backDispatcherOwner,
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

/**
 * 当前语言。
 *
 * 底部弹窗 / Dialog 会创建独立的窗口上下文，`stringResource` 在其中按系统语言解析，
 * 因此弹窗内部需要用 `LocalizedApp(LocalAppLanguage.current)` 再包一层。
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.SYSTEM }
