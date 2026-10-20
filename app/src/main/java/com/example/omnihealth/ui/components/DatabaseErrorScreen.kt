package com.example.omnihealth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omnihealth.R
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.quietClick

/**
 * 数据库无法打开时的兜底页。
 *
 * 迁移失败不应该只是一个闪退：这里保留应用可启动，告诉用户发生了什么，
 * 并给出「重试」；数据本身仍在磁盘上，不会被清掉。
 */
@Composable
fun DatabaseErrorScreen(cause: String, onRetry: () -> Unit) {
    val palette = OmniTheme.palette
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bgBottom)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.db_error_title),
            color = palette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.db_error_body),
            color = palette.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            text = cause,
            color = palette.textTertiary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
        GlassCard(modifier = Modifier.padding(top = 20.dp)) {
            Text(
                text = stringResource(R.string.db_error_retry),
                color = palette.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .quietClick(onClick = onRetry)
                    .padding(8.dp),
            )
        }
    }
}
