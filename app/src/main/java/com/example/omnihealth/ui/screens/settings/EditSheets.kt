package com.example.omnihealth.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.data.datastore.ActivityLevel
import com.example.omnihealth.data.datastore.ApiConfig
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.datastore.HealthGoal
import com.example.omnihealth.data.datastore.UserProfile
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import java.util.Locale

/* ------------------------------------------------------------------ */
/* 个人资料                                                            */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nickname by remember { mutableStateOf(profile.nickname) }
    var gender by remember { mutableStateOf(profile.gender) }
    var age by remember { mutableStateOf(profile.age.toString()) }
    var height by remember { mutableStateOf(profile.heightCm.toString()) }
    var weight by remember { mutableStateOf(profile.weightKg.toString()) }
    var activity by remember { mutableStateOf(profile.activityLevel) }
    var goal by remember { mutableStateOf(profile.goal) }

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
                    .padding(bottom = 30.dp),
            ) {
                SheetHeader(
                    title = stringResource(R.string.profile_edit_title),
                    subtitle = stringResource(R.string.profile_edit_sub),
                )

                SheetField(stringResource(R.string.field_nickname), nickname) { nickname = it }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SheetField(
                            stringResource(R.string.field_age),
                            age,
                            KeyboardType.Number,
                        ) { age = it.filter(Char::isDigit).take(3) }
                    }
                    Box(Modifier.weight(1f)) {
                        SheetField(
                            stringResource(R.string.field_height),
                            height,
                            KeyboardType.Decimal,
                        ) { height = it.filter { c -> c.isDigit() || c == '.' }.take(5) }
                    }
                    Box(Modifier.weight(1f)) {
                        SheetField(
                            stringResource(R.string.field_weight),
                            weight,
                            KeyboardType.Decimal,
                        ) { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) }
                    }
                }

                ChipGroup(
                    title = stringResource(R.string.profile_gender_age),
                    options = listOf(
                        Gender.MALE to stringResource(R.string.gender_male),
                        Gender.FEMALE to stringResource(R.string.gender_female),
                    ),
                    selected = gender,
                ) { gender = it }

                ChipGroup(
                    title = stringResource(R.string.profile_activity),
                    options = listOf(
                        ActivityLevel.SEDENTARY to stringResource(R.string.activity_sedentary),
                        ActivityLevel.LIGHT to stringResource(R.string.activity_light),
                        ActivityLevel.MODERATE to stringResource(R.string.activity_moderate),
                        ActivityLevel.ACTIVE to stringResource(R.string.activity_active),
                        ActivityLevel.ATHLETE to stringResource(R.string.activity_athlete),
                    ),
                    selected = activity,
                ) { activity = it }

                ChipGroup(
                    title = stringResource(R.string.profile_goal),
                    options = listOf(
                        HealthGoal.LOSE to stringResource(R.string.goal_lose),
                        HealthGoal.MAINTAIN to stringResource(R.string.goal_maintain),
                        HealthGoal.GAIN to stringResource(R.string.goal_gain),
                    ),
                    selected = goal,
                ) { goal = it }

                Spacer(Modifier.height(18.dp))
                PrimaryButton(stringResource(R.string.edit_save)) {
                    onSave(
                        profile.copy(
                            nickname = nickname.ifBlank { profile.nickname },
                            gender = gender,
                            age = age.toIntOrNull()?.coerceIn(10, 100) ?: profile.age,
                            heightCm = height.toFloatOrNull()?.coerceIn(100f, 230f) ?: profile.heightCm,
                            weightKg = weight.toFloatOrNull()?.coerceIn(25f, 250f) ?: profile.weightKg,
                            activityLevel = activity,
                            goal = goal,
                        ),
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* API 配置                                                            */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConfigSheet(
    config: ApiConfig,
    onDismiss: () -> Unit,
    onSave: (ApiConfig) -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var baseUrl by remember { mutableStateOf(config.baseUrl) }
    var apiKey by remember { mutableStateOf(config.apiKey) }
    var model by remember { mutableStateOf(config.model) }
    var temperature by remember { mutableStateOf(config.temperature) }

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
                    .padding(bottom = 30.dp),
            ) {
                SheetHeader(
                    title = stringResource(R.string.api_edit_title),
                    subtitle = stringResource(R.string.api_edit_sub),
                )

                SheetField(stringResource(R.string.field_base_url), baseUrl) { baseUrl = it.trim() }
                SheetField(stringResource(R.string.field_api_key), apiKey) { apiKey = it.trim() }
                SheetField(stringResource(R.string.field_model), model) { model = it.trim() }

                Text(
                    text = stringResource(R.string.field_temperature) +
                        "  " + String.format(Locale.US, "%.1f", temperature),
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.2f, 0.5f, 0.7f, 0.9f, 1.0f).forEach { value ->
                        val active = kotlin.math.abs(temperature - value) < 0.05f
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) {
                                        Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt))
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                palette.textPrimary.copy(alpha = 0.06f),
                                                palette.textPrimary.copy(alpha = 0.06f),
                                            ),
                                        )
                                    },
                                )
                                .bounceClick(pressScale = 0.94f, haptics = false) { temperature = value }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", value),
                                color = if (active) Color.White else palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                PrimaryButton(stringResource(R.string.edit_save)) {
                    onSave(
                        config.copy(
                            baseUrl = baseUrl.ifBlank { config.baseUrl },
                            apiKey = apiKey,
                            model = model.ifBlank { config.model },
                            temperature = temperature,
                        ),
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 每日目标                                                            */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsSheet(
    steps: Int,
    waterMl: Int,
    sleepMin: Int,
    calories: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int) -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var stepText by remember { mutableStateOf(steps.toString()) }
    var waterText by remember { mutableStateOf(waterMl.toString()) }
    var sleepText by remember { mutableStateOf(String.format(Locale.US, "%.1f", sleepMin / 60f)) }
    var calorieText by remember { mutableStateOf(calories.toString()) }

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
                    .padding(bottom = 30.dp),
            ) {
                SheetHeader(
                    title = stringResource(R.string.goals_edit_title),
                    subtitle = stringResource(R.string.goals_edit_sub),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SheetField(stringResource(R.string.field_steps_goal), stepText, KeyboardType.Number) {
                            stepText = it.filter(Char::isDigit).take(6)
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        SheetField(stringResource(R.string.field_water_goal), waterText, KeyboardType.Number) {
                            waterText = it.filter(Char::isDigit).take(5)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SheetField(stringResource(R.string.field_sleep_goal), sleepText, KeyboardType.Decimal) {
                            sleepText = it.filter { c -> c.isDigit() || c == '.' }.take(4)
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        SheetField(stringResource(R.string.field_calorie_goal), calorieText, KeyboardType.Number) {
                            calorieText = it.filter(Char::isDigit).take(5)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                PrimaryButton(stringResource(R.string.edit_save)) {
                    onSave(
                        stepText.toIntOrNull()?.coerceIn(1_000, 50_000) ?: steps,
                        waterText.toIntOrNull()?.coerceIn(500, 6_000) ?: waterMl,
                        ((sleepText.toFloatOrNull() ?: (sleepMin / 60f)).coerceIn(3f, 12f) * 60).toInt(),
                        calorieText.toIntOrNull()?.coerceIn(1_000, 6_000) ?: calories,
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* 共享控件                                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun SheetHeader(title: String, subtitle: String) {
    val palette = OmniTheme.palette
    Text(
        text = title,
        color = palette.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = subtitle,
        color = palette.textTertiary,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
    )
}

@Composable
private fun SheetField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    val palette = OmniTheme.palette
    Column(Modifier.padding(top = 10.dp)) {
        Text(label, color = palette.textTertiary, fontSize = 10.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.textPrimary.copy(alpha = 0.06f))
                .padding(horizontal = 14.dp, vertical = 11.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun <T> ChipGroup(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val palette = OmniTheme.palette
    Column(Modifier.padding(top = 14.dp)) {
        Text(title, color = palette.textTertiary, fontSize = 10.sp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { (value, label) ->
                val active = value == selected
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (active) {
                                Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        palette.textPrimary.copy(alpha = 0.06f),
                                        palette.textPrimary.copy(alpha = 0.06f),
                                    ),
                                )
                            },
                        )
                        .bounceClick(pressScale = 0.94f, haptics = false) { onSelect(value) }
                        .padding(vertical = 9.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.White else palette.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(palette.accent, palette.accentAlt)))
            .bounceClick(pressScale = 0.97f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
