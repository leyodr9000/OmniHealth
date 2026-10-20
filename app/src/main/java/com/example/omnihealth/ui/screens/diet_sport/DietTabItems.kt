package com.example.omnihealth.ui.screens.diet_sport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.data.local.dao.RecentFood
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.MealTemplateEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.ui.components.AppearOnScreen
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.theme.OmniPalette
import com.example.omnihealth.ui.theme.quietClick

/**
 * 记录页「饮食」标签的列表内容。
 *
 * 参数名与原来的局部变量保持一致，所以内容块可以原样搬过来；
 * 与 ViewModel 的耦合只剩一组回调，Toast 反馈仍留在本文件。
 */
fun LazyListScope.dietTabItems(
    page: Int,
    diet: DietUiState,
    query: String,
    results: List<FoodEntity>,
    dictionary: List<FoodEntity>,
    recentFoods: List<RecentFood>,
    templates: List<MealTemplateEntity>,
    mealTypeNow: String,
    palette: OmniPalette,
    cardContext: android.content.Context,
    onQueryChange: (String) -> Unit,
    onPickFood: (FoodEntity) -> Unit,
    onPickRecent: (RecentFood) -> Unit,
    onLogTemplate: (MealTemplateEntity, (Int) -> Unit) -> Unit,
    onCopyMeal: ((Int) -> Unit) -> Unit,
    onSaveTemplate: ((Boolean) -> Unit) -> Unit,
    onOpenSmart: () -> Unit,
    onOpenPlan: () -> Unit,
    onEdit: (DietLogEntity) -> Unit,
    onDelete: (DietLogEntity) -> Unit,
) {
            item { AppearOnScreen(0) { CalorieSummaryCard(diet) } }

            item {
                SearchBar(
                    value = query,
                    placeholder = stringResource(R.string.diet_search_hint),
                    onValueChange = onQueryChange,
                )
            }

            if (page == 0) {
                if (recentFoods.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = stringResource(R.string.recents_title),
                                color = palette.textTertiary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(recentFoods, key = { "r-${it.foodId}" }) { recent ->
                                    Text(
                                        text = recent.foodName,
                                        color = palette.textPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(palette.cardFill)
                                            .quietClick {
                                                onPickRecent(recent)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                if (templates.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = stringResource(R.string.templates_title),
                                color = palette.textTertiary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(templates, key = { "t-${it.id}" }) { template ->
                                    Text(
                                        text = template.name,
                                        color = palette.textPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(palette.accent.copy(alpha = 0.09f))
                                            .quietClick {
                                                onLogTemplate(template) { count ->
                                                    android.widget.Toast.makeText(
                                                        cardContext,
                                                        cardContext.getString(R.string.copy_last_done, count),
                                                        android.widget.Toast.LENGTH_SHORT,
                                                    ).show()
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.copy_last_meal, mealLabel(mealTypeNow)),
                        color = palette.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(palette.accent.copy(alpha = 0.10f))
                            .quietClick {
                                onCopyMeal { count ->
                                    val message = if (count > 0) {
                                        cardContext.getString(R.string.copy_last_done, count)
                                    } else {
                                        cardContext.getString(R.string.copy_last_empty)
                                    }
                                    android.widget.Toast.makeText(cardContext, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.save_as_template),
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(palette.textPrimary.copy(alpha = 0.05f))
                            .quietClick {
                                onSaveTemplate { ok ->
                                    val message = if (ok) {
                                        cardContext.getString(R.string.save_template_done)
                                    } else {
                                        cardContext.getString(R.string.copy_last_empty)
                                    }
                                    android.widget.Toast.makeText(cardContext, message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton(
                            text = stringResource(R.string.smart_meal_entry),
                            modifier = Modifier.weight(1f),
                        ) { onOpenSmart() }
                        ActionButton(
                            text = stringResource(R.string.plan_entry),
                            modifier = Modifier.weight(1f),
                        ) {
                            onOpenPlan()
                        }
                    }
                }
            }

            // 今日记录放在字典之前：录完能马上看到、马上改。
            if (diet.logs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.diet_logged_title),
                        subtitle = "${diet.calories} kcal",
                    )
                }
                listOf(
                    MealType.BREAKFAST to R.string.meal_breakfast,
                    MealType.LUNCH to R.string.meal_lunch,
                    MealType.DINNER to R.string.meal_dinner,
                    MealType.SNACK to R.string.meal_snack,
                ).forEach { (mealId, labelRes) ->
                    val group = diet.logs.filter { it.mealType == mealId }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(labelRes),
                                color = palette.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = if (group.isEmpty()) {
                                    stringResource(R.string.meal_empty)
                                } else {
                                    "${group.sumOf { it.calories.toDouble() }.toInt()} kcal"
                                },
                                color = palette.textTertiary,
                                fontSize = 10.sp,
                            )
                        }
                    }
                    items(group, key = { "l-${it.id}" }, contentType = { "log" }) { log ->
                        Box(Modifier.animateItem()) {
                            LogRow(
                                log = log,
                                onEdit = { onEdit(log) },
                                onDelete = { onDelete(log) },
                            )
                        }
                    }
                }
            }

            if (query.isNotBlank()) {
                if (results.isEmpty()) {
                    item {
                        Column {
                            Text(
                                text = stringResource(R.string.diet_search_empty),
                                color = palette.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                            Text(
                                text = stringResource(R.string.empty_describe_meal),
                                color = palette.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.accent.copy(alpha = 0.10f))
                                    .quietClick { onOpenSmart() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                } else {
                    items(results, key = { "s-${it.id}" }, contentType = { "food" }) { food ->
                        Box(Modifier.animateItem()) {
                            FoodRow(food) { onPickFood(food) }
                        }
                    }
                }
            } else {
                item {
                    SectionHeader(
                        title = stringResource(R.string.diet_dict_title),
                        subtitle = stringResource(R.string.diet_dict_sub, dictionary.size),
                    )
                }
                items(dictionary.take(40), key = { "d-${it.id}" }, contentType = { "food" }) { food ->
                    Box(Modifier.animateItem()) {
                        FoodRow(food) { onPickFood(food) }
                    }
                }
            }
}
