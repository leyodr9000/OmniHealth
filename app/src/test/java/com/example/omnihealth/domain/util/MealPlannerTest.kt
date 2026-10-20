package com.example.omnihealth.domain.util

import com.example.omnihealth.data.local.entity.FoodEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealPlannerTest {

    private fun food(name: String, category: String, kcal: Float, protein: Float = 5f) =
        FoodEntity(name = name, category = category, calories = kcal, protein = protein, servingGrams = 100f)

    private val foods = listOf(
        food("米饭", "主食", 116f),
        food("糙米饭", "主食", 111f),
        food("鸡胸肉", "肉蛋", 133f, 30f),
        food("鸡蛋", "肉蛋", 143f, 13f),
        food("西兰花", "蔬菜", 34f, 2.8f),
        food("菠菜", "蔬菜", 24f, 2.6f),
    )

    @Test
    fun plans_have_staple_protein_and_vegetable() {
        val plans = MealPlanner.plan(foods, budgetKcal = 600f, count = 3)
        assertEquals(3, plans.size)
        plans.forEach { plan ->
            assertTrue(plan.items.any { it.food.category == "主食" })
            assertTrue(plan.items.any { it.food.category == "肉蛋" })
            assertTrue(plan.items.any { it.food.category == "蔬菜" })
        }
    }

    @Test
    fun total_calories_stay_close_to_budget() {
        val plan = MealPlanner.plan(foods, budgetKcal = 600f, count = 1).single()
        // 允许 ±30%，但绝不应该超出预算一倍
        assertTrue(plan.kcal in 400f..820f)
        assertTrue(plan.kcal < 1200f)
    }

    @Test
    fun avoid_terms_are_filtered_out() {
        val plans = MealPlanner.plan(foods, 600f, avoid = listOf("鸡蛋"), count = 3)
        assertTrue(plans.isNotEmpty())
        plans.forEach { plan ->
            assertFalse(plan.items.any { it.food.name.contains("鸡蛋") })
        }
    }

    @Test
    fun deterministic_for_same_input() {
        val a = MealPlanner.plan(foods, 600f, count = 3)
        val b = MealPlanner.plan(foods, 600f, count = 3)
        assertEquals(a.map { it.items.map { i -> i.food.name } }, b.map { it.items.map { i -> i.food.name } })
    }

    @Test
    fun candidate_variety_changes_picks() {
        val plans = MealPlanner.plan(foods, 600f, count = 3)
        val firsts = plans.map { it.items.first().food.name }.distinct()
        assertTrue(firsts.size >= 2)
    }
}
