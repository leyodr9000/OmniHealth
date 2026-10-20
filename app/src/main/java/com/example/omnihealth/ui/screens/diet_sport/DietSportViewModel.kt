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
import com.example.omnihealth.domain.util.MealParser
import com.example.omnihealth.domain.util.MealPlanner
import com.example.omnihealth.domain.util.TimeUtils
import com.example.omnihealth.data.sensor.StepSensorManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val day: Long = 0L,
    val logs: List<DietLogEntity> = emptyList(),
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val fiber: Float = 0f,
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

    /** 当前查看的日期（epochDay），默认今天；历史补录与查看都走这里。 */
    private val _selectedDay = MutableStateFlow(TimeUtils.epochDayNow())
    val selectedDay: StateFlow<Long> = _selectedDay.asStateFlow()

    fun setDay(epochDay: Long) {
        _selectedDay.value = epochDay.coerceAtMost(TimeUtils.epochDayNow())
    }

    fun shiftDay(deltaDays: Long) = setDay(_selectedDay.value + deltaDays)

    val dietState: StateFlow<DietUiState> = combine(
        _selectedDay.flatMapLatest { repository.dietLogsForDay(it) },
        repository.settings,
    ) { logs, settings ->
        val profile = settings.profile
        val isMale = profile.gender != Gender.FEMALE
        val bmr = HealthCalculator.bmr(profile.weightKg, profile.heightCm, profile.age, isMale)
        val tdee = HealthCalculator.tdee(bmr, profile.activityLevel.factor)
        DietUiState(
            day = _selectedDay.value,
            logs = logs,
            calories = logs.sumOf { it.calories.toDouble() }.toInt(),
            protein = logs.sumOf { it.protein.toDouble() }.toInt(),
            carbs = logs.sumOf { it.carbs.toDouble() }.toInt(),
            fat = logs.sumOf { it.fat.toDouble() }.toInt(),
            fiber = logs.sumOf { it.fiber.toDouble() }.toFloat(),
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

    fun logFood(
        food: FoodEntity,
        grams: Float,
        mealType: String = MealType.LUNCH,
        epochDay: Long = _selectedDay.value,
    ) {
        viewModelScope.launch { repository.logFood(food, grams, mealType, epochDay) }
    }

    /** 编辑已有记录：只改克数与餐次，日期与时间保持不变。 */
    fun updateLog(log: DietLogEntity, grams: Float, mealType: String) {
        viewModelScope.launch { repository.updateDietLog(log, grams, mealType) }
    }

    /* ---------------- 一句话记餐 ---------------- */

    private val _drafts = MutableStateFlow<List<MealParser.Draft>>(emptyList())
    val drafts: StateFlow<List<MealParser.Draft>> = _drafts

    /** 只做识别与匹配；营养值由 UI 用食物资料换算，解析器不产生数字。 */
    fun parseMeal(text: String) {
        if (text.isBlank()) {
            _drafts.value = emptyList()
            return
        }
        viewModelScope.launch {
            val foods = repository.allFoods().first()
            _drafts.value = MealParser.parse(text, foods)
        }
    }

    fun updateDraftGrams(index: Int, grams: Float) {
        _drafts.value = _drafts.value.mapIndexed { i, draft ->
            if (i == index) draft.copy(grams = grams.coerceIn(1f, 2000f)) else draft
        }
    }

    fun removeDraft(index: Int) {
        _drafts.value = _drafts.value.filterIndexed { i, _ -> i != index }
    }

    fun clearDrafts() {
        _drafts.value = emptyList()
    }

    /* ---------------- 最近常吃 / 复制上一餐 ---------------- */

    val recentFoods: StateFlow<List<com.example.omnihealth.data.local.dao.RecentFood>> =
        repository.recentFoods(8)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 按当前时钟推断该复制哪一餐。 */
    fun mealTypeForNow(): String {
        val hour = java.time.LocalTime.now().hour
        return when {
            hour < 10 -> MealType.BREAKFAST
            hour < 15 -> MealType.LUNCH
            hour < 21 -> MealType.DINNER
            else -> MealType.SNACK
        }
    }

    fun copyLastMeal(mealType: String, onDone: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val items = repository.lastMeal(mealType)
            if (items.isEmpty()) {
                onDone(0)
                return@launch
            }
            val count = repository.copyMeal(items, _selectedDay.value)
            onDone(count)
        }
    }

    suspend fun foodById(id: Long): com.example.omnihealth.data.local.entity.FoodEntity? =
        repository.foodById(id)

    /* ---------------- 我的套餐 ---------------- */

    val templates: StateFlow<List<com.example.omnihealth.data.local.entity.MealTemplateEntity>> =
        repository.templates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 把「上一次同类餐」存成套餐，名字按餐次自动生成。 */
    fun saveLastMealAsTemplate(mealType: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val items = repository.lastMeal(mealType)
            if (items.isEmpty()) {
                onDone(false)
                return@launch
            }
            val label = when (mealType) {
                MealType.BREAKFAST -> "早餐套餐"
                MealType.LUNCH -> "午餐套餐"
                MealType.DINNER -> "晚餐套餐"
                else -> "加餐套餐"
            }
            repository.saveTemplate(label, items)
            onDone(true)
        }
    }

    fun logTemplate(
        template: com.example.omnihealth.data.local.entity.MealTemplateEntity,
        mealType: String,
        onDone: (Int) -> Unit = {},
    ) {
        viewModelScope.launch {
            onDone(repository.logTemplate(template, mealType, _selectedDay.value))
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch { repository.deleteTemplate(id) }
    }

    /* ---------------- 帮我搭配 ---------------- */

    private val _plans = MutableStateFlow<List<MealPlanner.Plan>>(emptyList())
    val plans: StateFlow<List<MealPlanner.Plan>> = _plans

    /**
     * 生成候选搭配。
     *
     * 预算取「今日剩余」的三分之一（一餐），下限 300 / 上限 900，
     * 不用剩余全部作为一餐目标；结果只是候选，不入库。
     */
    fun generatePlans() {
        viewModelScope.launch {
            val state = dietState.value
            val remaining = (state.calorieGoal - state.calories).coerceAtLeast(0)
            val budget = (remaining / 3f).coerceIn(300f, 900f)
            val foods = repository.allFoods().first()
            _plans.value = MealPlanner.plan(foods = foods, budgetKcal = budget, count = 3)
        }
    }

    fun clearPlans() {
        _plans.value = emptyList()
    }

    /** 用户确认「这一餐就是这么吃的」之后才写库。 */
    fun logPlan(plan: MealPlanner.Plan, mealType: String, epochDay: Long = _selectedDay.value) {
        viewModelScope.launch {
            plan.items.forEach { item ->
                repository.logFood(item.food, item.grams, mealType, epochDay)
            }
            _plans.value = emptyList()
        }
    }

    /** 仅保存已匹配且份量已确认的条目；未匹配项不写入。 */
    fun saveDrafts(mealType: String, epochDay: Long = _selectedDay.value) {
        val ready = _drafts.value.filter { it.food != null && it.grams != null }
        if (ready.isEmpty()) return
        viewModelScope.launch {
            ready.forEach { draft ->
                repository.logFood(draft.food!!, draft.grams!!, mealType, epochDay)
            }
            _drafts.value = emptyList()
        }
    }

    private val _pendingUndo = MutableStateFlow<DietLogEntity?>(null)
    val pendingUndo: StateFlow<DietLogEntity?> = _pendingUndo

    /** 删除后给 5 秒撤销窗口；窗口内恢复按原 id 写回。 */
    fun deleteLog(log: DietLogEntity) {
        viewModelScope.launch {
            repository.deleteDietLog(log)
            _pendingUndo.value = log
            delay(5_000)
            if (_pendingUndo.value?.id == log.id) _pendingUndo.value = null
        }
    }

    fun undoDelete() {
        val log = _pendingUndo.value ?: return
        _pendingUndo.value = null
        viewModelScope.launch { repository.restoreDietLog(log) }
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
