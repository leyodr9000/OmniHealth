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
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.omnihealth.ui.di.activityScopedViewModelOwner
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick
import com.example.omnihealth.ui.theme.bounceClick
import java.util.Locale

@Composable
fun ChartAnalyticsScreen(
    onOpenRecords: () -> Unit = {},
    onOpenReminders: () -> Unit = {},
) {
    val repository = LocalHealthRepository.current
    val viewModel: StatsViewModel = viewModel(
        viewModelStoreOwner = activityScopedViewModelOwner(),
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

        // 趋势解读：结论由确定性规则给出，模型只负责措辞（当前版本直接用规则文案）

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
        state.weeklyReview?.let { review ->
            item { WeeklyReviewCard(review = review, fiberDays = state.fiberDays, onOpenRecords = onOpenRecords, onOpenReminders = onOpenReminders) }
        }

        if (selected == 0) state.weightInsight?.let { insight ->
            item { TrendInsightCard(title = stringResource(R.string.trend_weight_title), insight = insight) }
        }
        if (selected == 0) state.calorieInsight?.let { insight ->
            item { TrendInsightCard(title = stringResource(R.string.trend_calorie_title), insight = insight) }
        }
        if (selected == 0) state.stepsInsight?.let { insight ->
            item { TrendInsightCard(title = stringResource(R.string.trend_steps_title), insight = insight) }
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

/**
 * 趋势解读卡：结论 → 依据 → 限制。
 *
 * 三段结构来自方案要求：先说发生了什么，再给区间与天数，最后说明不能过度解读的地方。
 */
@Composable
private fun TrendInsightCard(
    title: String,
    insight: com.example.omnihealth.domain.util.TrendInterpreter.Insight,
) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            if (!insight.reliable) {
                Text(
                    text = stringResource(R.string.trend_low_confidence),
                    color = palette.warning,
                    fontSize = 10.sp,
                )
            }
        }
        Text(
            text = when (insight.status) {
                com.example.omnihealth.domain.util.TrendInterpreter.Status.NO_RECENT_DATA ->
                    stringResource(R.string.trend_no_recent, title)
                com.example.omnihealth.domain.util.TrendInterpreter.Status.INSUFFICIENT ->
                    stringResource(R.string.trend_insufficient, title)
                com.example.omnihealth.domain.util.TrendInterpreter.Status.READY -> when (insight.direction) {
                    com.example.omnihealth.domain.util.TrendInterpreter.Direction.FLAT ->
                        stringResource(R.string.trend_flat, title)
                    com.example.omnihealth.domain.util.TrendInterpreter.Direction.UP ->
                        stringResource(
                            R.string.trend_change,
                            title,
                            stringResource(R.string.trend_up),
                            com.example.omnihealth.domain.util.TrendInterpreter.format(kotlin.math.abs(insight.delta)),
                            insight.unit,
                        )
                    com.example.omnihealth.domain.util.TrendInterpreter.Direction.DOWN ->
                        stringResource(
                            R.string.trend_change,
                            title,
                            stringResource(R.string.trend_down),
                            com.example.omnihealth.domain.util.TrendInterpreter.format(kotlin.math.abs(insight.delta)),
                            insight.unit,
                        )
                }
            },
            color = palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp),
        )
        // 依据与限制默认收起：首屏只留一句结论，需要时再展开
        var expanded by remember { mutableStateOf(false) }
        Text(
            text = if (expanded) stringResource(R.string.trend_hide_basis) else stringResource(R.string.trend_show_basis),
            color = palette.accent,
            fontSize = 10.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .quietClick { expanded = !expanded },
        )
        if (expanded) {
            Text(
                text = if (insight.reliable) {
                    stringResource(
                        R.string.trend_evidence,
                        com.example.omnihealth.domain.util.TrendInterpreter.format(insight.recentAvg),
                        insight.unit,
                        insight.recentDays,
                        com.example.omnihealth.domain.util.TrendInterpreter.format(insight.previousAvg),
                        insight.unit,
                        insight.previousDays,
                    )
                } else {
                    stringResource(R.string.trend_coverage, insight.recentDays, insight.previousDays)
                },
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.trend_limitation),
                color = palette.textTertiary.copy(alpha = 0.85f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** 周复盘卡：本周变化 → 数据依据 → 一条可执行行动。 */
@Composable
private fun WeeklyReviewCard(
    review: com.example.omnihealth.domain.util.WeeklyReview.Review,
    fiberDays: Int,
    onOpenRecords: () -> Unit,
    onOpenReminders: () -> Unit,
) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.review_title),
            color = palette.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = when (review.summary) {
                com.example.omnihealth.domain.util.WeeklyReview.Summary.NO_DATA ->
                    stringResource(R.string.review_summary_no_data)
                com.example.omnihealth.domain.util.WeeklyReview.Summary.FEW_DAYS ->
                    stringResource(R.string.review_summary_few, review.dietDays)
                com.example.omnihealth.domain.util.WeeklyReview.Summary.MISSING_BREAKFAST ->
                    stringResource(
                        R.string.review_summary_breakfast,
                        review.dietDays,
                        review.missingBreakfast,
                    )
                com.example.omnihealth.domain.util.WeeklyReview.Summary.STABLE ->
                    stringResource(R.string.review_summary_stable, review.dietDays)
            },
            color = palette.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(R.string.review_fiber, fiberDays),
            color = palette.textSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(
                R.string.review_coverage,
                review.dietDays,
                review.breakfastDays,
                review.sleepDays,
                review.stepDays,
            ),
            color = palette.textTertiary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        // 存成图片：给一个「带得走」的出口，而不只是留在 App 里
        val imageContext = androidx.compose.ui.platform.LocalContext.current
        val imageScope = androidx.compose.runtime.rememberCoroutineScope()
        Text(
            text = stringResource(R.string.review_share_image),
            color = palette.accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.accent.copy(alpha = 0.10f))
                .quietClick {
                    imageScope.launch {
                        val summaryText = when (review.summary) {
                            com.example.omnihealth.domain.util.WeeklyReview.Summary.NO_DATA ->
                                imageContext.getString(R.string.review_summary_no_data)
                            com.example.omnihealth.domain.util.WeeklyReview.Summary.FEW_DAYS ->
                                imageContext.getString(R.string.review_summary_few, review.dietDays)
                            com.example.omnihealth.domain.util.WeeklyReview.Summary.MISSING_BREAKFAST ->
                                imageContext.getString(
                                    R.string.review_summary_breakfast,
                                    review.dietDays,
                                    review.missingBreakfast,
                                )
                            com.example.omnihealth.domain.util.WeeklyReview.Summary.STABLE ->
                                imageContext.getString(R.string.review_summary_stable, review.dietDays)
                        }
                        val evidenceText = imageContext.getString(
                            R.string.review_coverage,
                            review.dietDays,
                            review.breakfastDays,
                            review.sleepDays,
                            review.stepDays,
                        )
                        val actionTexts = review.actions.map { action ->
                            when (action.kind) {
                                com.example.omnihealth.domain.util.WeeklyReview.ActionKind.FIX_LOGGING ->
                                    imageContext.getString(R.string.review_action_fix)
                                com.example.omnihealth.domain.util.WeeklyReview.ActionKind.CHECK_BREAKFAST ->
                                    imageContext.getString(R.string.review_action_breakfast)
                                com.example.omnihealth.domain.util.WeeklyReview.ActionKind.LOG_SLEEP ->
                                    imageContext.getString(R.string.review_action_sleep)
                                com.example.omnihealth.domain.util.WeeklyReview.ActionKind.NO_CHANGE ->
                                    imageContext.getString(R.string.review_action_none)
                            }
                        }
                        val file = com.example.omnihealth.data.export.ReportImageExporter
                            .export(imageContext, summaryText, evidenceText, actionTexts, fiberDays)
                        if (file != null) {
                            com.example.omnihealth.data.export.ReportImageExporter.share(imageContext, file)
                        }
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        review.actions.forEach { action ->
            val onClick = when (action.type) {
                com.example.omnihealth.domain.util.WeeklyReview.ActionType.OPEN_RECORDS -> onOpenRecords
                com.example.omnihealth.domain.util.WeeklyReview.ActionType.OPEN_REMINDERS -> onOpenReminders
                com.example.omnihealth.domain.util.WeeklyReview.ActionType.NONE -> null
            }
            Text(
                text = when (action.kind) {
                    com.example.omnihealth.domain.util.WeeklyReview.ActionKind.FIX_LOGGING ->
                        stringResource(R.string.review_action_fix)
                    com.example.omnihealth.domain.util.WeeklyReview.ActionKind.CHECK_BREAKFAST ->
                        stringResource(R.string.review_action_breakfast)
                    com.example.omnihealth.domain.util.WeeklyReview.ActionKind.LOG_SLEEP ->
                        stringResource(R.string.review_action_sleep)
                    com.example.omnihealth.domain.util.WeeklyReview.ActionKind.NO_CHANGE ->
                        stringResource(R.string.review_action_none)
                },
                color = if (onClick != null) palette.accent else palette.textTertiary,
                fontSize = 11.sp,
                fontWeight = if (onClick != null) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (onClick != null) palette.accent.copy(alpha = 0.10f)
                        else palette.textPrimary.copy(alpha = 0.04f),
                    )
                    .then(
                        if (onClick != null) Modifier.quietClick(onClick = onClick) else Modifier,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}
