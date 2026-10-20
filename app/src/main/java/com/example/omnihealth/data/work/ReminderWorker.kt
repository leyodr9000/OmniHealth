package com.example.omnihealth.data.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import com.example.omnihealth.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * 提醒 Worker：饮水 / 睡眠两类，靠 WorkManager 周期调度。
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: KIND_WATER
        if (kind == KIND_WATER && !insideWaterWindow()) {
            // 静默跳过：不在用户设定的时段内，不打扰。
            return Result.success()
        }
        notify(applicationContext, kind)
        return Result.success()
    }

    private suspend fun insideWaterWindow(): Boolean {
        val settings = runCatching {
            com.example.omnihealth.data.datastore.SettingsPreferences(applicationContext)
                .settings.first()
        }.getOrNull() ?: return true
        return LocalTime.now().hour in settings.waterStartHour until settings.waterEndHour
    }

    private fun notify(context: Context, kind: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        // 文案走资源，跟随应用语言（英文用户不该收到中文通知）
        val (title, text) = when (kind) {
            KIND_SLEEP -> context.getString(R.string.reminder_sleep_notif_title) to
                context.getString(R.string.reminder_sleep_notif_body)
            KIND_MEAL_LOG -> context.getString(R.string.reminder_meal_notif_title) to
                context.getString(R.string.reminder_meal_notif_body)
            else -> context.getString(R.string.reminder_water_notif_title) to
                context.getString(R.string.reminder_water_notif_body)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching { manager.notify(kind.hashCode(), notification) }
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "健康提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "饮水与睡眠提醒"
            },
        )
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KIND_WATER = "water"
        const val KIND_SLEEP = "sleep"
        const val KIND_MEAL_LOG = "meal_log"
        private const val CHANNEL_ID = "omni_health_reminders"
    }
}

/**
 * 提醒调度器。饮水按 2 小时周期，睡眠固定在 22:30。
 */
object ReminderScheduler {

    private const val WATER_WORK = "reminder_water"
    private const val SLEEP_WORK = "reminder_sleep"
    private const val MEAL_LOG_WORK = "reminder_meal_log"

    /**
     * 饮水提醒：按用户设定的时段与间隔调度。
     *
     * 首个提醒对齐到时段起点（若当前已在时段内则等一个间隔），
     * 时段之外的触发由 Worker 自行跳过。
     */
    fun setWaterReminder(
        context: Context,
        enabled: Boolean,
        startHour: Int = 8,
        endHour: Int = 22,
        intervalHours: Int = 2,
    ) {
        val manager = WorkManager.getInstance(context)
        if (!enabled) {
            manager.cancelUniqueWork(WATER_WORK)
            return
        }
        val now = LocalDateTime.now()
        val inWindow = now.hour in startHour until endHour
        val delay = if (inWindow) {
            Duration.ofHours(intervalHours.toLong()).toMillis()
        } else {
            millisUntil(LocalTime.of(startHour.coerceIn(0, 23), 0))
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalHours.toLong(), TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(androidx.work.workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_WATER))
            .build()
        manager.enqueueUniquePeriodicWork(WATER_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun setSleepReminder(
        context: Context,
        enabled: Boolean,
        hour: Int = 22,
        minute: Int = 30,
    ) {
        val manager = WorkManager.getInstance(context)
        if (!enabled) {
            manager.cancelUniqueWork(SLEEP_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntil(LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))), TimeUnit.MILLISECONDS)
            .setInputData(androidx.work.workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_SLEEP))
            .build()
        manager.enqueueUniquePeriodicWork(SLEEP_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    /** 记录提醒：每天固定时刻提醒补记当天餐食。 */
    fun setMealLogReminder(context: Context, enabled: Boolean, hour: Int = 20) {
        val manager = WorkManager.getInstance(context)
        if (!enabled) {
            manager.cancelUniqueWork(MEAL_LOG_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntil(LocalTime.of(hour.coerceIn(0, 23), 0)), TimeUnit.MILLISECONDS)
            .setInputData(androidx.work.workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_MEAL_LOG))
            .build()
        manager.enqueueUniquePeriodicWork(MEAL_LOG_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun millisUntil(target: LocalTime): Long {
        val now = LocalDateTime.now()
        var next = now.with(target)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis().coerceAtLeast(60_000L)
    }
}
