package com.example.omnihealth.domain.util

import com.example.omnihealth.data.local.entity.FoodEntity

/**
 * 一句话记餐的**确定性**解析器。
 *
 * 职责边界（对应方案第八节）：
 * - 这里只做「切分 + 数量识别 + 食物名匹配」，全部是规则；
 * - 营养值一律由匹配到的食物资料 × 克数计算，解析器不产生任何营养数字；
 * - 份量不可确定时返回 null，由 UI 要求用户确认，不假装已知。
 */
object MealParser {

    /** 单位 → 相对「一份」的倍数。碗、杯这类模糊单位必须让用户可改。 */
    private val UNIT_FACTOR = mapOf(
        "碗" to 2.0f,
        "杯" to 1.0f,
        "份" to 1.0f,
        "个" to 1.0f,
        "片" to 0.5f,
        "根" to 1.0f,
        "盒" to 1.0f,
        "把" to 0.5f,
    )

    private val CHINESE_NUMBERS = mapOf(
        '一' to 1, '两' to 2, '二' to 2, '三' to 3, '四' to 4,
        '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9, '十' to 10,
    )

    data class Draft(
        val segment: String,
        val food: FoodEntity?,
        /** 估算克数；null 表示需要用户确认。 */
        val grams: Float?,
        /** 匹配置信：命中完整名称 / 只命中部分 / 未命中 */
        val matchKind: MatchKind,
    ) {
        enum class MatchKind { EXACT, PARTIAL, NONE }

        val needsConfirm: Boolean get() = grams == null
    }

    private val SEGMENT_SPLIT = Regex("[、,，。;；\\n]+|和|还有|加上|以及")

    fun parse(
        text: String,
        foods: List<FoodEntity>,
        defaultServingGrams: (FoodEntity) -> Float = { it.servingGrams },
    ): List<Draft> {
        if (text.isBlank()) return emptyList()
        return text.split(SEGMENT_SPLIT)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { segment -> draftFor(segment, foods, defaultServingGrams) }
    }

    private fun draftFor(
        segment: String,
        foods: List<FoodEntity>,
        servingOf: (FoodEntity) -> Float,
    ): Draft {
        val match = matchFood(segment, foods)
        val quantity = extractQuantity(segment)

        val grams: Float? = when {
            match == null -> null
            quantity == null -> servingOf(match.food)
            quantity.unit == null -> quantity.value
            else -> quantity.value * (UNIT_FACTOR[quantity.unit] ?: 1f) * servingOf(match.food)
        }

        return Draft(
            segment = segment,
            food = match?.food,
            grams = grams,
            matchKind = match?.kind ?: Draft.MatchKind.NONE,
        )
    }

    private data class FoodMatch(val food: FoodEntity, val kind: Draft.MatchKind)

    /** 最长命中优先：「糙米饭」不应被「米饭」抢走。 */
    private fun matchFood(segment: String, foods: List<FoodEntity>): FoodMatch? {
        var best: FoodMatch? = null
        var bestLength = 0
        foods.forEach { food ->
            listOf(food.name, food.nameEn).filter { it.isNotBlank() }.forEach { candidate ->
                if (segment.contains(candidate, ignoreCase = true) && candidate.length > bestLength) {
                    bestLength = candidate.length
                    best = FoodMatch(
                        food = food,
                        kind = if (segment.trim() == candidate || segment.trim().startsWith(candidate)) {
                            Draft.MatchKind.EXACT
                        } else {
                            Draft.MatchKind.PARTIAL
                        },
                    )
                }
            }
        }
        return best
    }

    private data class Quantity(val value: Float, val unit: String?)

    /** 识别「300 克 / 2 个 / 一碗 / 500ml」；纯数字视为克。 */
    private fun extractQuantity(segment: String): Quantity? {
        val unitPattern = Regex(
            "(\\d+(?:\\.\\d+)?|[一两二三四五六七八九十])\\s*(克|g|G|ml|ML|毫升|碗|杯|个|片|份|根|盒|把)",
        )
        unitPattern.find(segment)?.let { m ->
            val number = m.groupValues[1].toFloatOrNull()
                ?: CHINESE_NUMBERS[m.groupValues[1].first()]?.toFloat()
                ?: return@let
            val rawUnit = m.groupValues[2]
            val unit = when (rawUnit) {
                "g", "G", "克" -> null      // 克直接就是重量
                "ml", "ML", "毫升" -> null  // 近似 1 ml = 1 g
                else -> rawUnit
            }
            return Quantity(number, unit)
        }

        // 「一碗米饭」这种数字在前的写法已被上面的正则覆盖；再兜底纯数字
        val bare = Regex("(\\d+(?:\\.\\d+)?)").find(segment)
        bare?.let { return Quantity(it.groupValues[1].toFloatOrNull() ?: return null, null) }
        return null
    }
}
