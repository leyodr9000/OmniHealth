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

/* 外观页的选项组件：框架/语言芯片、主题卡、布局卡与示意图。 */

@Composable
internal fun FrameworkChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) palette.accent else palette.cardFill)
            .border(1.dp, if (selected) palette.accent else palette.cardStroke, RoundedCornerShape(12.dp))
            .bounceClick(pressScale = 0.96f, haptics = false, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
internal fun LanguageChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) palette.accent else palette.cardFill)
            .border(1.dp, palette.cardStroke, RoundedCornerShape(12.dp))
            .bounceClick(pressScale = 0.94f, haptics = false, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
internal fun ThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val preview = paletteFor(mode)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(preview.cardFill)
            .border(
                1.dp,
                if (selected) preview.accent else preview.cardStroke,
                RoundedCornerShape(14.dp),
            )
            .bounceClick(pressScale = 0.98f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(preview.accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(mode.labelRes).take(1),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(mode.labelRes),
                color = preview.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(mode.blurbRes),
                color = preview.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    1.5.dp,
                    if (selected) preview.accent else preview.textTertiary.copy(alpha = 0.35f),
                    CircleShape,
                )
                .background(if (selected) preview.accent else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
    }
}

@Composable
internal fun LayoutOption(
    mode: NavLayoutMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.cardFill)
            .border(
                1.dp,
                if (selected) palette.accent else palette.cardStroke,
                RoundedCornerShape(14.dp),
            )
            .bounceClick(pressScale = 0.98f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LayoutDiagram(mode)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(mode.labelRes),
                color = palette.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(mode.descRes),
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(
                    1.5.dp,
                    if (selected) palette.accent else palette.textTertiary.copy(alpha = 0.35f),
                    CircleShape,
                )
                .background(if (selected) palette.accent else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
    }
}

@Composable
internal fun LayoutDiagram(mode: NavLayoutMode) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .size(width = 44.dp, height = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.textPrimary.copy(alpha = 0.04f))
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
                                .background(palette.accent.copy(alpha = 0.75f)),
                        )
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
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
                            .background(palette.accent.copy(alpha = 0.75f)),
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
                            .size(width = 17.dp, height = 11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.accent.copy(alpha = 0.30f)),
                    )
                    Box(
                        Modifier
                            .size(width = 11.dp, height = 11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(palette.accent.copy(alpha = 0.18f)),
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
                                .background(palette.accent.copy(alpha = 0.75f)),
                        )
                    }
                }
            }
        }
    }
}
