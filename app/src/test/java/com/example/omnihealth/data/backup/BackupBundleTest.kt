package com.example.omnihealth.data.backup

import com.example.omnihealth.data.local.entity.DietLogEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupBundleTest {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    @Test
    fun round_trip_preserves_version_and_records() {
        val bundle = BackupBundle(
            version = 1,
            createdAt = 1_700_000_000_000,
            dietLogs = listOf(
                DietLogEntity(
                    id = 7,
                    foodName = "米饭",
                    grams = 200f,
                    mealType = "LUNCH",
                    calories = 232f,
                    protein = 5.2f,
                    carbs = 51.8f,
                    fat = 0.6f,
                    epochDay = 20_000,
                ),
            ),
        )
        val encoded = json.encodeToString(BackupBundle.serializer(), bundle)
        assertTrue(encoded.contains("\"version\""))
        val decoded = json.decodeFromString(BackupBundle.serializer(), encoded)
        assertEquals(1, decoded.version)
        assertEquals(1, decoded.dietLogs.size)
        assertEquals(7L, decoded.dietLogs.first().id)
        assertEquals(1, decoded.totalRecords)
    }

    @Test
    fun unknown_fields_are_ignored_for_forward_compatibility() {
        val raw = """{"version":2,"createdAt":1,"vitals":[],"futureField":"x"}"""
        val decoded = json.decodeFromString(BackupBundle.serializer(), raw)
        assertEquals(2, decoded.version)
        assertEquals(0, decoded.totalRecords)
    }
}