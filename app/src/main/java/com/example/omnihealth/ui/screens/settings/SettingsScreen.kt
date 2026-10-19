package com.example.omnihealth.ui.screens.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.launch
import java.util.Locale

private enum class SettingsPage { ROOT, APPEARANCE }

private enum class SettingsSheet { NONE, PROFILE, API, GOALS }

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    layoutMode: NavLayoutMode,
    language: AppLanguage,
    framework: UiFramework,
    onThemeChange: (ThemeMode) -> Unit,
    onLayoutChange: (NavLayoutMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onFrameworkChange: (UiFramework) -> Unit,
) {
    val palette = OmniTheme.palette
    val repository = LocalHealthRepository.current
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()

    var page by remember { mutableStateOf(SettingsPage.ROOT) }
    var sheet by remember { mutableStateOf(SettingsSheet.NONE) }

    BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }

    if (page == SettingsPage.APPEARANCE) {
        AppearanceScreen(
            themeMode = themeMode,
            layoutMode = layoutMode,
            language = language,
            framework = framework,
            onThemeChange = onThemeChange,
            onLayoutChange = onLayoutChange,
            onLanguageChange = onLanguageChange,
            onFrameworkChange = onFrameworkChange,
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
                    value = stringResource(R.string.settings_reminders_off),
                    onClick = {},
                )
                SettingDivider()
                SettingItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_row_about),
                    value = stringResource(R.string.settings_version),
                    onClick = {},
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
            .bounceClick(pressScale = 0.99f, haptics = false, onClick = onClick)
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
