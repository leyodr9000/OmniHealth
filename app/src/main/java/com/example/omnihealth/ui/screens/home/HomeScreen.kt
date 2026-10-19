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
    onQuickLog: () -> Unit,
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

        item { AppearOnScreen(index = 1) { HealthScoreCard(state) } }

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
                    progress = state.stepsProgress,
                    hint = stringResource(R.string.home_goal_steps, formatInt(state.stepGoal)),
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
                MetricTile(
                    icon = Icons.Rounded.WaterDrop,
                    title = stringResource(R.string.home_water),
                    value = String.format(Locale.US, "%.2f", animatedFloat(state.waterMl / 1000f)),
                    unit = "L",
                    accent = Color(0xFF4FB3FF),
                    progress = state.waterProgress,
                    hint = stringResource(
                        R.string.home_goal_water,
                        String.format(Locale.US, "%.1f", state.waterGoalMl / 1000f),
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
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
                    accent = Color(0xFF8B7CFF),
                    progress = state.sleepProgress,
                    hint = stringResource(
                        R.string.home_goal_sleep,
                        String.format(Locale.US, "%.1f", state.sleepGoalHours),
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
                MetricTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    title = stringResource(R.string.home_calories),
                    value = formatInt(animatedInt(state.caloriesIn)),
                    unit = "kcal",
                    accent = Color(0xFFFF8A5B),
                    progress = state.calorieProgress,
                    hint = stringResource(R.string.home_remaining, state.caloriesLeft),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDiet,
                )
            }
        }

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

@Composable
private fun HabitRow(
    name: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .quietClick(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val checkScale by animateFloatAsState(
            targetValue = if (checked) 1f else 0.86f,
            animationSpec = spring(dampingRatio = 0.42f, stiffness = 620f),
            label = "habitCheck",
        )
        Box(
            Modifier
                .size(22.dp)
                .graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                }
                .clip(CircleShape)
                .background(if (checked) palette.accent else Color.Transparent)
                .border(
                    width = if (checked) 0.dp else 1.5.dp,
                    color = if (checked) palette.accent else palette.textTertiary.copy(alpha = 0.5f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = name,
            color = if (checked) palette.textTertiary else palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "✕",
            color = palette.textTertiary.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier
                .quietClick(onClick = onDelete)
                .padding(6.dp),
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val palette = OmniTheme.palette
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.cardFill,
        title = {
            Text(
                text = stringResource(R.string.habits_add),
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.habit_name),
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.textPrimary.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it.take(20) },
                        singleLine = true,
                        textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.edit_save),
                color = palette.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .quietClick { onConfirm(text) }
                    .padding(8.dp),
            )
        },
        dismissButton = {
            Text(
                text = stringResource(R.string.edit_cancel),
                color = palette.textTertiary,
                fontSize = 13.sp,
                modifier = Modifier
                    .quietClick(onClick = onDismiss)
                    .padding(8.dp),
            )
        },
    )
}

private fun formatInt(value: Int): String = String.format(Locale.US, "%,d", value)

@Composable
private fun HealthScoreCard(state: HomeUiState) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = if (state.score.reliable) state.scoreOverall / 100f else 0f,
                modifier = Modifier.size(104.dp),
                ringColor = palette.accent,
                strokeWidth = 9.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.score.reliable) "${animatedInt(state.scoreOverall)}" else "—",
                        color = palette.textPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            if (state.score.reliable) R.string.home_score_label else R.string.score_insufficient,
                        ),
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_health_index),
                    color = palette.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                ScoreBar(stringResource(R.string.dim_nutrition), state.score.nutrition / 100f, palette.accent)
                ScoreBar(stringResource(R.string.dim_activity), state.score.activity / 100f, palette.accentAlt)
                ScoreBar(stringResource(R.string.dim_sleep), state.score.sleep / 100f, Color(0xFF8B7CFF))
                ScoreBar(stringResource(R.string.dim_hydration), state.score.hydration / 100f, Color(0xFF4FB3FF))
            }
        }
    }
}

@Composable
private fun ScoreBar(label: String, fraction: Float, color: Color) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.textTertiary, fontSize = 10.sp, modifier = Modifier.width(42.dp))
        Box(
            Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.textTertiary.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedFraction(fraction))
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun AiTipCard(insight: String, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(palette.corner))
            .background(palette.accent)
            .bounceClick(pressScale = 0.97f, onClick = onClick)
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.home_ai_title),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.home_ai_cta),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = insight,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
