package com.example.omnihealth.ui.screens.ai_advisor

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.omnihealth.R
import com.example.omnihealth.ui.components.SectionHeader
import com.example.omnihealth.ui.di.LocalHealthRepository
import com.example.omnihealth.ui.di.activityScopedViewModelOwner
import com.example.omnihealth.ui.theme.GlassCard
import com.example.omnihealth.ui.theme.OmniTheme
import com.example.omnihealth.ui.theme.bounceClick

@Composable
fun AiAdvisorScreen() {
    val repository = LocalHealthRepository.current
    val viewModel: AiAdvisorViewModel = viewModel(
        viewModelStoreOwner = activityScopedViewModelOwner(),
        factory = viewModelFactory { initializer { AiAdvisorViewModel(repository) } },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val palette = OmniTheme.palette
    val configuration = LocalConfiguration.current
    val useChinese = configuration.locales[0].language.startsWith("zh")

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.chat.size) {
        if (state.chat.isNotEmpty()) {
            listState.animateScrollToItem((state.chat.size - 1).coerceAtLeast(0))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 16.5.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.ai_title),
                            color = palette.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.ai_subtitle),
                            color = palette.textTertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (state.hasApiKey) {
                                    palette.accent
                                } else {
                                    palette.textTertiary.copy(alpha = 0.14f)
                                },
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = if (state.hasApiKey) {
                                stringResource(R.string.ai_provider_cloud, state.modelName)
                            } else {
                                stringResource(R.string.ai_provider_offline)
                            },
                            color = if (state.hasApiKey) Color.White else palette.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(palette.corner))
                        .background(palette.accent)
                        .bounceClick(pressScale = 0.97f) { viewModel.generateReport(useChinese) }
                        .padding(18.dp),
                ) {
                    Column {
                        Text(
                            text = if (state.loadingReport) {
                                stringResource(R.string.ai_generating)
                            } else {
                                stringResource(R.string.ai_generate)
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.ai_subtitle),
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }
            }

            if (state.report.isNotBlank()) {
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.ai_report_title),
                                color = palette.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = stringResource(R.string.ai_score, state.reportScore),
                                color = palette.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = state.report,
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = stringResource(R.string.ai_chat_title),
                    subtitle = if (state.offlineHint || !state.hasApiKey) {
                        stringResource(R.string.ai_provider_offline)
                    } else {
                        state.modelName
                    },
                    action = {
                        if (state.chat.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.ai_clear),
                                color = palette.accent,
                                fontSize = 11.sp,
                                modifier = Modifier.bounceClick(pressScale = 0.94f, haptics = false) {
                                    viewModel.clearChat()
                                },
                            )
                        }
                    },
                )
            }

            if (state.chat.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.ai_empty_chat),
                        color = palette.textTertiary,
                        fontSize = 11.sp,
                    )
                }
            }

            items(state.chat) { item ->
                ChatBubble(item)
            }

            if (state.sending) {
                item {
                    Text(
                        text = "···",
                        color = palette.textTertiary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }

        InputBar(
            value = input,
            placeholder = stringResource(R.string.ai_input_hint),
            sendLabel = stringResource(R.string.ai_send),
            onValueChange = { input = it },
            onSend = {
                val text = input.trim()
                if (text.isNotEmpty()) {
                    viewModel.send(text, useChinese)
                    input = ""
                }
            },
        )
    }
}

@Composable
private fun ChatBubble(item: ChatItem) {
    val palette = OmniTheme.palette
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (item.fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(horizontalAlignment = if (item.fromUser) Alignment.End else Alignment.Start) {
            Box(
                Modifier
                    .fillMaxWidth(0.86f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (item.fromUser) 16.dp else 4.dp,
                            bottomEnd = if (item.fromUser) 4.dp else 16.dp,
                        ),
                    )
                    .background(
                        if (item.fromUser) {
                            palette.accent
                        } else {
                            palette.bgMid
                        },
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = item.text,
                    color = if (item.fromUser) Color.White else palette.textPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
            if (!item.fromUser && item.offline) {
                Text(
                    text = stringResource(R.string.ai_provider_offline),
                    color = palette.textTertiary,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(start = 6.dp, top = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun InputBar(
    value: String,
    placeholder: String,
    sendLabel: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val palette = OmniTheme.palette
    Row(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.cardFill)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = palette.textTertiary, fontSize = 12.sp)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = palette.textPrimary, fontSize = 13.sp),
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(palette.accent)
                .bounceClick(pressScale = 0.94f, onClick = onSend)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(sendLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
