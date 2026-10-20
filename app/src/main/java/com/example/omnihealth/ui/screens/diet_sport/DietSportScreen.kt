package com.example.omnihealth.ui.screens.diet_sport

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import com.example.omnihealth.BuildConfig
import com.example.omnihealth.R
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.domain.util.TimeUtils
import com.example.omnihealth.ui.components.AppearOnScreen
import com.example.omnihealth.ui.components.animatedInt
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.QuickLogType
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.di.activityScopedViewModelOwner
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.quietClick
import java.util.Locale

@Composable
fun DietSportScreen(
    initialTab: Int = 0,
    onQuickLog: (QuickLogType) -> Unit,
    onOpenSport: () -> Unit,
) {
    val repository = LocalHealthRepository.current
    val app = LocalContext.current.applicationContext as Application
    val viewModel: DietSportViewModel = viewModel(
        viewModelStoreOwner = activityScopedViewModelOwner(),
        factory = viewModelFactory { initializer { DietSportViewModel(app, repository) } },
    )
    LaunchedEffect(Unit) { viewModel.startSensorIfPossible() }

    val diet by viewModel.dietState.collectAsStateWithLifecycle()
    val sport by viewModel.sportState.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val dictionary by viewModel.dictionary.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val palette = OmniTheme.palette
    val mealTypeNow = viewModel.mealTypeForNow()

    var tab by remember { mutableIntStateOf(initialTab) }
    var pending by remember { mutableStateOf<FoodEntity?>(null) }
    var editing by remember { mutableStateOf<DietLogEntity?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { viewModel.refreshSensorPermission() }

    val pendingUndo by viewModel.pendingUndo.collectAsStateWithLifecycle()
    val writeError by viewModel.error.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    var smartOpen by remember { mutableStateOf(false) }
    var planOpen by remember { mutableStateOf(false) }
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val recentFoods by viewModel.recentFoods.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var pendingGrams by remember { mutableStateOf<Float?>(null) }
    val cardScope = rememberCoroutineScope()
    val cardContext = androidx.compose.ui.platform.LocalContext.current

    Box(Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = tab,
        transitionSpec = {
            slideInHorizontally(tween(220)) { it / 4 } togetherWith slideOutHorizontally(tween(220)) { -it / 4 }
        },
        label = "dietSportTab",
    ) { page ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bgBottom)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = if (page == 0) stringResource(R.string.diet_tab) else stringResource(R.string.sport_tab),
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_status, stringResource(R.string.diet_today_title)),
                color = palette.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                    DayStepper(label = "‹", enabled = true, onClick = { viewModel.shiftDay(-1) })
                    Text(
                        text = dayLabel(diet.day),
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                    DayStepper(
                        label = "›",
                        enabled = diet.day < TimeUtils.epochDayNow(),
                    onClick = { viewModel.shiftDay(1) },
                )
            }
        }

        item {
            SegmentedControl(
                options = listOf(stringResource(R.string.diet_tab), stringResource(R.string.sport_tab)),
                selected = tab,
                onSelect = { tab = it },
            )
        }

        if (page == 0) {
            dietTabItems(
                page = page,
                diet = diet,
                query = query,
                results = results,
                dictionary = dictionary,
                recentFoods = recentFoods,
                templates = templates,
                mealTypeNow = mealTypeNow,
                palette = palette,
                cardContext = cardContext,
                onQueryChange = viewModel::onQueryChange,
                onPickFood = { pending = it },
                onPickRecent = { recent ->
                    cardScope.launch {
                        val food = viewModel.foodById(recent.foodId)
                        if (food != null) {
                            pendingGrams = recent.grams
                            pending = food
                        }
                    }
                },
                onLogTemplate = { template, done -> viewModel.logTemplate(template, mealTypeNow, done) },
                onCopyMeal = { done -> viewModel.copyLastMeal(mealTypeNow, done) },
                onSaveTemplate = { done -> viewModel.saveLastMealAsTemplate(mealTypeNow, done) },
                onOpenSmart = { smartOpen = true },
                onOpenPlan = {
                    viewModel.generatePlans()
                    planOpen = true
                },
                onEdit = { editing = it },
                onDelete = { viewModel.deleteLog(it) },
            )
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile(
                        icon = Icons.Rounded.DirectionsWalk,
                        title = stringResource(R.string.sport_steps),
                        value = String.format(Locale.US, "%,d", animatedInt(sport.steps)),
                        unit = "",
                        accent = palette.accent,
                        progress = sport.progress,
                        hint = if (sport.sensorAvailable) {
                            stringResource(R.string.sport_steps_hint)
                        } else {
                            stringResource(R.string.sport_no_sensor)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MetricTile(
                        icon = Icons.Rounded.LocalFireDepartment,
                        title = stringResource(R.string.sport_today_workouts),
                        value = "${animatedInt(sport.burned)}",
                        unit = "kcal",
                        accent = palette.calorie,
                        progress = (sport.burned / 400f).coerceIn(0f, 1f),
                        hint = stringResource(R.string.sport_burned, sport.burned),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 模拟步数只在调试构建可见：正式版的步数必须来自传感器或手动补录，
                    // 不能让模拟值混进真实统计。
                    if (BuildConfig.DEBUG) {
                        ActionButton(
                            text = stringResource(R.string.sport_simulate),
                            modifier = Modifier.weight(1f),
                        ) { viewModel.simulateSteps() }
                    }

                    if (!sport.hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActionButton(
                            text = stringResource(R.string.sport_grant),
                            modifier = Modifier.weight(1f),
                        ) { permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) }
                    }
                }
            }

            item { WorkoutTimerCard(sport, onStart = viewModel::startTimer, onStop = viewModel::stopTimer) }

            item {
                SectionHeader(
                    title = stringResource(R.string.sport_today_workouts),
                    subtitle = stringResource(R.string.sport_burned, sport.burned),
                )
            }

            if (sport.workouts.isEmpty()) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.sport_no_workout),
                            color = palette.textSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
            } else {
                items(sport.workouts, key = { "w-${it.id}" }) { workout ->
                    WorkoutRow(workout)
                }
            }
        }
    }
    }

    writeError?.let {
        UndoBar(
            text = stringResource(R.string.write_failed),
            actionLabel = stringResource(R.string.dismiss),
            onAction = { viewModel.consumeError() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 12.dp),
        )
    }

    pendingUndo?.let { log ->
        UndoBar(
            text = stringResource(R.string.deleted_hint, log.foodName),
            actionLabel = stringResource(R.string.undo),
            onAction = { viewModel.undoDelete() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 12.dp),
        )
    }
    }

    if (planOpen) {
        MealPlanSheet(
            plans = plans,
            onRegenerate = { viewModel.generatePlans() },
            onAccept = { plan, meal ->
                viewModel.logPlan(plan, meal)
                planOpen = false
            },
            onDismiss = {
                viewModel.clearPlans()
                planOpen = false
            },
        )
    }

    if (smartOpen) {
        SmartMealSheet(
            drafts = drafts,
            onTextChange = {},
            onParse = { viewModel.parseMeal(it) },
            onGramsChange = { index, grams -> viewModel.updateDraftGrams(index, grams) },
            onRemove = { index -> viewModel.removeDraft(index) },
            onSave = { meal ->
                viewModel.saveDrafts(meal)
                smartOpen = false
            },
            onDismiss = {
                viewModel.clearDrafts()
                smartOpen = false
            },
        )
    }

    editing?.let { log ->
        val per100 = if (log.grams > 0f) 100f / log.grams else 1f
        FoodAmountSheet(
            food = FoodEntity(
                name = log.foodName,
                category = "",
                calories = log.calories * per100,
                protein = log.protein * per100,
                carbs = log.carbs * per100,
                fat = log.fat * per100,
            ),
            initialGrams = log.grams,
            initialMeal = log.mealType,
            isEdit = true,
            onDismiss = { editing = null },
            onConfirm = { grams, meal ->
                viewModel.updateLog(log, grams, meal)
                editing = null
            },
        )
    }

    pending?.let { food ->
        FoodAmountSheet(
            food = food,
            initialGrams = pendingGrams ?: food.servingGrams.coerceAtLeast(50f),
            onDismiss = {
                pending = null
                pendingGrams = null
            },
            onConfirm = { grams, meal ->
                viewModel.logFood(food, grams, meal)
                pending = null
                pendingGrams = null
            },
        )
    }
}
