package com.example.omnihealth.ui.components

import androidx.annotation.StringRes
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.frostBar
import kotlin.math.roundToInt

/**
 * 快捷记录类型。步长与快捷加减值随类型变化。
 */
enum class QuickLogType(
    @StringRes val labelRes: Int,
    val emoji: String,
    val unit: String,
    val defaultValue: Float,
    val step: Float,
    val deltas: List<Float>,
) {
    WEIGHT(R.string.quick_log_weight, "⚖️", "kg", 62f, 0.1f, listOf(-0.5f, -0.1f, 0.1f, 0.5f)),
    WATER(R.string.quick_log_water, "💧", "ml", 250f, 50f, listOf(100f, 200f, 250f, 500f)),
    SLEEP(R.string.quick_log_sleep, "🌙", "h", 7.5f, 0.5f, listOf(-1f, -0.5f, 0.5f, 1f)),
    STEPS(R.string.quick_log_steps, "👟", "step", 2000f, 500f, listOf(500f, 1000f, 2000f, 5000f)),
    MOOD(R.string.quick_log_mood, "😊", "pt", 3f, 1f, listOf(1f, 2f, 3f, 4f, 5f)),
    ;

    fun format(value: Float): String = when {
        this == WEIGHT -> String.format("%.1f", value)
        this == SLEEP -> String.format("%.1f", value)
        else -> value.roundToInt().toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogBottomSheet(
    onDismiss: () -> Unit,
    onSave: (QuickLogType, Float) -> Unit,
    initialType: QuickLogType = QuickLogType.WATER,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var type by remember { mutableStateOf(initialType) }
    var value by remember(initialType) { mutableStateOf(initialType.defaultValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = palette.barFill,
        tonalElevation = 0.dp,
        scrimColor = palette.textPrimary.copy(alpha = 0.28f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(palette.textTertiary.copy(alpha = 0.4f)),
            )
        },
    ) {
        LocalizedApp(LocalAppLanguage.current) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 26.dp),
            ) {
                Text(
                    text = stringResource(R.string.quick_log_title),
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.quick_log_subtitle),
                    color = palette.textTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(18.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QuickLogType.entries) { item ->
                        TypeChip(
                            emoji = item.emoji,
                            label = stringResource(item.labelRes),
                            selected = item == type,
                            onClick = {
                                type = item
                                value = item.defaultValue
                            },
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.textPrimary.copy(alpha = 0.03f))
                        .padding(vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepperButton("−") { value = (value - type.step).coerceAtLeast(0f) }
                        Text(
                            text = type.format(value),
                            color = palette.textPrimary,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 22.dp)
                                .width(130.dp),
                        )
                        StepperButton("+") { value += type.step }
                    }
                    Text(
                        text = type.unit,
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    type.deltas.forEach { delta ->
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.textPrimary.copy(alpha = 0.05f))
                                .bounceClick(pressScale = 0.92f, haptics = false) {
                                    value = (value + delta).coerceAtLeast(0f)
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (delta > 0) "+${type.format(delta)}" else type.format(delta),
                                color = palette.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                AppPrimaryButton(
                    text = stringResource(R.string.quick_log_save),
                    onClick = { onSave(type, value) },
                )
            }
        }
    }
}

@Composable
private fun TypeChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) {
                    Modifier.background(palette.accent)
                } else {
                    Modifier.frostBar(palette, RoundedCornerShape(16.dp))
                },
            )
            .bounceClick(pressScale = 0.93f, haptics = false, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = if (selected) androidx.compose.ui.graphics.Color.White else palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    val palette = OmniTheme.palette
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(palette.textPrimary.copy(alpha = 0.07f))
            .bounceClick(pressScale = 0.9f, haptics = false, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = palette.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}
