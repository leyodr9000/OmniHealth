package com.example.omnihealth.ui.screens.diet_sport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.example.omnihealth.domain.util.MealPlanner
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick

/**
 * 「帮我搭配下一餐」面板。
 *
 * 候选由本地规则生成，营养按食物资料换算；这里只展示候选，
 * 只有用户点「就吃这一餐」才会写库。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanSheet(
    plans: List<MealPlanner.Plan>,
    onRegenerate: () -> Unit,
    onAccept: (MealPlanner.Plan, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                    text = stringResource(R.string.plan_title),
                    color = palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.plan_hint),
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Row(
                    Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
                                    if (meal == id) palette.accent.copy(alpha = 0.12f) else palette.cardFill,
                                )
                                .quietClick { meal = id }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (plans.isEmpty()) {
                    Text(
                        text = stringResource(R.string.plan_empty),
                        color = palette.textTertiary,
                        fontSize = 12.sp,
                    )
                }

                plans.forEach { plan ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.cardFill)
                            .padding(12.dp),
                    ) {
                        plan.items.forEach { item ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.food.name,
                                    color = palette.textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "${item.grams.toInt()} g · ${item.kcal.toInt()} kcal",
                                    color = palette.textTertiary,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                        Text(
                            text = stringResource(
                                R.string.plan_total,
                                plan.kcal.toInt(),
                                plan.protein.toInt(),
                            ),
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            text = stringResource(R.string.plan_estimate_note),
                            color = palette.textTertiary,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            text = stringResource(R.string.plan_accept),
                            color = palette.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.accent.copy(alpha = 0.10f))
                                .quietClick { onAccept(plan, meal) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    text = stringResource(R.string.plan_regenerate),
                    color = palette.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .quietClick(onClick = onRegenerate)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
