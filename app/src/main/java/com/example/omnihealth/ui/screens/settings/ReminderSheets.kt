@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
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
import com.example.omnihealth.ui.components.AppPrimaryButton
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick
import com.example.omnihealth.ui.theme.bounceClick
import java.util.Locale

/* 提醒相关弹窗与步进控件。拆分自原 EditSheets.kt。 */

@Composable
fun RemindersSheet(
    water: Boolean,
    sleep: Boolean,
    startHour: Int,
    endHour: Int,
    intervalHours: Int,
    sleepHour: Int,
    sleepMinute: Int,
    mealLog: Boolean,
    mealLogHour: Int,
    onDismiss: () -> Unit,
    onWaterChange: (Boolean) -> Unit,
    onSleepChange: (Boolean) -> Unit,
    onWindowChange: (Int, Int, Int) -> Unit,
    onSleepTimeChange: (Int, Int) -> Unit,
    onMealLogChange: (Boolean) -> Unit,
    onMealLogHourChange: (Int) -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    title = stringResource(R.string.reminders_title),
                    subtitle = stringResource(R.string.reminders_sub),
                )

                ReminderRow(
                    title = stringResource(R.string.reminder_water),
                    desc = stringResource(R.string.reminder_water_desc, intervalHours),
                    checked = water,
                    onCheckedChange = onWaterChange,
                )

                if (water) {
                    WindowStepper(
                        label = stringResource(R.string.reminder_window_start),
                        value = String.format(java.util.Locale.US, "%02d:00", startHour),
                        onMinus = {
                            onWindowChange(startHour - 1.coerceAtLeast(0).coerceAtMost(endHour - 1), endHour, intervalHours)
                        },
                        onPlus = {
                            onWindowChange((startHour + 1).coerceAtMost(endHour - 1), endHour, intervalHours)
                        },
                    )
                    WindowStepper(
                        label = stringResource(R.string.reminder_window_end),
                        value = String.format(java.util.Locale.US, "%02d:00", endHour),
                        onMinus = {
                            onWindowChange(startHour, (endHour - 1).coerceAtLeast(startHour + 1), intervalHours)
                        },
                        onPlus = {
                            onWindowChange(startHour, (endHour + 1).coerceAtMost(24), intervalHours)
                        },
                    )
                    WindowStepper(
                        label = stringResource(R.string.reminder_window_interval),
                        value = stringResource(R.string.reminder_hours, intervalHours),
                        onMinus = {
                            onWindowChange(startHour, endHour, (intervalHours - 1).coerceAtLeast(1))
                        },
                        onPlus = {
                            onWindowChange(startHour, endHour, (intervalHours + 1).coerceAtMost(6))
                        },
                    )
                }
                ReminderRow(
                    title = stringResource(R.string.reminder_sleep),
                    desc = stringResource(R.string.reminder_sleep_desc),
                    checked = sleep,
                    onCheckedChange = onSleepChange,
                )

                if (sleep) {
                    WindowStepper(
                        label = stringResource(R.string.reminder_sleep_hour),
                        value = String.format(java.util.Locale.US, "%02d", sleepHour),
                        onMinus = { onSleepTimeChange((sleepHour - 1 + 24) % 24, sleepMinute) },
                        onPlus = { onSleepTimeChange((sleepHour + 1) % 24, sleepMinute) },
                    )
                    WindowStepper(
                        label = stringResource(R.string.reminder_sleep_minute),
                        value = String.format(java.util.Locale.US, "%02d", sleepMinute),
                        onMinus = { onSleepTimeChange(sleepHour, (sleepMinute - 15 + 60) % 60) },
                        onPlus = { onSleepTimeChange(sleepHour, (sleepMinute + 15) % 60) },
                    )
                }

                ReminderRow(
                    title = stringResource(R.string.reminder_meal_log),
                    desc = stringResource(R.string.reminder_meal_log_desc),
                    checked = mealLog,
                    onCheckedChange = onMealLogChange,
                )
                if (mealLog) {
                    WindowStepper(
                        label = stringResource(R.string.reminder_sleep_hour),
                        value = String.format(java.util.Locale.US, "%02d:00", mealLogHour),
                        onMinus = { onMealLogHourChange((mealLogHour - 1 + 24) % 24) },
                        onPlus = { onMealLogHourChange((mealLogHour + 1) % 24) },
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.reminder_permission_hint),
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun ReminderRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(desc, color = palette.textTertiary, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

/* ------------------------------------------------------------------ */
/* 关于                                                                */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WindowStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = palette.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
        )
        StepperButton("−", onMinus)
        Text(
            text = value,
            color = palette.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        StepperButton("+", onPlus)
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.textPrimary.copy(alpha = 0.06f))
            .quietClick(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = palette.textSecondary, fontSize = 13.sp)
    }
}
