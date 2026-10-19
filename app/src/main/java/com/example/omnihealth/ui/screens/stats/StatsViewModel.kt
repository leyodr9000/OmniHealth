package com.example.omnihealth.ui.screens.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.StepEntity
import com.example.omnihealth.data.local.entity.VitalEntity
import com.example.omnihealth.data.repository.HealthRepository
import com.example.omnihealth.domain.util.HealthCalculator
import com.example.omnihealth.domain.util.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class StatsUiState(
    val labels: List<String> = emptyList(),
    val weight: List<Float> = emptyList(),
    val steps: List<Int> = emptyList(),
    val sleep: List<Float> = emptyList(),
    val calories: List<Int> = emptyList(),
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val score: HealthCalculator.ScoreBreakdown = HealthCalculator.ScoreBreakdown(0, 0, 0, 0, 0),
    val stepGoal: Int = 10_000,
    val hasData: Boolean = false,
)

private data class StatsCore(
    val settings: AppSettings,
    val waterMl: Float,
    val habitTotal: Int,
    val habitDone: Int,
)

class StatsViewModel(private val repository: HealthRepository) : ViewModel() {

    private val core = combine(
        repository.settings,
        repository.waterToday(),
        repository.habits(),
        repository.habitLogsToday(),
    ) { settings, water, habits, logs ->
        StatsCore(settings, water, habits.size, logs.size)
    }

    val uiState: StateFlow<StatsUiState> = combine(
        core,
        repository.stepsRange(7),
        repository.sleepRange(7),
        repository.dietLogsBetween(7),
        repository.weightTrend(30),
    ) { c, stepRows, sleepRows, dietRows, weightRows ->
        build(
            core = c,
            stepRows = stepRows,
            sleepRows = sleepRows,
            dietRows = dietRows,
            weightRows = weightRows,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    private fun build(
        core: StatsCore,
        stepRows: List<StepEntity>,
        sleepRows: List<VitalEntity>,
        dietRows: List<DietLogEntity>,
        weightRows: List<VitalEntity>,
    ): StatsUiState {
        val days = TimeUtils.lastDays(7)
        val labelFormatter = DateTimeFormatter.ofPattern("M/d", Locale.getDefault())

        val stepsByDay = stepRows.associateBy { it.epochDay }
        val sleepByDay = sleepRows.groupBy { TimeUtils.epochDayOf(it.recordedAt) }
            .mapValues { (_, list) -> list.sumOf { it.value.toDouble() }.toFloat() }
        val dietByDay = dietRows.groupBy { it.epochDay }
            .mapValues { (_, list) -> list.sumOf { it.calories.toDouble() }.toInt() }
        val weightByDay = weightRows
            .groupBy { TimeUtils.epochDayOf(it.recordedAt) }
            .mapValues { (_, list) -> list.maxByOrNull { it.recordedAt }?.value ?: 0f }

        var lastWeight = weightByDay.values.lastOrNull() ?: core.settings.profile.weightKg
        val weights = days.map { day ->
            weightByDay[day] ?: lastWeight.also { lastWeight = it }
        }

        val today = TimeUtils.epochDayNow()
        val todayMacros = dietRows.filter { it.epochDay == today }
        val protein = todayMacros.sumOf { it.protein.toDouble() }.toInt()
        val carbs = todayMacros.sumOf { it.carbs.toDouble() }.toInt()
        val fat = todayMacros.sumOf { it.fat.toDouble() }.toInt()

        val profile = core.settings.profile
        val isMale = profile.gender != Gender.FEMALE
        val bmr = HealthCalculator.bmr(profile.weightKg, profile.heightCm, profile.age, isMale)
        val tdee = HealthCalculator.tdee(bmr, profile.activityLevel.factor)
        val calorieGoal = HealthCalculator.calorieTarget(tdee, profile.goal.calorieDelta)

        val todaySteps = stepsByDay[today]?.steps ?: 0
        val score = HealthCalculator.fiveDimensionScore(
            caloriesIn = dietByDay[today]?.toFloat() ?: 0f,
            calorieTarget = calorieGoal,
            steps = todaySteps,
            stepGoal = core.settings.stepGoal,
            sleepHours = sleepByDay[today] ?: 0f,
            sleepGoalHours = core.settings.sleepGoalMin / 60f,
            waterMl = core.waterMl,
            waterGoalMl = core.settings.waterGoalMl.toFloat(),
            habitChecks = core.habitDone,
            habitTarget = core.habitTotal.coerceAtLeast(1),
            sleepRecorded = (sleepByDay[today] ?: 0f) > 0f,
            waterRecorded = core.waterMl > 0f,
            nutritionRecorded = (dietByDay[today] ?: 0) > 0,
            habitsRecorded = core.habitTotal > 0,
        )

        return StatsUiState(
            labels = days.map { LocalDate.ofEpochDay(it).format(labelFormatter) },
            weight = weights,
            steps = days.map { stepsByDay[it]?.steps ?: 0 },
            sleep = days.map { sleepByDay[it] ?: 0f },
            calories = days.map { dietByDay[it] ?: 0 },
            protein = protein,
            carbs = carbs,
            fat = fat,
            score = score,
            stepGoal = core.settings.stepGoal,
            hasData = stepRows.isNotEmpty() || dietRows.isNotEmpty() || weightRows.isNotEmpty(),
        )
    }
}
