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

/* 饮食/运动页的行组件与小组件。拆分自原 DietSportScreen.kt，便于单文件维护。 */

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun FoodAmountSheet(
    food: FoodEntity,
    onDismiss: () -> Unit,
    onConfirm: (Float, String) -> Unit,
    initialGrams: Float = food.servingGrams.coerceAtLeast(50f),
    initialMeal: String = MealType.LUNCH,
    isEdit: Boolean = false,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var grams by remember { mutableStateOf(initialGrams) }
    var meal by remember { mutableStateOf(initialMeal) }
    var byServing by remember { mutableStateOf(false) }
    val serving = food.servingGrams.coerceAtLeast(1f)

    val meals = listOf(
        MealType.BREAKFAST to stringResource(R.string.meal_breakfast),
        MealType.LUNCH to stringResource(R.string.meal_lunch),
        MealType.DINNER to stringResource(R.string.meal_dinner),
        MealType.SNACK to stringResource(R.string.meal_snack),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = palette.barFill,
        tonalElevation = 0.dp,
        scrimColor = palette.textPrimary.copy(alpha = 0.28f),
    ) {
        LocalizedApp(LocalAppLanguage.current) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    text = stringResource(if (isEdit) R.string.diet_edit_title else R.string.diet_add_title),
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = food.name,
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    meals.forEach { (type, label) ->
                        val active = type == meal
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (active) {
                                        Modifier.background(palette.accent)
                                    } else {
                                        Modifier.background(palette.textPrimary.copy(alpha = 0.06f))
                                    },
                                )
                                .bounceClick(pressScale = 0.95f, haptics = false) { meal = type }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.diet_grams),
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (byServing) {
                            "${(grams / serving).let { if (it >= 10f) String.format(java.util.Locale.US, "%.0f", it) else String.format(java.util.Locale.US, "%.1f", it) }} 份"
                        } else {
                            "${grams.toInt()} g"
                        },
                        color = palette.textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(false to stringResource(R.string.unit_gram), true to stringResource(R.string.unit_serving, serving.toInt()))
                        .forEach { (isServing, label) ->
                            Text(
                                text = label,
                                color = if (byServing == isServing) palette.accent else palette.textTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (byServing == isServing) palette.accent.copy(alpha = 0.12f)
                                        else palette.textPrimary.copy(alpha = 0.05f),
                                    )
                                    .quietClick { byServing = isServing }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    Text(
                        text = "${grams.toInt()} g",
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(-50f, -10f, 10f, 50f).forEach { delta ->
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.textPrimary.copy(alpha = 0.05f))
                                .bounceClick(pressScale = 0.93f, haptics = false) {
                                    val step = if (byServing) serving * (delta / 50f) else delta
                                    grams = (grams + step).coerceIn(5f, 1000f)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (delta > 0) "+${delta.toInt()}" else "${delta.toInt()}",
                                color = palette.textSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                val kcal = (food.calories * grams / 100f).toInt()
                Text(
                    text = "$kcal kcal · P ${(food.protein * grams / 100f).toInt()} g · C ${(food.carbs * grams / 100f).toInt()} g · F ${(food.fat * grams / 100f).toInt()} g",
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(palette.accent)
                        .bounceClick(pressScale = 0.97f) { onConfirm(grams, meal) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(if (isEdit) R.string.edit_save else R.string.diet_add_confirm),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CalorieSummaryCard(diet: DietUiState) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = diet.progress,
                modifier = Modifier.size(86.dp),
                ringColor = palette.calorie,
                strokeWidth = 8.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${diet.calories}",
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("kcal", color = palette.textTertiary, fontSize = 9.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.diet_today_title),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                )
                Text(
                    text = stringResource(R.string.diet_left, diet.left),
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroPill("P", "${diet.protein} g", palette.accent)
                    MacroPill("C", "${diet.carbs} g", palette.warning)
                    MacroPill("F", "${diet.fat} g", palette.accentAlt)
                }
            }
        }
    }
}

@Composable
internal fun MacroPill(label: String, value: String, color: Color) {
    val palette = OmniTheme.palette
    Column {
        Text(label, color = palette.textTertiary, fontSize = 9.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.textPrimary.copy(alpha = 0.06f))
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val active = index == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (active) {
                            Modifier.background(palette.accent)
                        } else {
                            Modifier
                        },
                    )
                    .bounceClick(pressScale = 0.96f, haptics = false) { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
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

@Composable
internal fun SearchBar(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardFill)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.textTertiary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = palette.textTertiary, fontSize = 12.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun FoodRow(food: FoodEntity, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardFill)
            .quietClick(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(food.name, color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${food.category} · P${food.protein.toInt()} C${food.carbs.toInt()} F${food.fat.toInt()}",
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${food.calories.toInt()} kcal",
                color = palette.textSecondary,
                fontSize = 11.sp,
            )
            Text("/100g", color = palette.textTertiary, fontSize = 9.sp)
        }
    }
}

@Composable
internal fun LogRow(
    log: DietLogEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardFill)
            .quietClick(onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(log.foodName, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${log.grams.toInt()} g · ${mealLabel(log.mealType)}",
                color = palette.textTertiary,
                fontSize = 10.sp,
            )
        }
        Text(
            text = "${log.calories.toInt()} kcal",
            color = palette.textSecondary,
            fontSize = 11.sp,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(palette.danger.copy(alpha = 0.12f))
                .bounceClick(pressScale = 0.9f, haptics = false, onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Text("✕", color = palette.danger, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun mealLabel(type: String): String = when (type) {
    MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
    MealType.DINNER -> stringResource(R.string.meal_dinner)
    MealType.SNACK -> stringResource(R.string.meal_snack)
    else -> stringResource(R.string.meal_lunch)
}

@Composable
internal fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.accent.copy(alpha = 0.12f))
            .bounceClick(pressScale = 0.94f, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = palette.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun WorkoutTimerCard(state: SportUiState, onStart: () -> Unit, onStop: () -> Unit) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sport_timer),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                )
                Text(
                    text = formatTime(state.seconds),
                    color = palette.textPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (state.running) palette.danger else palette.accent)
                    .bounceClick(pressScale = 0.95f) { if (state.running) onStop() else onStart() }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Text(
                    text = if (state.running) stringResource(R.string.sport_stop) else stringResource(R.string.sport_start),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun WorkoutRow(workout: WorkoutEntity) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardFill)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(workout.type, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(formatTime(workout.durationSec), color = palette.textTertiary, fontSize = 10.sp)
        }
        Text(
            text = stringResource(R.string.sport_burned, workout.calories),
            color = palette.success,
            fontSize = 11.sp,
        )
    }
}

