package com.example.omnihealth.ui.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.omnihealth.data.repository.HealthRepository

/**
 * 极简依赖注入：应用根部提供 Repository，各 ViewModel 直接取用。
 */
val LocalHealthRepository = staticCompositionLocalOf<HealthRepository> {
    error("HealthRepository not provided")
}
