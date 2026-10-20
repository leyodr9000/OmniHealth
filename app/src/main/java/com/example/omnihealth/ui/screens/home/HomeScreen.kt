package com.example.omnihealth.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.WaterDrop
import com.example.omnihealth.R
import com.example.omnihealth.ui.components.AppearOnScreen
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.QuickLogType
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.animatedFloat
import com.example.omnihealth.ui.components.animatedFraction
import com.example.omnihealth.ui.components.animatedInt
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.components.StatChip
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.di.activityScopedViewModelOwner
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.quietClick
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDiet: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAi: () -> Unit,
    onQuickLog: (QuickLogType) -> Unit,
) {
    val repository = LocalHealthRepository.current
    val viewModel: HomeViewModel = viewModel(
        viewModelStoreOwner = activityScopedViewModelOwner(),
        factory = viewModelFactory { initializer { HomeViewModel(repository) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = OmniTheme.palette
    var showAddHabit by remember { mutableStateOf(false) }
    val insightText = when {
        state.steps < state.stepGoal -> stringResource(
            R.string.insight_steps_left,
            formatInt(state.stepGoal - state.steps),
        )
        state.waterProgress < 0.7f -> stringResource(
            R.string.insight_water_left,
            "${(((state.waterGoalMl - state.waterMl).coerceAtLeast(0f)) / 50).toInt() * 50} ml",
        )
        state.caloriesLeft > 0 -> stringResource(
            R.string.insight_calories_left,
            formatInt(state.caloriesLeft),
        )
        else -> stringResource(R.string.insight_all_good)
    }
    val locale = Locale.getDefault()
    val dateText = LocalDate.now().format(
        if (locale.language.startsWith("zh")) {
            DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
        } else {
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
        },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppearOnScreen(index = 0) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_greeting, state.nickname),
                        color = palette.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.home_status, dateText),
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .bounceClick(pressScale = 0.9f, onClick = onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.nickname.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                }
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_section_today),
                subtitle = stringResource(R.string.home_section_today_sub),
                action = {
                    Text(
                        text = stringResource(R.string.home_view_stats),
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.bounceClick(pressScale = 0.94f, haptics = false, onClick = onOpenStats),
                    )
                },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    icon = Icons.Rounded.DirectionsWalk,
                    title = stringResource(R.string.home_steps),
                    value = formatInt(animatedInt(state.steps)),
                    unit = "",
                    accent = palette.accent,
                    progress = if (state.hasSteps) state.stepsProgress else null,
                    hint = if (state.hasSteps) stringResource(R.string.home_goal_steps, formatInt(state.stepGoal)) else stringResource(R.string.home_not_recorded),
                    unrecorded = !state.hasSteps,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickLog(QuickLogType.STEPS) },
                )
                MetricTile(
                    icon = Icons.Rounded.WaterDrop,
                    title = stringResource(R.string.home_water),
                    value = String.format(Locale.US, "%.2f", animatedFloat(state.waterMl / 1000f)),
                    unit = "L",
                    accent = palette.water,
                    progress = if (state.hasWater) state.waterProgress else null,
                    hint = if (state.hasWater) stringResource(
                        R.string.home_goal_water,
                        String.format(Locale.US, "%.1f", state.waterGoalMl / 1000f),
                    ) else stringResource(R.string.home_not_recorded),
                    unrecorded = !state.hasWater,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickLog(QuickLogType.WATER) },
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(
                    icon = Icons.Rounded.Bedtime,
                    title = stringResource(R.string.home_sleep),
                    value = String.format(Locale.US, "%.1f", animatedFloat(state.sleepHours)),
                    unit = "h",
                    accent = palette.sleep,
                    progress = if (state.hasSleep) state.sleepProgress else null,
                    hint = if (state.hasSleep) stringResource(
                        R.string.home_goal_sleep,
                        String.format(Locale.US, "%.1f", state.sleepGoalHours),
                    ) else stringResource(R.string.home_not_recorded),
                    unrecorded = !state.hasSleep,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickLog(QuickLogType.SLEEP) },
                )
                MetricTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    title = stringResource(R.string.home_calories),
                    value = formatInt(animatedInt(state.caloriesIn)),
                    unit = "kcal",
                    accent = palette.calorie,
                    progress = if (state.hasCalories) state.calorieProgress else null,
                    hint = if (state.hasCalories) stringResource(R.string.home_remaining, state.caloriesLeft) else stringResource(R.string.home_not_recorded),
                    unrecorded = !state.hasCalories,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDiet,
                )
            }
        }

        // 健康分放在「今日已记录什么」之后：先给事实，再给结论。
        // 健康分放在「今日已记录什么」之后：先给事实，再给结论。
        item { AppearOnScreen(index = 1) { HealthScoreCard(state) } }

        item { AiTipCard(insight = insightText, onClick = onOpenAi) }

        item {
            SectionHeader(
                title = stringResource(R.string.home_section_quick),
                subtitle = stringResource(R.string.home_quick_hint),
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(
                    label = "BMI",
                    value = String.format(Locale.US, "%.1f", animatedFloat(state.bmi)),
                    color = palette.accentAlt,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    label = "TDEE",
                    value = "${animatedInt(state.tdee)}",
                    color = palette.danger,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    label = stringResource(R.string.dim_consistency),
                    value = "${state.habitDone}/${state.habitTotal}",
                    color = palette.success,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.estimate_note),
                color = palette.textTertiary,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 6.dp, start = 2.dp),
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.habits_title),
                subtitle = stringResource(R.string.habits_sub, state.habitDone, state.habitTotal),
                action = {
                    Text(
                        text = "+ " + stringResource(R.string.habits_add),
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.bounceClick(
                            pressScale = 0.94f,
                            haptics = false,
                        ) { showAddHabit = true },
                    )
                },
            )
        }

        item {
            GlassCard(
                Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                if (state.habits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.habits_empty),
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                } else {
                    state.habits.forEach { habit ->
                        HabitRow(
                            name = habit.name,
                            checked = habit.id in state.checkedHabitIds,
                            onToggle = { viewModel.toggleHabit(habit.id) },
                            onDelete = { viewModel.deleteHabit(habit) },
                        )
                    }
                }
            }
        }
    }

    if (showAddHabit) {
        AddHabitDialog(
            onDismiss = { showAddHabit = false },
            onConfirm = { name ->
                viewModel.addHabit(name)
                showAddHabit = false
            },
        )
    }
}
