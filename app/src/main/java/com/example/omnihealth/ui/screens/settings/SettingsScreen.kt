package com.example.omnihealth.ui.screens.settings

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.omnihealth.R
import com.example.omnihealth.data.datastore.ActivityLevel
import com.example.omnihealth.data.datastore.AppSettings
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.datastore.HealthGoal
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.i18n.AppLanguage
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.ThemeMode
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.paletteFor
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    layoutMode: NavLayoutMode,
    language: AppLanguage,
    onThemeChange: (ThemeMode) -> Unit,
    onLayoutChange: (NavLayoutMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val palette = OmniTheme.palette
    val repository = LocalHealthRepository.current
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    var sheet by remember { mutableStateOf(SettingsSheet.NONE) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                color = palette.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_section_language),
                subtitle = stringResource(R.string.settings_section_language_sub),
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { item ->
                    LanguageChip(
                        label = stringResource(item.labelRes),
                        selected = item == language,
                        modifier = Modifier.weight(1f),
                        onClick = { onLanguageChange(item) },
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_section_theme),
                subtitle = stringResource(R.string.settings_section_theme_sub),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOption(
                        mode = mode,
                        selected = mode == themeMode,
                        onClick = { onThemeChange(mode) },
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_section_layout),
                subtitle = stringResource(R.string.settings_section_layout_sub),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NavLayoutMode.entries.forEach { mode ->
                    LayoutOption(
                        mode = mode,
                        selected = mode == layoutMode,
                        onClick = { onLayoutChange(mode) },
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_section_profile),
                subtitle = stringResource(R.string.settings_section_profile_sub),
            )
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { sheet = SettingsSheet.PROFILE }) {
                ProfileRow(stringResource(R.string.profile_nickname), settings.profile.nickname)
                ProfileRow(
                    stringResource(R.string.profile_gender_age),
                    "${genderLabel(settings.profile.gender)} · ${settings.profile.age}",
                )
                ProfileRow(
                    stringResource(R.string.profile_body),
                    "${settings.profile.heightCm.toInt()} cm · ${settings.profile.weightKg} kg",
                )
                ProfileRow(stringResource(R.string.profile_activity), activityLabel(settings.profile.activityLevel))
                ProfileRow(stringResource(R.string.profile_goal), goalLabel(settings.profile.goal))
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.settings_section_api),
                subtitle = stringResource(R.string.settings_section_api_sub),
            )
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { sheet = SettingsSheet.API }) {
                ProfileRow("Provider", if (settings.api.apiKey.isBlank()) "Offline engine" else "Cloud")
                ProfileRow("Base URL", settings.api.baseUrl)
                ProfileRow("Model", settings.api.model)
                ProfileRow(
                    "API Key",
                    if (settings.api.apiKey.isBlank()) "Not set" else "••••" + settings.api.apiKey.takeLast(4),
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.goals_edit_title),
                subtitle = stringResource(R.string.goals_edit_sub),
            )
        }

        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { sheet = SettingsSheet.GOALS }) {
                ProfileRow(stringResource(R.string.field_steps_goal), String.format(Locale.US, "%,d", settings.stepGoal))
                ProfileRow(stringResource(R.string.field_water_goal), "${settings.waterGoalMl} ml")
                ProfileRow(
                    stringResource(R.string.field_sleep_goal),
                    String.format(Locale.US, "%.1f h", settings.sleepGoalMin / 60f),
                )
                ProfileRow(stringResource(R.string.field_calorie_goal), "${settings.calorieGoal}")
            }
        }

        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(palette.textPrimary.copy(alpha = 0.05f))
                    .padding(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_footer),
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                )
            }
        }
    }

    when (sheet) {
        SettingsSheet.PROFILE -> ProfileEditSheet(
            profile = settings.profile,
            onDismiss = { sheet = SettingsSheet.NONE },
            onSave = { profile ->
                scope.launch { repository.updateProfile(profile) }
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

private enum class SettingsSheet { NONE, PROFILE, API, GOALS }

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

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) {
                    Modifier.background(palette.accent)
                } else {
                    Modifier.background(palette.textPrimary.copy(alpha = 0.06f))
                },
            )
            .bounceClick(pressScale = 0.94f, haptics = false, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val preview = paletteFor(mode)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(preview.corner))
            .background(preview.cardFill)
            .bounceClick(pressScale = 0.97f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(preview.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(preview.mode.labelRes).take(1),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(mode.labelRes),
                    color = preview.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(mode.taglineRes),
                    color = preview.textTertiary,
                    fontSize = 9.sp,
                )
            }
            Text(
                text = stringResource(mode.blurbRes),
                color = preview.textSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) preview.accent else preview.textTertiary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Text("✓", color = preview.bgTop, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LayoutOption(
    mode: NavLayoutMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(palette.corner))
            .background(palette.cardFill)
            .bounceClick(pressScale = 0.97f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LayoutDiagram(mode)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(mode.labelRes),
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(mode.descRes),
                color = palette.textSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) palette.accent else palette.textTertiary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LayoutDiagram(mode: NavLayoutMode) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .size(width = 46.dp, height = 46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.textPrimary.copy(alpha = 0.06f))
            .padding(6.dp),
    ) {
        when (mode) {
            NavLayoutMode.A_FAB -> {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    repeat(4) {
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.7f)),
                        )
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(palette.accentAlt),
                )
            }

            NavLayoutMode.B_FIVE -> Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(5) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(palette.accent.copy(alpha = 0.7f)),
                    )
                }
            }

            NavLayoutMode.C_BENTO -> {
                Row(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        Modifier
                            .size(width = 18.dp, height = 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.accent.copy(alpha = 0.35f)),
                    )
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.accentAlt.copy(alpha = 0.35f)),
                    )
                }
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    repeat(3) {
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(palette.accent.copy(alpha = 0.7f)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
