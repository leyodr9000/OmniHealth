package com.example.omnihealth.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.omniDataStore: DataStore<Preferences> by preferencesDataStore(name = "omni_settings")

enum class Gender { MALE, FEMALE, OTHER }

/** 活动系数（Mifflin-St Jeor → TDEE）。 */
enum class ActivityLevel(val factor: Float) {
    SEDENTARY(1.2f),
    LIGHT(1.375f),
    MODERATE(1.55f),
    ACTIVE(1.725f),
    ATHLETE(1.9f),
}

enum class HealthGoal(val calorieDelta: Int) {
    LOSE(-400),
    MAINTAIN(0),
    GAIN(300),
}

data class UserProfile(
    val nickname: String = "Kai",
    val gender: Gender = Gender.MALE,
    val age: Int = 26,
    val heightCm: Float = 178f,
    val weightKg: Float = 62.4f,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: HealthGoal = HealthGoal.MAINTAIN,
)

data class ApiConfig(
    val baseUrl: String = "https://api.deepseek.com",
    val apiKey: String = "",
    val model: String = "deepseek-chat",
    val temperature: Float = 0.7f,
)

/** 全局设置快照。 */
data class AppSettings(
    val themeId: String = "miuix_glass",
    val layoutId: String = "layout_a",
    val languageId: String = "system",
    val frameworkId: String = "material3",
    val profile: UserProfile = UserProfile(),
    val api: ApiConfig = ApiConfig(),
    val stepGoal: Int = 10_000,
    val waterGoalMl: Int = 2_500,
    val sleepGoalMin: Int = 480,
    val calorieGoal: Int = 2_000,
    val waterReminder: Boolean = false,
    val sleepReminder: Boolean = false,
    val onboarded: Boolean = false,
)

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_id")
        val layout = stringPreferencesKey("layout_id")
        val language = stringPreferencesKey("language_id")
        val framework = stringPreferencesKey("framework_id")

        val nickname = stringPreferencesKey("profile_nickname")
        val gender = stringPreferencesKey("profile_gender")
        val age = intPreferencesKey("profile_age")
        val height = floatPreferencesKey("profile_height")
        val weight = floatPreferencesKey("profile_weight")
        val activity = stringPreferencesKey("profile_activity")
        val goal = stringPreferencesKey("profile_goal")

        val apiBaseUrl = stringPreferencesKey("api_base_url")
        val apiKey = stringPreferencesKey("api_key")
        val apiModel = stringPreferencesKey("api_model")
        val apiTemperature = floatPreferencesKey("api_temperature")

        val stepGoal = intPreferencesKey("goal_steps")
        val waterGoal = intPreferencesKey("goal_water")
        val sleepGoal = intPreferencesKey("goal_sleep")
        val calorieGoal = intPreferencesKey("goal_calories")
        val onboarded = booleanPreferencesKey("onboarded")
        val waterReminder = booleanPreferencesKey("reminder_water")
        val sleepReminder = booleanPreferencesKey("reminder_sleep")
    }

    val settings: Flow<AppSettings> = context.omniDataStore.data.map { p ->
        AppSettings(
            themeId = p[Keys.theme] ?: "miuix_glass",
            layoutId = p[Keys.layout] ?: "layout_a",
            languageId = p[Keys.language] ?: "system",
            frameworkId = p[Keys.framework] ?: "material3",
            profile = UserProfile(
                nickname = p[Keys.nickname] ?: "Kai",
                gender = p[Keys.gender]?.let { runCatching { Gender.valueOf(it) }.getOrNull() } ?: Gender.MALE,
                age = p[Keys.age] ?: 26,
                heightCm = p[Keys.height] ?: 178f,
                weightKg = p[Keys.weight] ?: 62.4f,
                activityLevel = p[Keys.activity]?.let { runCatching { ActivityLevel.valueOf(it) }.getOrNull() }
                    ?: ActivityLevel.MODERATE,
                goal = p[Keys.goal]?.let { runCatching { HealthGoal.valueOf(it) }.getOrNull() } ?: HealthGoal.MAINTAIN,
            ),
            api = ApiConfig(
                baseUrl = p[Keys.apiBaseUrl] ?: "https://api.deepseek.com",
                apiKey = p[Keys.apiKey] ?: "",
                model = p[Keys.apiModel] ?: "deepseek-chat",
                temperature = p[Keys.apiTemperature] ?: 0.7f,
            ),
            stepGoal = p[Keys.stepGoal] ?: 10_000,
            waterGoalMl = p[Keys.waterGoal] ?: 2_500,
            sleepGoalMin = p[Keys.sleepGoal] ?: 480,
            calorieGoal = p[Keys.calorieGoal] ?: 2_000,
            waterReminder = p[Keys.waterReminder] ?: false,
            sleepReminder = p[Keys.sleepReminder] ?: false,
            onboarded = p[Keys.onboarded] ?: false,
        )
    }

    suspend fun setThemeId(id: String) = context.omniDataStore.edit { it[Keys.theme] = id }

    suspend fun setLayoutId(id: String) = context.omniDataStore.edit { it[Keys.layout] = id }

    suspend fun setLanguageId(id: String) = context.omniDataStore.edit { it[Keys.language] = id }

    suspend fun setFrameworkId(id: String) = context.omniDataStore.edit { it[Keys.framework] = id }

    suspend fun updateProfile(profile: UserProfile) = context.omniDataStore.edit { p ->
        p[Keys.nickname] = profile.nickname
        p[Keys.gender] = profile.gender.name
        p[Keys.age] = profile.age
        p[Keys.height] = profile.heightCm
        p[Keys.weight] = profile.weightKg
        p[Keys.activity] = profile.activityLevel.name
        p[Keys.goal] = profile.goal.name
    }

    suspend fun updateApi(api: ApiConfig) = context.omniDataStore.edit { p ->
        p[Keys.apiBaseUrl] = api.baseUrl
        p[Keys.apiKey] = api.apiKey
        p[Keys.apiModel] = api.model
        p[Keys.apiTemperature] = api.temperature
    }

    suspend fun updateGoals(steps: Int, waterMl: Int, sleepMin: Int, calories: Int) =
        context.omniDataStore.edit { p ->
            p[Keys.stepGoal] = steps
            p[Keys.waterGoal] = waterMl
            p[Keys.sleepGoal] = sleepMin
            p[Keys.calorieGoal] = calories
        }

    suspend fun setOnboarded(value: Boolean) = context.omniDataStore.edit { it[Keys.onboarded] = value }

    suspend fun setWaterReminder(enabled: Boolean) =
        context.omniDataStore.edit { it[Keys.waterReminder] = enabled }

    suspend fun setSleepReminder(enabled: Boolean) =
        context.omniDataStore.edit { it[Keys.sleepReminder] = enabled }
}
