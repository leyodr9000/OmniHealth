package com.example.omnihealth.data.export

import android.content.Context
import com.example.omnihealth.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 把本地数据导出为 CSV（Excel 可直接打开）。
 *
 * 写到应用外部私有目录 `Android/data/<pkg>/files/exports/`，
 * 不需要存储权限，卸载即清理。
 */
object CsvExporter {

    suspend fun export(context: Context, db: AppDatabase): File? = withContext(Dispatchers.IO) {
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(dir, "omnihealth-$stamp.csv")

        val sb = StringBuilder()
        sb.appendLine("# OmniHealth export $stamp")

        sb.appendLine()
        sb.appendLine("# vitals")
        sb.appendLine("type,value,secondary,unit,recorded_at,note")
        db.vitalDao().allBetween(0L, Long.MAX_VALUE).forEach { v ->
            sb.appendLine(
                listOf(v.type, v.value, v.secondary ?: "", v.unit, v.recordedAt, escape(v.note))
                    .joinToString(","),
            )
        }

        sb.appendLine()
        sb.appendLine("# diet_logs")
        sb.appendLine("epoch_day,meal,food,grams,kcal,protein,carbs,fat,timestamp")
        db.dietDao().logsBetween(0L, Long.MAX_VALUE).forEach { d ->
            sb.appendLine(
                listOf(
                    d.epochDay, d.mealType, escape(d.foodName), d.grams,
                    d.calories, d.protein, d.carbs, d.fat, d.timestamp,
                ).joinToString(","),
            )
        }

        sb.appendLine()
        sb.appendLine("# steps")
        sb.appendLine("epoch_day,steps,goal,distance_m,active_min,kcal")
        db.stepDao().range(0L, Long.MAX_VALUE).forEach { s ->
            sb.appendLine(
                listOf(s.epochDay, s.steps, s.goal, s.distanceMeters, s.activeMinutes, s.calories)
                    .joinToString(","),
            )
        }

        sb.appendLine()
        sb.appendLine("# workouts")
        sb.appendLine("epoch_day,type,duration_sec,kcal,timestamp,note")
        db.stepDao().workoutsBetween(0L, Long.MAX_VALUE).forEach { w ->
            sb.appendLine(
                listOf(w.epochDay, w.type, w.durationSec, w.calories, w.timestamp, escape(w.note))
                    .joinToString(","),
            )
        }

        sb.appendLine()
        sb.appendLine("# habits")
        sb.appendLine("habit,emoji,target_per_day")
        db.habitDao().allHabits().forEach { h ->
            sb.appendLine(listOf(escape(h.name), h.emoji, h.targetPerDay).joinToString(","))
        }

        sb.appendLine()
        sb.appendLine("# habit_logs")
        sb.appendLine("epoch_day,habit_id,count,timestamp")
        db.habitDao().logsBetween(0L, Long.MAX_VALUE).forEach { l ->
            sb.appendLine(listOf(l.epochDay, l.habitId, l.count, l.timestamp).joinToString(","))
        }

        runCatching { file.writeText(sb.toString()) }.map { file }.getOrNull()
    }

    /**
     * CSV 字段转义 + 公式注入防护。
     *
     * 以 `= + - @`、制表符或回车开头的单元格会被 Excel/WPS 当成公式执行，
     * 这里统一在前面加单引号，使其按纯文本显示。
     */
    private fun escape(value: String): String {
        val guarded = if (value.isNotEmpty() && value[0] in FORMULA_PREFIXES) "'$value" else value
        return if (guarded.contains(',') || guarded.contains('"') || guarded.contains('\n')) {
            "\"${guarded.replace("\"", "\"\"")}\""
        } else {
            guarded
        }
    }

    private val FORMULA_PREFIXES = charArrayOf('=', '+', '-', '@', '\t', '\r')
}
