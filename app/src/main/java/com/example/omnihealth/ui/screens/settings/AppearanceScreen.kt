package com.example.omnihealth.ui.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.ui.i18n.AppLanguage
import com.example.omnihealth.ui.navigation.NavLayoutMode
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.ThemeMode
import com.example.omnihealth.ui.theme.UiFramework
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.paletteFor

@Composable
fun AppearanceScreen(
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
    onBack: () -> Unit,
) {
    val palette = OmniTheme.palette

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(palette.textPrimary.copy(alpha = 0.05f))
                        .bounceClick(pressScale = 0.9f, onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.settings_back),
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_appearance_title),
                        color = palette.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_appearance_sub),
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.settings_section_framework),
                color = palette.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UiFramework.entries.forEach { item ->
                    FrameworkChip(
                        label = stringResource(item.labelRes),
                        selected = item == framework,
                        modifier = Modifier.weight(1f),
                        onClick = { onFrameworkChange(item) },
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "system" to stringResource(R.string.dark_system),
                    "light" to stringResource(R.string.dark_light),
                    "dark" to stringResource(R.string.dark_dark),
                ).forEach { (id, label) ->
                    FrameworkChip(
                        label = label,
                        selected = id == darkModeId,
                        modifier = Modifier.weight(1f),
                        onClick = { onDarkModeChange(id) },
                    )
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.cardFill)
                    .border(1.dp, palette.cardStroke, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.motion_title),
                        color = palette.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = stringResource(R.string.motion_desc),
                        color = palette.textTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = onReduceMotionChange,
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.settings_section_language),
                color = palette.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp),
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
            Text(
                text = stringResource(R.string.settings_section_theme),
                color = palette.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
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
            Text(
                text = stringResource(R.string.settings_section_layout),
                color = palette.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NavLayoutMode.selectable.forEach { mode ->
                    LayoutOption(
                        mode = mode,
                        selected = mode == layoutMode,
                        onClick = { onLayoutChange(mode) },
                    )
                }
            }
        }
    }
}