internal fun formatTime(seconds: Int): String =
    String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
/** 删除后的撤销条：5 秒内可恢复，避免误删无法找回。 */
@Composable
internal fun UndoBar(
    text: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = OmniTheme.palette
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.textPrimary.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = actionLabel,
            color = palette.accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .quietClick(onClick = onAction)
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
        )
    }
}
/** 日期步进按钮。 */
@Composable
internal fun DayStepper(label: String, enabled: Boolean, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(palette.textPrimary.copy(alpha = if (enabled) 0.06f else 0.03f))
            .then(if (enabled) Modifier.quietClick(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) palette.textSecondary else palette.textTertiary.copy(alpha = 0.5f),
            fontSize = 16.sp,
        )
    }
}

/** 今天显示「今天」，其他日期显示 M月d日。 */
@Composable
internal fun dayLabel(epochDay: Long): String {
    val locale = java.util.Locale.getDefault()
    val today = TimeUtils.epochDayNow()
    if (epochDay == today) return stringResource(R.string.day_today)
    val date = java.time.LocalDate.ofEpochDay(epochDay)
    return date.format(
        if (locale.language.startsWith("zh")) {
            java.time.format.DateTimeFormatter.ofPattern("M月d日", java.util.Locale.CHINA)
        } else {
            java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.ENGLISH)
        },
    )
}