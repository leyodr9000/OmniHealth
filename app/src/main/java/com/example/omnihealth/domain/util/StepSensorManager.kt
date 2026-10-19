package com.example.omnihealth.domain.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 计步传感器封装。
 *
 * [Sensor.TYPE_STEP_COUNTER] 返回的是「开机以来的累计步数」，因此需要用
 * 「当天基线」做差：跨天时把基线重置为当前读数。
 *
 * 模拟器 / 无传感器设备可调用 [simulate] 手动加步，保证演示可用。
 */
class StepSensorManager(private val context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounter: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _todaySteps = MutableStateFlow(prefs.getInt(KEY_STEPS, 0))
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    private val _sensorActive = MutableStateFlow(false)
    val sensorActive: StateFlow<Boolean> = _sensorActive.asStateFlow()

    val hasSensor: Boolean get() = stepCounter != null

    private var baselineRaw: Long = prefs.getLong(KEY_BASELINE, -1L)
    private var baselineDay: Long = prefs.getLong(KEY_DAY, -1L)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val raw = event?.values?.firstOrNull()?.toLong() ?: return
            consumeRaw(raw)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun hasActivityPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) == PackageManager.PERMISSION_GRANTED

    fun start() {
        val manager = sensorManager ?: return
        val sensor = stepCounter ?: return
        if (!hasActivityPermission()) return
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        _sensorActive.value = true
    }

    fun stop() {
        sensorManager?.unregisterListener(listener)
        _sensorActive.value = false
    }

    /** 累计读数 → 今日步数（跨天自动重置基线）。 */
    private fun consumeRaw(raw: Long) {
        val today = TimeUtils.epochDayNow()
        if (baselineDay != today || baselineRaw < 0L || raw < baselineRaw) {
            baselineDay = today
            baselineRaw = raw
            prefs.edit().putLong(KEY_DAY, today).putLong(KEY_BASELINE, raw).apply()
            persist(0)
            return
        }
        val steps = (raw - baselineRaw).toInt().coerceAtLeast(0)
        persist(steps)
    }

    /** 模拟器补步（也用于手动校准）。 */
    fun simulate(extraSteps: Int) {
        if (extraSteps == 0) return
        _todaySteps.update { (it + extraSteps).coerceAtLeast(0) }
        prefs.edit().putInt(KEY_STEPS, _todaySteps.value).apply()
    }

    /** 用数据库值回填（重启后恢复）。 */
    fun restore(steps: Int) {
        if (steps > _todaySteps.value) {
            _todaySteps.value = steps
            prefs.edit().putInt(KEY_STEPS, steps).apply()
        }
    }

    fun resetToday() {
        _todaySteps.value = 0
        prefs.edit().putInt(KEY_STEPS, 0).apply()
        baselineRaw = -1L
        baselineDay = -1L
    }

    private fun persist(steps: Int) {
        _todaySteps.value = steps
        prefs.edit().putInt(KEY_STEPS, steps).apply()
    }

    private companion object {
        const val PREFS_NAME = "omni_step_sensor"
        const val KEY_BASELINE = "baseline_raw"
        const val KEY_DAY = "baseline_day"
        const val KEY_STEPS = "today_steps"
    }
}
