package com.example.omnihealth.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.omnihealth.MainActivity
import com.example.omnihealth.R
import com.example.omnihealth.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * 桌面小组件：今天记录了多少、还剩多少。
 *
 * 只读一次数据库的一个聚合值，不做任何网络与图片处理，避免拖慢桌面刷新。
 */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val text = runCatching { loadToday(appContext) }.getOrNull()
            runCatching {
                appWidgetIds.forEach { id ->
                    appWidgetManager.updateAppWidget(id, build(appContext, text))
                }
            }
            pending.finish()
        }
    }

    private suspend fun loadToday(context: Context): Pair<Int, Int>? {
        val db = AppDatabase.instance() ?: return null
        val today = com.example.omnihealth.domain.util.TimeUtils.epochDayNow()
        val kcal = db.dietDao().caloriesBetween(today, today)?.toInt() ?: 0
        return kcal to 0
    }

    private fun build(context: Context, today: Pair<Int, Int>?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_today)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_calories, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_detail, pendingIntent)

        if (today == null) {
            views.setTextViewText(R.id.widget_calories, "—")
            views.setTextViewText(R.id.widget_detail, context.getString(R.string.widget_hint))
        } else {
            views.setTextViewText(R.id.widget_calories, "${today.first}")
            views.setTextViewText(R.id.widget_detail, context.getString(R.string.widget_logged))
        }
        return views
    }

    companion object {
        /** 记录变化后刷新桌面，避免显示过期数据。 */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, TodayWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                },
            )
        }
    }
}
