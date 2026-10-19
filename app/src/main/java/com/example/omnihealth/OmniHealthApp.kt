package com.example.omnihealth

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.example.omnihealth.data.datastore.SettingsPreferences
import com.example.omnihealth.data.local.AppDatabase
import com.example.omnihealth.data.repository.HealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 应用入口 + 手写依赖容器（无 DI 框架，保持构建简单）。
 *
 * 实现 [Configuration.Provider] 后，WorkManager 改为按需初始化：
 * 清单里移除了它的自动初始化器，冷启动不再额外拉起一个 ContentProvider。
 */
class OmniHealthApp : Application(), Configuration.Provider {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.build(this, applicationScope) }

    val preferences: SettingsPreferences by lazy { SettingsPreferences(this) }

    val repository: HealthRepository by lazy { HealthRepository(database, preferences) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OmniHealthApp
            private set
    }
}
