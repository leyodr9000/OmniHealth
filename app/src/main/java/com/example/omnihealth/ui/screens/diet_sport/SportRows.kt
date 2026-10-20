package com.example.omnihealth.ui.screens.diet_sport

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import com.example.omnihealth.BuildConfig
import com.example.omnihealth.R
import com.example.omnihealth.data.local.entity.DietLogEntity
import com.example.omnihealth.data.local.entity.FoodEntity
import com.example.omnihealth.data.local.entity.MealType
import com.example.omnihealth.data.local.entity.WorkoutEntity
import com.example.omnihealth.domain.util.TimeUtils
import com.example.omnihealth.ui.components.AppearOnScreen
import com.example.omnihealth.ui.components.animatedInt
import com.example.omnihealth.ui.components.MetricTile
import com.example.omnihealth.ui.components.QuickLogType
import com.example.omnihealth.ui.components.ProgressRing
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.di.activityScopedViewModelOwner
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick
import com.example.omnihealth.ui.theme.quietClick
import java.util.Locale

/* 运动页的行组件：计时卡与训练记录行。 */

@Composable
internal fun WorkoutTimerCard(state: SportUiState, onStart: () -> Unit, onStop: () -> Unit) {
    val palette = OmniTheme.palette
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sport_timer),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                )
                Text(
                    text = formatTime(state.seconds),
                    color = palette.textPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (state.running) palette.danger else palette.accent)
                    .bounceClick(pressScale = 0.95f) { if (state.running) onStop() else onStart() }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Text(
                    text = if (state.running) stringResource(R.string.sport_stop) else stringResource(R.string.sport_start),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun WorkoutRow(workout: WorkoutEntity) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.cardFill)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(workout.type, color = palette.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(formatTime(workout.durationSec), color = palette.textTertiary, fontSize = 10.sp)
        }
        Text(
            text = stringResource(R.string.sport_burned, workout.calories),
            color = palette.success,
            fontSize = 11.sp,
        )
    }
}

internal fun formatTime(seconds: Int): String =
    String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
/** 删除后的撤销条：5 秒内可恢复，避免误删无法找回。 */