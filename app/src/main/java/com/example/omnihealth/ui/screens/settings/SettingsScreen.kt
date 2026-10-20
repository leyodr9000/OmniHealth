package com.example.omnihealth.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import java.io.File
import android.widget.Toast
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.omnihealth.data.work.ReminderScheduler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omnihealth.BuildConfig
import com.example.omnihealth.R
import com.example.omnihealth.data.datastore.ActivityLevel
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.datastore.HealthGoal
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.i18n.AppLanguage
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.ThemeMode
import com.example.omnihealth.ui.theme.UiFramework
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.quietClick
import kotlinx.coroutines.launch
import java.util.Locale

private enum class SettingsPage { ROOT, APPEARANCE }

private enum class SettingsSheet { NONE, PROFILE, API, GOALS, REMINDERS, ABOUT }

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    layoutMode: NavLayoutMode,
    language: AppLanguage,
    framework: UiFramework,
    darkModeId: String,
    reduceMotion: Boolean,
    onThemeChange: (ThemeMode) -> Unit,
    onLayoutChange: (NavLayoutMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFrameworkChange: (UiFramework) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onDarkModeChange: (String) -> Unit,
) {
    val palette = OmniTheme.palette
    val repository = LocalHealthRepository.current
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    var restoreCandidate by remember { mutableStateOf<Pair<android.net.Uri, com.example.omnihealth.data.backup.BackupBundle>?>(null) }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bundle = runCatching { repository.inspectBackup(context, uri) }.getOrNull()
                if (bundle == null) {
                    Toast.makeText(context, context.getString(R.string.restore_read_failed), Toast.LENGTH_LONG).show()
                } else {
                    restoreCandidate = uri to bundle
                }
            }
        }
    }

    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var page by remember { mutableStateOf(SettingsPage.ROOT) }
    var sheet by remember { mutableStateOf(SettingsSheet.NONE) }

    BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }

    if (page == SettingsPage.APPEARANCE) {
        AppearanceScreen(
            themeMode = themeMode,
            layoutMode = layoutMode,
            language = language,
            framework = framework,
            darkModeId = darkModeId,
            reduceMotion = reduceMotion,
            onThemeChange = onThemeChange,
            onLayoutChange = onLayoutChange,
            onLanguageChange = onLanguageChange,
            onFrameworkChange = onFrameworkChange,
            onReduceMotionChange = onReduceMotionChange,
            onDarkModeChange = onDarkModeChange,
            onBack = { page = SettingsPage.ROOT },
        )
        return
    }

    val profile = settings.profile
    val bmi = if (profile.heightCm > 0f) {
        profile.weightKg / ((profile.heightCm / 100f) * (profile.heightCm / 100f))
    } else {
        0f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profile.nickname.take(1).uppercase(),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = profile.nickname,
                        color = palette.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "BMI ${String.format(Locale.US, "%.1f", bmi)} · " +
                            goalLabel(profile.goal),
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.settings_group_personal)) {
                SettingItem(
                    icon = Icons.Rounded.Person,
                    title = stringResource(R.string.settings_row_profile),
                    subtitle = "${genderLabel(profile.gender)} · ${profile.age} · " +
                        "${profile.heightCm.toInt()} cm · ${profile.weightKg} kg",
                    onClick = { sheet = SettingsSheet.PROFILE },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Flag,
                    title = stringResource(R.string.settings_row_goals),
                    subtitle = "${String.format(Locale.US, "%,d", settings.stepGoal)} · " +
                        "${settings.waterGoalMl} ml · " +
                        String.format(Locale.US, "%.1f h", settings.sleepGoalMin / 60f) + " · " +
                        "${settings.calorieGoal} kcal",
                    onClick = { sheet = SettingsSheet.GOALS },
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.settings_group_preferences)) {
                SettingItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_row_appearance),
                    value = stringResource(framework.labelRes) + " · " +
                        stringResource(themeMode.labelRes),
                    onClick = { page = SettingsPage.APPEARANCE },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.AutoAwesome,
                    title = stringResource(R.string.settings_row_ai),
                    value = if (settings.api.apiKey.isBlank()) {
                        stringResource(R.string.ai_provider_offline)
                    } else {
                        settings.api.model
                    },
                    onClick = { sheet = SettingsSheet.API },
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.settings_group_about)) {
                SettingItem(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_row_reminders),
                    value = if (settings.waterReminder || settings.sleepReminder) {
                        stringResource(R.string.reminder_on)
                    } else {
                        stringResource(R.string.reminder_off)
                    },
                    onClick = { sheet = SettingsSheet.REMINDERS },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Save,
                    title = stringResource(R.string.settings_row_backup),
                    onClick = {
                        scope.launch {
                            val file = runCatching { repository.exportBackup(context) }.getOrNull()
                            if (file != null) {
                                shareFile(context, file, "application/json")
                            } else {
                                Toast.makeText(context, context.getString(R.string.export_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Restore,
                    title = stringResource(R.string.settings_row_restore),
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Download,
                    title = stringResource(R.string.settings_row_export),
                    onClick = {
                        scope.launch {
                            val file = runCatching { repository.exportCsv(context) }.getOrNull()
                            if (file != null) {
                                shareFile(context, file)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.export_failed),
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                    },
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_row_about),
                    value = BuildConfig.VERSION_NAME,
                    onClick = { sheet = SettingsSheet.ABOUT },
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.settings_footer),
                color = palette.textTertiary,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }

    restoreCandidate?.let { (uri, bundle) ->
        AlertDialog(
            onDismissRequest = { restoreCandidate = null },
            containerColor = palette.cardFill,
            title = {
                Text(
                    text = stringResource(R.string.restore_confirm_title),
                    color = palette.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.restore_confirm_body, bundle.totalRecords, bundle.version),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                )
            },
            confirmButton = {
                Text(
                    text = stringResource(R.string.edit_save),
                    color = palette.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .quietClick {
                            restoreCandidate = null
                            scope.launch {
                                val result = runCatching { repository.restoreBackup(context, uri) }
                                    .getOrNull()
                                val message = when (result) {
                                    is com.example.omnihealth.data.backup.RestoreResult.Success ->
                                        context.getString(R.string.restore_done, result.counts.values.sum())
                                    is com.example.omnihealth.data.backup.RestoreResult.Failure ->
                                        context.getString(R.string.restore_failed, result.reason)
                                    null -> context.getString(R.string.restore_failed, "unknown")
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                        .padding(8.dp),
                )
            },
            dismissButton = {
                Text(
                    text = stringResource(R.string.edit_cancel),
                    color = palette.textTertiary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .quietClick { restoreCandidate = null }
                        .padding(8.dp),
                )
            },
        )
    }

    when (sheet) {
        SettingsSheet.PROFILE -> ProfileEditSheet(
            profile = profile,
            onDismiss = { sheet = SettingsSheet.NONE },
            onSave = { updated ->
                scope.launch { repository.updateProfile(updated) }
                sheet = SettingsSheet.NONE
            },
        )

        SettingsSheet.API -> ApiConfigSheet(
            config = settings.api,
            onDismiss = { sheet = SettingsSheet.NONE },
            onSave = { api ->
                scope.launch { repository.updateApi(api) }
                sheet = SettingsSheet.NONE
            },
        )

        SettingsSheet.GOALS -> GoalsSheet(
            steps = settings.stepGoal,
            waterMl = settings.waterGoalMl,
            sleepMin = settings.sleepGoalMin,
            calories = settings.calorieGoal,
            onDismiss = { sheet = SettingsSheet.NONE },
            onSave = { steps, water, sleepMin, calories ->
                scope.launch { repository.updateGoals(steps, water, sleepMin, calories) }
                sheet = SettingsSheet.NONE
            },
        )

        SettingsSheet.REMINDERS -> RemindersSheet(
            water = settings.waterReminder,
            sleep = settings.sleepReminder,
            startHour = settings.waterStartHour,
            endHour = settings.waterEndHour,
            intervalHours = settings.waterIntervalHours,
            onDismiss = { sheet = SettingsSheet.NONE },
            onWaterChange = { enabled ->
                ensureNotificationPermission()
                scope.launch { repository.setWaterReminder(enabled) }
                ReminderScheduler.setWaterReminder(
                    context = context,
                    enabled = enabled,
                    startHour = settings.waterStartHour,
                    endHour = settings.waterEndHour,
                    intervalHours = settings.waterIntervalHours,
                )
            },
            onSleepChange = { enabled ->
                ensureNotificationPermission()
                scope.launch { repository.setSleepReminder(enabled) }
                ReminderScheduler.setSleepReminder(context, enabled)
            },
            onWindowChange = { start, end, interval ->
                scope.launch {
                    repository.setWaterWindow(start, end, interval)
                    ReminderScheduler.setWaterReminder(
                        context = context,
                        enabled = settings.waterReminder,
                        startHour = start,
                        endHour = end,
                        intervalHours = interval,
                    )
                }
            },
        )

        SettingsSheet.ABOUT -> AboutSheet(
            version = BuildConfig.VERSION_NAME,
            onDismiss = { sheet = SettingsSheet.NONE },
        )

        SettingsSheet.NONE -> Unit
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = OmniTheme.palette
    Column {
        Text(
            text = title,
            color = palette.textTertiary,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.cardFill)
                .border(1.dp, palette.cardStroke, RoundedCornerShape(14.dp)),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .quietClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(palette.accent.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = palette.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                color = palette.textTertiary,
                fontSize = 10.sp,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = palette.textTertiary.copy(alpha = 0.65f),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SettingDivider() {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 54.dp)
            .height(0.6.dp)
            .background(palette.cardStroke),
    )
}

@Composable
private fun genderLabel(gender: Gender): String = stringResource(
    if (gender == Gender.FEMALE) R.string.gender_female else R.string.gender_male,
)

@Composable
private fun activityLabel(level: ActivityLevel): String = stringResource(
    when (level) {
        ActivityLevel.SEDENTARY -> R.string.activity_sedentary
        ActivityLevel.LIGHT -> R.string.activity_light
        ActivityLevel.MODERATE -> R.string.activity_moderate
        ActivityLevel.ACTIVE -> R.string.activity_active
        ActivityLevel.ATHLETE -> R.string.activity_athlete
    },
)

@Composable
private fun goalLabel(goal: HealthGoal): String = stringResource(
    when (goal) {
        HealthGoal.LOSE -> R.string.goal_lose
        HealthGoal.MAINTAIN -> R.string.goal_maintain
        HealthGoal.GAIN -> R.string.goal_gain
    },
)
/** 通过系统分享面板导出 CSV（微信 / 邮件 / 文件管理器都能接收）。 */
private fun shareFile(context: android.content.Context, file: File, mime: String = "text/csv") {
    val uri = runCatching {
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.getOrNull() ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.name)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, context.getString(R.string.settings_row_export)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(chooser) }
}