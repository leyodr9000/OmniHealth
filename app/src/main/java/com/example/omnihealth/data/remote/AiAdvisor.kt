package com.example.omnihealth.data.remote

import com.example.omnihealth.data.datastore.ApiConfig
import com.example.omnihealth.domain.model.HealthContext

/**
 * AI 门面：优先走用户配置的 OpenAI 兼容接口，任何失败都回落到 [OfflineAiEngine]。
 */
class AiAdvisor {

    suspend fun report(context: HealthContext, config: ApiConfig): AiAnswer {
        val offline = OfflineAiEngine.weeklyReport(context)
        if (config.apiKey.isBlank()) {
            return AiAnswer(text = offline, offline = true)
        }
        return runCatching {
            val service = AiClientFactory.create(config.baseUrl, config.apiKey)
            val system = if (context.useChinese) {
                "你是一位严谨、务实的私人健康教练。基于用户最近的数据给出简短的周报：" +
                    "1) 一句话总体评价；2) 分点给出 3 条可执行建议。不要编造数据，总字数不超过 220 字。"
            } else {
                "You are a pragmatic health coach. Based on the user's data, write a short weekly report: " +
                    "1) one-sentence assessment; 2) three actionable bullets. Do not invent data, keep it under 160 words."
            }
            val response = service.chat(
                authorization = "Bearer ${config.apiKey}",
                body = ChatRequest(
                    model = config.model,
                    temperature = config.temperature,
                    messages = listOf(
                        ChatMessage("system", system),
                        ChatMessage("user", context.promptBlock()),
                    ),
                ),
            )
            val text = response.content?.takeIf { it.isNotBlank() }
                ?: return@runCatching AiAnswer(offline, offline = true, error = "empty response")
            AiAnswer(text = text.trim(), offline = false)
        }.getOrElse { error ->
            AiAnswer(text = offline, offline = true, error = error.message)
        }
    }

    suspend fun chat(
        question: String,
        context: HealthContext,
        config: ApiConfig,
        history: List<ChatMessage> = emptyList(),
    ): AiAnswer {
        val offline = OfflineAiEngine.answer(question, context)
        if (config.apiKey.isBlank()) {
            return AiAnswer(text = offline, offline = true)
        }
        return runCatching {
            val service = AiClientFactory.create(config.baseUrl, config.apiKey)
            val system = if (context.useChinese) {
                "你是用户的私人健康教练。回答要具体、可执行，引用给出的数据，不要编造。控制在 150 字以内。"
            } else {
                "You are the user's health coach. Be specific and actionable, cite the data, never invent numbers. Under 120 words."
            }
            val messages = buildList {
                add(ChatMessage("system", system + "\n\n" + context.promptBlock()))
                addAll(history.takeLast(6))
                add(ChatMessage("user", question))
            }
            val response = service.chat(
                authorization = "Bearer ${config.apiKey}",
                body = ChatRequest(model = config.model, temperature = config.temperature, messages = messages),
            )
            val text = response.content?.takeIf { it.isNotBlank() }
                ?: return@runCatching AiAnswer(offline, offline = true, error = "empty response")
            AiAnswer(text = text.trim(), offline = false)
        }.getOrElse { error ->
            AiAnswer(text = offline, offline = true, error = error.message)
        }
    }
}
