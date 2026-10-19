package com.example.omnihealth.ui.screens.stats

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.omnihealth.ui.chart.ChartHeight
import com.example.omnihealth.ui.chart.CombinedStepsChart
import com.example.omnihealth.ui.chart.HealthRadarChart
import com.example.omnihealth.ui.chart.LineTrendChart
import com.example.omnihealth.ui.chart.MacroPieChart
import com.example.omnihealth.ui.chart.StepsBarChart
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import java.util.Locale

@Composable
fun ChartAnalyticsScreen() {
    val repository = LocalHealthRepository.current
    val viewModel: StatsViewModel = viewModel(
        factory = viewModelFactory { initializer { StatsViewModel(repository) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = OmniTheme.palette

    val tabs = listOf(
        stringResource(R.string.stats_tab_trend),
        stringResource(R.string.stats_tab_macro),
        stringResource(R.string.stats_tab_week),
        stringResource(R.string.stats_tab_radar),
    )
    var selected by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.stats_title),
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.stats_subtitle),
                color = palette.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { label ->
                    val index = tabs.indexOf(label)
                    val active = index == selected
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (active) {
                                    Modifier.background(palette.accent)
                                } else {
                                    Modifier.background(palette.textPrimary.copy(alpha = 0.06f))
                                },
                            )
                            .bounceClick(pressScale = 0.94f, haptics = false) { selected = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }

        if (!state.hasData) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.stats_no_data),
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        when (selected) {
            0 -> {
                item {
                    ChartCard(
                        title = stringResource(R.string.stats_line_title),
                        subtitle = stringResource(R.string.stats_line_sub),
                    ) {
                        LineTrendChart(
                            labels = state.labels,
                            values = state.weight,
                            color = palette.accentAlt,
                            modifier = Modifier.height(ChartHeight),
                        )
                    }
                }
                item {
                    ChartCard(
                        title = stringResource(R.string.stats_combined_title),
                        subtitle = stringResource(R.string.stats_combined_sub),
                    ) {
                        CombinedStepsChart(
                            labels = state.labels,
                            steps = state.steps,
                            calories = state.calories,
                            modifier = Modifier.height(ChartHeight),
                        )
                    }
                }
            }

            1 -> {
                item {
                    ChartCard(
                        title = stringResource(R.string.stats_macro_title),
                        subtitle = stringResource(R.string.stats_macro_sub),
                    ) {
                        MacroPieChart(
                            protein = state.protein.toFloat(),
                            carbs = state.carbs.toFloat(),
                            fat = state.fat.toFloat(),
                            labels = listOf("P", "C", "F"),
                            modifier = Modifier.height(ChartHeight),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroTile("Protein", state.protein, palette.accent, Modifier.weight(1f))
                        MacroTile("Carbs", state.carbs, palette.warning, Modifier.weight(1f))
                        MacroTile("Fat", state.fat, palette.accentAlt, Modifier.weight(1f))
                    }
                }
            }

            2 -> {
                item {
                    ChartCard(
                        title = stringResource(R.string.stats_week_title),
                        subtitle = stringResource(
                            R.string.stats_week_sub,
                            String.format(Locale.US, "%,d", state.stepGoal),
                        ),
                    ) {
                        StepsBarChart(
                            labels = state.labels,
                            steps = state.steps,
                            goal = state.stepGoal,
                            modifier = Modifier.height(ChartHeight),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            stringResource(R.string.stats_steps),
                            String.format(Locale.US, "%,d", state.steps.sum()),
                            Modifier.weight(1f),
                        )
                        SummaryCard(
                            stringResource(R.string.stats_calories),
                            "${state.calories.sum()} kcal",
                            Modifier.weight(1f),
                        )
                    }
                }
            }

            else -> {
                item {
                    ChartCard(
                        title = stringResource(R.string.stats_radar_title),
                        subtitle = stringResource(R.string.stats_radar_sub),
                    ) {
                        HealthRadarChart(
                            labels = listOf(
                                stringResource(R.string.dim_nutrition),
                                stringResource(R.string.dim_activity),
                                stringResource(R.string.dim_sleep),
                                stringResource(R.string.dim_hydration),
                                stringResource(R.string.dim_consistency),
                            ),
                            values = listOf(
                                state.score.nutrition.toFloat(),
                                state.score.activity.toFloat(),
                                state.score.sleep.toFloat(),
                                state.score.hydration.toFloat(),
                                state.score.consistency.toFloat(),
                            ),
                            modifier = Modifier.height(ChartHeight + 24.dp),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            stringResource(R.string.home_health_index),
                            "${state.score.overall}",
                            Modifier.weight(1f),
                        )
                        SummaryCard(
                            stringResource(R.string.stats_sleep),
                            String.format(Locale.US, "%.1f h", state.sleep.lastOrNull() ?: 0f),
                            Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        SectionHeader(title = title, subtitle = subtitle)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun MacroTile(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    val palette = OmniTheme.palette
    GlassCard(modifier, contentPadding = PaddingValues(14.dp)) {
        Text(label, color = palette.textTertiary, fontSize = 10.sp)
        Text(
            text = "$value g",
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    val palette = OmniTheme.palette
    GlassCard(modifier, contentPadding = PaddingValues(14.dp)) {
        Text(title, color = palette.textTertiary, fontSize = 10.sp)
        Text(
            text = value,
            color = palette.textPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
