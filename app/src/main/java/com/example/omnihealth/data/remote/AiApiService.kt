package com.example.omnihealth.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val stream: Boolean = false,
)

@Serializable
data class ChatChoice(
    val index: Int = 0,
    val message: ChatMessage = ChatMessage("assistant", ""),
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class ChatResponse(
    val id: String = "",
    val model: String = "",
    val choices: List<ChatChoice> = emptyList(),
) {
    val content: String? get() = choices.firstOrNull()?.message?.content
}

/**
 * OpenAI 兼容接口（DeepSeek / 任意中转站 / 本地 Ollama 均可）。
 */
interface AiApiService {
    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body body: ChatRequest,
    ): ChatResponse
}

object AiClientFactory {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun create(baseUrl: String, apiKey: String): AiApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $apiKey")
                        .build(),
                )
            }
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(if (normalized.contains("/v1/")) normalized else normalized + "v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AiApiService::class.java)
    }
}
