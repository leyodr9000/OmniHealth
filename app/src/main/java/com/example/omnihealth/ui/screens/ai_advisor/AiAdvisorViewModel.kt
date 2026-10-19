package com.example.omnihealth.ui.screens.ai_advisor

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnihealth.data.local.entity.AiReportEntity
import com.example.omnihealth.data.remote.ChatMessage
import com.example.omnihealth.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ChatItem(
    val text: String,
    val fromUser: Boolean,
    val offline: Boolean = false,
)

@Immutable
data class AiUiState(
    val report: String = "",
    val reportScore: Int = 0,
    val loadingReport: Boolean = false,
    val sending: Boolean = false,
    val chat: List<ChatItem> = emptyList(),
    val hasApiKey: Boolean = false,
    val modelName: String = "",
    val offlineHint: Boolean = false,
    val reportOffline: Boolean = false,
    val reportGeneratedAt: Long = 0L,
    val reportCoverageDays: Int = 7,
    val reportGaps: List<String> = emptyList(),
    val error: String? = null,
)

class AiAdvisorViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AiUiState())
    val state: StateFlow<AiUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.settings.first()
            val latest: AiReportEntity? = repository.latestWeeklyReport()
            _state.update {
                it.copy(
                    hasApiKey = settings.api.apiKey.isNotBlank(),
                    modelName = settings.api.model,
                    report = latest?.content ?: "",
                    reportScore = latest?.score ?: 0,
                )
            }
        }
    }

    fun generateReport(useChinese: Boolean) {
        if (_state.value.loadingReport) return
        _state.update { it.copy(loadingReport = true, error = null) }
        viewModelScope.launch {
            val answer = runCatching { repository.generateWeeklyReport(useChinese) }.getOrNull()
            val context = runCatching { repository.healthContext(useChinese) }.getOrNull()
            _state.update {
                it.copy(
                    loadingReport = false,
                    report = answer?.text ?: "",
                    reportScore = context?.score ?: it.reportScore,
                    offlineHint = answer?.offline == true,
            reportOffline = answer?.offline == true,
            reportGeneratedAt = System.currentTimeMillis(),
            reportGaps = buildList {
                if ((context?.avgSleepHours ?: 0f) <= 0f) add("sleep")
                if ((context?.waterMl ?: 0f) <= 0f) add("water")
                if ((context?.avgCalories ?: 0) <= 0) add("diet")
                if ((context?.avgSteps ?: 0) <= 0) add("steps")
            },
                    error = answer?.error,
                )
            }
        }
    }

    fun send(text: String, useChinese: Boolean) {
        val question = text.trim()
        if (question.isEmpty() || _state.value.sending) return
        val history = _state.value.chat.map {
            ChatMessage(if (it.fromUser) "user" else "assistant", it.text)
        }
        _state.update {
            it.copy(
                chat = it.chat + ChatItem(question, fromUser = true),
                sending = true,
                error = null,
            )
        }
        viewModelScope.launch {
            val answer = runCatching { repository.askAdvisor(question, useChinese, history) }.getOrNull()
            _state.update {
                it.copy(
                    sending = false,
                    chat = it.chat + ChatItem(
                        text = answer?.text ?: "…",
                        fromUser = false,
                        offline = answer?.offline == true,
                    ),
                    error = answer?.error,
                )
            }
        }
    }

    fun clearChat() {
        _state.update { it.copy(chat = emptyList(), error = null) }
    }
}
