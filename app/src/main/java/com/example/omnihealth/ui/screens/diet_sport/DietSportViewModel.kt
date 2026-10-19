package com.example.omnihealth.ui.screens.diet_sport

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.data.repository.HealthRepository
import com.example.omnihealth.domain.util.HealthCalculator
import com.example.omnihealth.domain.util.StepSensorManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Immutable
data class DietUiState(
    val logs: List<DietLogEntity> = emptyList(),
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val calorieGoal: Int = 2_000,
) {
    val left: Int get() = (calorieGoal - calories).coerceAtLeast(0)
    val progress: Float get() = if (calorieGoal > 0) calories / calorieGoal.toFloat() else 0f
}

@Immutable
data class SportUiState(
    val steps: Int = 0,
    val stepGoal: Int = 10_000,
    val sensorAvailable: Boolean = true,
    val hasPermission: Boolean = true,
    val workouts: List<WorkoutEntity> = emptyList(),
    val running: Boolean = false,
    val seconds: Int = 0,
) {
    val progress: Float get() = if (stepGoal > 0) steps / stepGoal.toFloat() else 0f
    val burned: Int get() = workouts.sumOf { it.calories }
}

data class PendingFood(
    val food: FoodEntity,
    val grams: Float,
    val mealType: String,
)

@OptIn(FlowPreview::class)
class DietSportViewModel(
    application: Application,
    private val repository: HealthRepository,
) : AndroidViewModel(application) {

    private val sensor = StepSensorManager(application)

    /* ---------------- 搜索 ---------------- */

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val results: StateFlow<List<FoodEntity>> = _query
        .debounce(220)
        .flatMapLatest { text ->
            flow {
                emit(if (text.isBlank()) emptyList() else runCatching { repository.searchFoods(text) }.getOrDefault(emptyList()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dictionary = repository.allFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ---------------- 饮食 ---------------- */

    val dietState: StateFlow<DietUiState> = combine(
        repository.todayDietLogs(),
        repository.settings,
    ) { logs, settings ->
        val profile = settings.profile
        val isMale = profile.gender != Gender.FEMALE
        val bmr = HealthCalculator.bmr(profile.weightKg, profile.heightCm, profile.age, isMale)
        val tdee = HealthCalculator.tdee(bmr, profile.activityLevel.factor)
        DietUiState(
            logs = logs,
            calories = logs.sumOf { it.calories.toDouble() }.toInt(),
            protein = logs.sumOf { it.protein.toDouble() }.toInt(),
            carbs = logs.sumOf { it.carbs.toDouble() }.toInt(),
            fat = logs.sumOf { it.fat.toDouble() }.toInt(),
            calorieGoal = HealthCalculator.calorieTarget(tdee, profile.goal.calorieDelta),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DietUiState())

    /* ---------------- 运动 ---------------- */

    private val timerSeconds = MutableStateFlow(0)
    private val timerRunning = MutableStateFlow(false)
    private var timerJob: Job? = null

    val sportState: StateFlow<SportUiState> = combine(
        sensor.todaySteps,
        repository.settings,
        repository.todayWorkouts(),
        combine(timerSeconds, timerRunning) { seconds, running -> seconds to running },
    ) { steps, settings, workouts, timer ->
        SportUiState(
            steps = steps,
            stepGoal = settings.stepGoal,
            sensorAvailable = sensor.hasSensor,
            hasPermission = sensor.hasActivityPermission(),
            workouts = workouts,
            running = timer.second,
            seconds = timer.first,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SportUiState())

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun logFood(food: FoodEntity, grams: Float, mealType: String = MealType.LUNCH) {
        viewModelScope.launch { repository.logFood(food, grams, mealType) }
    }

    fun deleteLog(log: DietLogEntity) {
        viewModelScope.launch { repository.deleteDietLog(log) }
    }

    fun startTimer() {
        if (timerRunning.value) return
        timerRunning.value = true
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                timerSeconds.update { it + 1 }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        timerRunning.value = false
        val seconds = timerSeconds.value
        timerSeconds.value = 0
        if (seconds >= 5) {
            viewModelScope.launch {
                repository.logWorkout(
                    type = "STRENGTH",
                    durationSec = seconds,
                    calories = (seconds / 60f * 6.5f).toInt().coerceAtLeast(1),
                )
            }
        }
    }

    fun simulateSteps() {
        sensor.simulate(1_000)
    }

    fun refreshSensorPermission() {
        sensor.start()
    }

    fun startSensorIfPossible() {
        viewModelScope.launch {
            repository.todaySteps().first()?.steps?.let { sensor.restore(it) }
            sensor.start()
        }
        viewModelScope.launch {
            sensor.todaySteps
                .debounce(1_200)
                .map { it }
                .collect { steps -> if (steps > 0) repository.saveSteps(steps) }
        }
    }

    override fun onCleared() {
        sensor.stop()
        timerJob?.cancel()
        super.onCleared()
    }
}
