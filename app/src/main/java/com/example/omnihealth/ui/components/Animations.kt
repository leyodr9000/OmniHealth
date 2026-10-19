package com.example.omnihealth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * 首屏逐段入场：淡入 + 上浮，按 [index] 错开 45ms。
 *
 * 只给固定的头部/英雄区用；列表项不要用（回收复用会重复触发动画）。
 */
@Composable
fun AppearOnScreen(
    index: Int = 0,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 45L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(280, easing = FastOutSlowInEasing)) { it / 18 } +
            expandVertically(tween(280, easing = FastOutSlowInEasing), expandFrom = androidx.compose.ui.Alignment.Top),
    ) {
        content()
    }
}

/** 数字滚动到目标值（健康分、步数这类主指标）。 */
@Composable
fun animatedInt(target: Int, durationMillis: Int = 650): Int {
    val value by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "animatedInt",
    )
    return value
}

/** 0f..1f 的平滑进度，用于进度条 / 圆环 / 选中态。 */
@Composable
fun animatedFraction(target: Float, durationMillis: Int = 600): Float {
    val value by animateFloatAsState(
        targetValue = target.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "animatedFraction",
    )
    return value
}

/** 浮点滚动（饮水 L、睡眠 h 这类小数指标）。 */
@Composable
fun animatedFloat(target: Float, durationMillis: Int = 600): Float {
    val value by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "animatedFloat",
    )
    return value
}