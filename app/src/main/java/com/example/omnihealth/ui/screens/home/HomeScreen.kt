package com.example.omnihealth.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.omnihealth.R
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.components.StatChip
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
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
        factory = viewModelFactory { initializer { HomeViewModel(repository) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = OmniTheme.palette
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
                        .background(Brush.linearGradient(listOf(palette.accent, palette.accentAlt)))
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

        item { HealthScoreCard(state) }

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
                    emoji = "👟",
                    title = stringResource(R.string.home_steps),
                    value = formatInt(state.steps),
                    unit = "",
                    accent = palette.accent,
                    progress = state.stepsProgress,
                    hint = stringResource(R.string.home_goal_steps, formatInt(state.stepGoal)),
                    modifier = Modifier.weight(1f),
                    onClick = onQuickLog,
                )
                MetricTile(
                    emoji = "💧",
                    title = stringResource(R.string.home_water),
                    value = String.format(Locale.US, "%.2f", state.waterMl / 1000f),
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
                    emoji = "🌙",
                    title = stringResource(R.string.home_sleep),
                    value = String.format(Locale.US, "%.1f", state.sleepHours),
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
                    emoji = "🔥",
                    title = stringResource(R.string.home_calories),
                    value = formatInt(state.caloriesIn),
                    unit = "kcal",
                    accent = Color(0xFFFF8A5B),
                    progress = state.calorieProgress,
                    hint = stringResource(R.string.home_remaining, state.caloriesLeft),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenDiet,
                )
            }
        }

        item { AiTipCard(onClick = onOpenAi) }

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
                    value = String.format(Locale.US, "%.1f", state.bmi),
                    color = palette.accentAlt,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    label = "TDEE",
                    value = "${state.tdee}",
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
    }
}

private fun formatInt(value: Int): String = String.format(Locale.US, "%,d", value)

@Composable
private fun HealthScoreCard(state: HomeUiState) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = state.scoreOverall / 100f,
                modifier = Modifier.size(104.dp),
                ringColor = palette.accent,
                strokeWidth = 9.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.scoreOverall}",
                        color = palette.textPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.home_score_label),
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
                Text(
                    text = stringResource(R.string.home_score_delta, 4),
                    color = palette.success,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp),
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
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

@Composable
private fun AiTipCard(onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(palette.corner))
            .background(
                Brush.linearGradient(
                    listOf(palette.accent.copy(alpha = 0.92f), palette.accentAlt.copy(alpha = 0.92f)),
                ),
            )
            .bounceClick(pressScale = 0.97f, onClick = onClick)
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 14.sp)
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
        }
    }
}
