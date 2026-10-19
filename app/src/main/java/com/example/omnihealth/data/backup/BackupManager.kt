package com.example.omnihealth.data.backup

import android.content.Context
import android.net.Uri
import com.example.omnihealth.data.local.AppDatabase
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.HabitEntity
import com.example.omnihealth.data.local.entity.HabitLogEntity
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.VitalEntity
import com.example.omnihealth.data.local.entity.WorkoutEntity
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class BackupBundle(
    val version: Int = 1,
    val createdAt: Long = 0L,
    val vitals: List<VitalEntity> = emptyList(),
    val dietLogs: List<DietLogEntity> = emptyList(),
    val steps: List<StepEntity> = emptyList(),
    val workouts: List<WorkoutEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
) {
    val totalRecords: Int
        get() = vitals.size + dietLogs.size + steps.size + workouts.size +
            habits.size + habitLogs.size
}

sealed interface RestoreResult {
    data class Success(val counts: Map<String, Int>) : RestoreResult

    data class Failure(val reason: String) : RestoreResult
}

/**
 * 完整备份与恢复。
 *
 * 与 CSV 导出的区别：备份保留主键与表间关系（habits ↔ habit_logs），可原样还原；
 * 恢复在单个 Room 事务内完成，失败整体回滚，不会留下半份数据。
 * 备份不含 API Key 与任何凭据。
 */
object BackupManager {

    private const val VERSION = 1
    private const val FILE_PREFIX = "omnihealth-backup-"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun export(context: Context, db: AppDatabase): File? = withContext(Dispatchers.IO) {
        val bundle = BackupBundle(
            version = VERSION,
            createdAt = System.currentTimeMillis(),
            vitals = db.vitalDao().all(),
            dietLogs = db.dietDao().allLogs(),
            steps = db.stepDao().allSteps(),
            workouts = db.stepDao().allWorkouts(),
            habits = db.habitDao().allHabits(),
            habitLogs = db.habitDao().logsBetween(0L, Long.MAX_VALUE),
        )
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(dir, "$FILE_PREFIX$stamp.json")
        runCatching { file.writeText(json.encodeToString(BackupBundle.serializer(), bundle)) }
            .map { file }
            .getOrNull()
    }

    /** 先解析并校验，再在事务里整体替换；任何一步失败都不改动现有数据。 */
    suspend fun restore(context: Context, db: AppDatabase, uri: Uri): RestoreResult =
        withContext(Dispatchers.IO) {
            val raw = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull() ?: return@withContext RestoreResult.Failure("cannot_read")

            val bundle = runCatching {
                json.decodeFromString(BackupBundle.serializer(), raw)
            }.getOrNull() ?: return@withContext RestoreResult.Failure("invalid_json")

            if (bundle.version > VERSION) {
                return@withContext RestoreResult.Failure("unsupported_version_${bundle.version}")
            }
            if (bundle.totalRecords == 0) {
                return@withContext RestoreResult.Failure("empty_backup")
            }

            val applied = runCatching {
                db.withTransaction {
                    db.vitalDao().clear()
                    db.dietDao().clearLogs()
                    db.stepDao().clearSteps()
                    db.stepDao().clearWorkouts()
                    db.habitDao().clearLogs()
                    db.habitDao().clearHabits()

                    db.vitalDao().insertAll(bundle.vitals)
                    db.dietDao().insertLogs(bundle.dietLogs)
                    db.stepDao().insertSteps(bundle.steps)
                    db.stepDao().insertWorkouts(bundle.workouts)
                    db.habitDao().insertHabits(bundle.habits)
                    db.habitDao().insertLogs(bundle.habitLogs)
                }
            }

            if (applied.isFailure) {
                return@withContext RestoreResult.Failure(applied.exceptionOrNull()?.message ?: "restore_failed")
            }

            RestoreResult.Success(
                mapOf(
                    "vitals" to bundle.vitals.size,
                    "diet" to bundle.dietLogs.size,
                    "steps" to bundle.steps.size,
                    "workouts" to bundle.workouts.size,
                    "habits" to bundle.habits.size,
                    "habitLogs" to bundle.habitLogs.size,
                ),
            )
        }

    /** 恢复前先读文件头，确认版本与记录数，避免误恢复。 */
    suspend fun inspect(context: Context, uri: Uri): BackupBundle? = withContext(Dispatchers.IO) {
        val raw = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: return@withContext null
        runCatching { json.decodeFromString(BackupBundle.serializer(), raw) }.getOrNull()
    }
}
