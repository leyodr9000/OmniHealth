package com.example.omnihealth.ui.screens.diet_sport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.domain.util.MealParser
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick

/**
 * 「描述这一餐」的校对面板。
 *
 * 结构与方案的智能记餐流程一致：描述 → 识别 → 匹配候选 → 用户校对 → 保存。
 * 未匹配到食物的条目不会被保存，份量可逐条修改。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMealSheet(
    drafts: List<MealParser.Draft>,
    onTextChange: (String) -> Unit,
    onParse: (String) -> Unit,
    onGramsChange: (Int, Float) -> Unit,
    onRemove: (Int) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    var meal by remember { mutableStateOf(MealType.LUNCH) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = palette.barFill,
        tonalElevation = 0.dp,
        scrimColor = palette.textPrimary.copy(alpha = 0.26f),
    ) {
        LocalizedApp(LocalAppLanguage.current) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    text = stringResource(R.string.smart_meal_title),
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.smart_meal_hint),
                    color = palette.textTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onTextChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.smart_meal_placeholder),
                            color = palette.textTertiary,
                            fontSize = 12.sp,
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    shape = RoundedCornerShape(14.dp),
                )

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.smart_meal_parse),
                        color = palette.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.accent.copy(alpha = 0.10f))
                            .quietClick { onParse(text) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }

                if (drafts.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    drafts.forEachIndexed { index, draft ->
                        DraftRow(
                            draft = draft,
                            onGramsChange = { onGramsChange(index, it) },
                            onRemove = { onRemove(index) },
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    val ready = drafts.count { it.food != null && it.grams != null }
                    if (ready > 0) {
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                MealType.BREAKFAST to R.string.meal_breakfast,
                                MealType.LUNCH to R.string.meal_lunch,
                                MealType.DINNER to R.string.meal_dinner,
                                MealType.SNACK to R.string.meal_snack,
                            ).forEach { (id, label) ->
                                Text(
                                    text = stringResource(label),
                                    color = if (meal == id) palette.accent else palette.textTertiary,
                                    fontSize = 11.sp,
                                    fontWeight = if (meal == id) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            if (meal == id) palette.accent.copy(alpha = 0.12f)
                                            else palette.cardFill,
                                        )
                                        .quietClick { meal = id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.smart_meal_save, ready),
                            color = palette.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.accent.copy(alpha = 0.12f))
                                .quietClick { onSave(meal) }
                                .padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftRow(
    draft: MealParser.Draft,
    onGramsChange: (Float) -> Unit,
    onRemove: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.cardFill)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = draft.food?.name ?: draft.segment,
                color = if (draft.food != null) palette.textPrimary else palette.warning,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = if (draft.food == null) {
                    stringResource(R.string.smart_meal_unmatched)
                } else {
                    "${draft.grams?.toInt() ?: 0} g"
                },
                color = palette.textTertiary,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (draft.food != null) {
            Text(
                text = "−",
                color = palette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.textPrimary.copy(alpha = 0.06f))
                    .quietClick { onGramsChange((draft.grams ?: 0f) - 25f) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Text(
                text = "+",
                color = palette.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.textPrimary.copy(alpha = 0.06f))
                    .quietClick { onGramsChange((draft.grams ?: 0f) + 25f) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Text(
            text = "✕",
            color = palette.textTertiary,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .quietClick(onClick = onRemove)
                .padding(6.dp),
        )
    }
}
