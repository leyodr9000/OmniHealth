package com.example.omnihealth.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.data.repository.HealthRepository
import com.example.omnihealth.domain.util.HealthCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val nickname: String = "Kai",
    val steps: Int = 0,
    val stepGoal: Int = 10_000,
    val waterMl: Float = 0f,
    val waterGoalMl: Int = 2_500,
    val sleepHours: Float = 0f,
    val sleepGoalHours: Float = 8f,
    val caloriesIn: Int = 0,
    val calorieGoal: Int = 2_000,
    val weightKg: Float = 0f,
    val bmi: Float = 0f,
    val tdee: Int = 0,
    val habitDone: Int = 0,
    val habitTotal: Int = 0,
    val workouts: List<WorkoutEntity> = emptyList(),
    val score: HealthCalculator.ScoreBreakdown = HealthCalculator.ScoreBreakdown(0, 0, 0, 0, 0),
) {
    val scoreOverall: Int get() = score.overall
    val stepsProgress: Float get() = if (stepGoal > 0) steps / stepGoal.toFloat() else 0f
    val waterProgress: Float get() = if (waterGoalMl > 0) waterMl / waterGoalMl else 0f
    val calorieProgress: Float get() = if (calorieGoal > 0) caloriesIn / calorieGoal.toFloat() else 0f
    val sleepProgress: Float get() = (sleepHours / sleepGoalHours).coerceIn(0f, 1f)
    val caloriesLeft: Int get() = (calorieGoal - caloriesIn).coerceAtLeast(0)
}

class HomeViewModel(private val repository: HealthRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.settings,
        repository.todaySnapshot(),
        repository.habits(),
        repository.habitLogsToday(),
        repository.todayWorkouts(),
    ) { settings, snapshot, habits, logs, workouts ->
        val profile = settings.profile
        val isMale = profile.gender != Gender.FEMALE
        val bmr = HealthCalculator.bmr(profile.weightKg, profile.heightCm, profile.age, isMale)
        val tdee = HealthCalculator.tdee(bmr, profile.activityLevel.factor)
        val calorieGoal = HealthCalculator.calorieTarget(tdee, profile.goal.calorieDelta)
        val bmi = HealthCalculator.bmi(snapshot.weightKg, profile.heightCm)

        val score = HealthCalculator.fiveDimensionScore(
            caloriesIn = snapshot.caloriesIn,
            calorieTarget = calorieGoal,
            steps = snapshot.steps,
            stepGoal = snapshot.stepGoal,
            sleepHours = snapshot.sleepHours,
            sleepGoalHours = settings.sleepGoalMin / 60f,
            waterMl = snapshot.waterMl,
            waterGoalMl = settings.waterGoalMl.toFloat(),
            habitChecks = logs.size,
            habitTarget = habits.size.coerceAtLeast(1),
        )

        HomeUiState(
            nickname = profile.nickname,
            steps = snapshot.steps,
            stepGoal = snapshot.stepGoal,
            waterMl = snapshot.waterMl,
            waterGoalMl = settings.waterGoalMl,
            sleepHours = snapshot.sleepHours,
            sleepGoalHours = settings.sleepGoalMin / 60f,
            caloriesIn = snapshot.caloriesIn.toInt(),
            calorieGoal = calorieGoal,
            weightKg = if (snapshot.weightKg > 0f) snapshot.weightKg else profile.weightKg,
            bmi = bmi,
            tdee = tdee.toInt(),
            habitDone = logs.size,
            habitTotal = habits.size,
            workouts = workouts,
            score = score,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun logWater(ml: Float) = launch { repository.logWater(ml) }

    fun logWeight(kg: Float) = launch { repository.logWeight(kg) }

    fun logSleep(hours: Float) = launch { repository.logSleep(hours) }

    fun toggleHabit(id: Long) = launch { repository.toggleHabit(id) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
