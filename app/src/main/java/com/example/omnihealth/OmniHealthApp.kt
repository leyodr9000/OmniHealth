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
import kotlinx.coroutines.launch

/**
 * 应用入口 + 手写依赖容器（无 DI 框架，保持构建简单）。
 *
 * 实现 [Configuration.Provider] 后，WorkManager 改为按需初始化：
 * 清单里移除了它的自动初始化器，冷启动不再额外拉起一个 ContentProvider。
 */
class OmniHealthApp : Application(), Configuration.Provider {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 数据库打开失败时记录原因；UI 据此给出可操作提示，而不是闪退。 */
    var databaseError: Throwable? = null

    val database: AppDatabase? by lazy {
        runCatching { AppDatabase.build(this, applicationScope) }
            .onFailure {
                databaseError = it
                Log.e("OmniHealth", "database open failed", it)
            }
            .getOrNull()
    }

    val preferences: SettingsPreferences by lazy { SettingsPreferences(this) }

    val repository: HealthRepository? by lazy {
        database?.let { HealthRepository(it, preferences) }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 增量补库：老用户升级后也能搜到新加的食物
        applicationScope.launch {
            runCatching {
                database?.let { db ->
                    com.example.omnihealth.data.local.FoodSeeder.sync(this@OmniHealthApp, db.dietDao())
                    com.example.omnihealth.widget.TodayWidgetProvider.refresh(this@OmniHealthApp)
                }
            }
        }
    }

    companion object {
        lateinit var instance: OmniHealthApp
            private set
    }
}
