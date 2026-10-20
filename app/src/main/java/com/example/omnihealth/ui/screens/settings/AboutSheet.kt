@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.omnihealth.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.data.datastore.ActivityLevel
import com.example.omnihealth.data.datastore.ApiConfig
import com.example.omnihealth.data.datastore.Gender
import com.example.omnihealth.data.datastore.HealthGoal
import com.example.omnihealth.data.datastore.UserProfile
import com.example.omnihealth.ui.components.AppPrimaryButton
import com.example.omnihealth.ui.i18n.LocalAppLanguage
import com.example.omnihealth.ui.i18n.LocalizedApp
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick
import com.example.omnihealth.ui.theme.bounceClick
import java.util.Locale

/* 关于弹窗。拆分自原 EditSheets.kt。 */

@Composable
fun AboutSheet(
    version: String,
    onDismiss: () -> Unit,
) {
    val palette = OmniTheme.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = palette.barFill,
        tonalElevation = 0.dp,
        scrimColor = palette.textPrimary.copy(alpha = 0.28f),
    ) {
        LocalizedApp(LocalAppLanguage.current) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 30.dp),
            ) {
                SheetHeader(
                    title = stringResource(R.string.about_title),
                    subtitle = stringResource(R.string.about_version, version),
                )

                Text(
                    text = stringResource(R.string.about_privacy),
                    color = palette.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.about_licenses_title),
                    color = palette.textTertiary,
                    fontSize = 10.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.about_licenses),
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "github.com/leyodr9000/OmniHealth",
                    color = palette.accent,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

/** 提醒时段步进条：标签 + 当前值 + 减/加。 */