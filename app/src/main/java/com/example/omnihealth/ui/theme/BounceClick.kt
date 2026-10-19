package com.example.omnihealth.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * MIUIX 物理弹性按压缩放。
 *
 * 按下立即缩到 [pressScale]，松手用「欠阻尼弹簧」回弹，产生轻微过冲的物理手感。
 * 全程只走 UI 线程的动画，不额外分配层。
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressScale: Float = 0.94f,
    haptics: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.38f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bounceScale",
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(enabled, onClick) {
        if (!enabled) return@pointerInput
        detectTapGestures(
            onPress = {
                pressed = true
                try {
                    awaitRelease()
                } finally {
                    pressed = false
                }
            },
            onTap = {
                if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        )
    }
}
