package com.example.omnihealth.domain.util

import com.example.omnihealth.data.local.entity.FoodEntity
import kotlin.math.roundToInt

/**
 * 「帮我搭配」的**确定性**生成器。
 *
 * 对应方案第五节与第八节：
 * - 模型可以提出候选，但营养由食物资料计算、约束由本文件校验；
 * - 禁忌/过敏关键词在这里硬过滤，不依赖提示词；
 * - 候选只是**建议**，与已记录数据严格分开，不落库、不视为已吃。
 */
object MealPlanner {

    private val STAPLE = setOf("主食")
    private val PROTEIN = setOf("肉蛋", "水产", "豆制品")
    private val VEGETABLE = setOf("蔬菜", "水果")

    data class Item(val food: FoodEntity, val grams: Float) {
        val kcal: Float get() = food.calories * grams / 100f
        val protein: Float get() = food.protein * grams / 100f
    }

    data class Plan(
        val index: Int,
        val items: List<Item>,
        val note: String,
    ) {
        val kcal: Float get() = items.sumOf { it.kcal.toDouble() }.toFloat()
        val protein: Float get() = items.sumOf { it.protein.toDouble() }.toFloat()

        /** 估算属性必须随结果一起展示，不假装是精确值。 */
        val estimateNote: String get() = "按参考份量估算，实际以称重为准"
    }

    /**
     * @param budgetKcal 这一餐的目标热量（一般取「今日剩余」的一部分，不能直接用剩余全量）
     * @param avoid 禁忌/不吃的关键词，命中食物名（中英）即排除
     * @param count 候选数量
     */
    fun plan(
        foods: List<FoodEntity>,
        budgetKcal: Float,
        avoid: List<String> = emptyList(),
        count: Int = 3,
    ): List<Plan> {
        val safeBudget = budgetKcal.coerceIn(250f, 1200f)
        val blocked = avoid.map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        fun usable(categories: Set<String>): List<FoodEntity> = foods
            .filter { it.category in categories }
            .filter { food ->
                blocked.none { term ->
                    food.name.lowercase().contains(term) || food.nameEn.lowercase().contains(term)
                }
            }
            .sortedBy { it.name }

        val staples = usable(STAPLE)
        val proteins = usable(PROTEIN)
        val veg = usable(VEGETABLE)

        if (staples.isEmpty() || proteins.isEmpty()) return emptyList()

        return (0 until count).mapNotNull { index ->
            val staple = staples.pick(index) ?: return@mapNotNull null
            val protein = proteins.pick(index + 1) ?: return@mapNotNull null
            val vegetable = veg.pick(index + 2)

            val items = buildList {
                add(staple.portion(safeBudget * 0.45f))
                add(protein.portion(safeBudget * 0.35f))
                if (vegetable != null) add(vegetable.portion(safeBudget * 0.20f))
            }

            Plan(
                index = index,
                items = items,
                note = if (vegetable == null) "缺少蔬菜候选，只有主食与蛋白" else "主食 + 蛋白 + 蔬果",
            )
        }
    }


    private fun List<FoodEntity>.pick(offset: Int): FoodEntity? =
        if (isEmpty()) null else this[offset.mod(size)]

    private fun FoodEntity.portion(kcalTarget: Float): Item {
        val per100 = if (calories <= 0f) 100f else calories
        val raw = kcalTarget / per100 * 100f
        val rounded = (raw / 10f).roundToInt() * 10f
        return Item(this, rounded.coerceIn(30f, 400f))
    }
}
