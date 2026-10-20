package com.example.omnihealth.domain.util

import com.example.omnihealth.data.local.entity.FoodEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MealParserTest {

    private val rice = FoodEntity(
        id = 1, name = "米饭", nameEn = "Steamed rice", category = "主食",
        calories = 116f, protein = 2.6f, carbs = 25.9f, fat = 0.3f, servingGrams = 150f,
    )
    private val egg = FoodEntity(
        id = 2, name = "鸡蛋", nameEn = "Egg", category = "蛋白",
        calories = 155f, protein = 13f, carbs = 1.1f, fat = 11f, servingGrams = 50f,
    )
    private val soybean = FoodEntity(
        id = 3, name = "无糖豆浆", nameEn = "Soy milk", category = "饮品",
        calories = 31f, protein = 3f, carbs = 1.2f, fat = 1.6f, servingGrams = 250f,
    )
    private val foods = listOf(rice, egg, soybean)

    @Test
    fun splits_a_sentence_into_food_drafts() {
        val drafts = MealParser.parse("中午吃了一碗米饭、两个鸡蛋、一杯无糖豆浆", foods)
        assertEquals(3, drafts.size)
        assertEquals("米饭", drafts[0].food?.name)
        assertEquals("鸡蛋", drafts[1].food?.name)
        assertEquals("无糖豆浆", drafts[2].food?.name)
    }

    @Test
    fun explicit_grams_are_used_as_is() {
        val drafts = MealParser.parse("米饭 200 克", foods)
        assertEquals(200f, drafts.single().grams)
    }

    @Test
    fun vague_units_are_converted_through_serving_grams() {
        val drafts = MealParser.parse("两个鸡蛋", foods)
        // 鸡蛋一份 50 g，两个 = 100 g
        assertEquals(100f, drafts.single().grams)
        val bowl = MealParser.parse("一碗米饭", foods)
        // 米饭一份 150 g，一碗按两份
        assertEquals(300f, bowl.single().grams)
    }

    @Test
    fun unknown_food_is_reported_instead_of_invented() {
        val drafts = MealParser.parse("一份榴莲披萨", foods)
        assertNull(drafts.single().food)
        assertNull(drafts.single().grams)
        assertEquals(MealParser.Draft.MatchKind.NONE, drafts.single().matchKind)
    }

    @Test
    fun longest_name_wins() {
        val congee = FoodEntity(id = 9, name = "糙米饭", category = "主食", calories = 112f, servingGrams = 150f)
        val drafts = MealParser.parse("150 克糙米饭", foods + congee)
        assertEquals("糙米饭", drafts.single().food?.name)
        assertEquals(150f, drafts.single().grams)
    }

    @Test
    fun empty_input_yields_nothing() {
        assertTrue(MealParser.parse("   ", foods).isEmpty())
    }
}
