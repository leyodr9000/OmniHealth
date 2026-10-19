package com.example.omnihealth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.omnihealth.data.local.dao.AiReportDao
import com.example.omnihealth.data.local.dao.DietDao
import com.example.omnihealth.data.local.dao.HabitDao
import com.example.omnihealth.data.local.dao.StepDao
import com.example.omnihealth.data.local.dao.VitalDao
import com.example.omnihealth.data.local.entity.AiReportEntity
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.HabitEntity
import com.example.omnihealth.data.local.entity.HabitLogEntity
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.VitalEntity
import com.example.omnihealth.data.local.entity.WorkoutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VitalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        FoodEntity::class,
        DietLogEntity::class,
        StepEntity::class,
        WorkoutEntity::class,
        AiReportEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vitalDao(): VitalDao
    abstract fun habitDao(): HabitDao
    abstract fun dietDao(): DietDao
    abstract fun stepDao(): StepDao
    abstract fun aiReportDao(): AiReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun build(context: Context, scope: CoroutineScope): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omnihealth.db",
                )
                    .addCallback(FoodSeedCallback(context.applicationContext, scope))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        internal fun instance(): AppDatabase? = INSTANCE
    }
}

/**
 * 首次建库时预置食物字典（assets/foods.json，200+ 条）。
 * 由 Room 的 onCreate 触发，跑在 IO 线程，不阻塞首帧。
 */
private class FoodSeedCallback(
    private val context: Context,
    private val scope: CoroutineScope,
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val database = AppDatabase.instance() ?: return
        scope.launch(Dispatchers.IO) {
            runCatching { FoodSeeder.seed(context, database.dietDao()) }
        }
    }
}
