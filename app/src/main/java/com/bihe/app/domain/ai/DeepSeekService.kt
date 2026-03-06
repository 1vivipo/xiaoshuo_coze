package com.bihe.app.domain.ai

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 4096,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class DeepSeekResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

interface DeepSeekApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: DeepSeekRequest): DeepSeekResponse
}

class DeepSeekService(private var apiKey: String, private var baseUrl: String = "https://api.deepseek.com") {
    
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private var retrofit = createRetrofit()
    
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val api: DeepSeekApi by lazy { retrofit.create(DeepSeekApi::class.java) }
    
    suspend fun chat(
        messages: List<Message>,
        model: String = "deepseek-chat",
        temperature: Float = 0.7f,
        maxTokens: Int = 4096
    ): Result<String> {
        return try {
            val request = DeepSeekRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                max_tokens = maxTokens
            )
            val response = api.chatCompletion(request)
            Result.success(response.choices.firstOrNull()?.message?.content ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun continueWriting(
        context: String,
        outline: String = "",
        characters: String = "",
        worldSetting: String = "",
        targetWords: Int = 2000,
        style: String = "网文风格"
    ): Result<String> {
        val systemPrompt = buildSystemPrompt(style)
        val userPrompt = buildUserPrompt(context, outline, characters, worldSetting, targetWords)
        
        return chat(
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = userPrompt)
            ),
            maxTokens = minOf(targetWords * 2, 8000)
        )
    }
    
    private fun buildSystemPrompt(style: String): String {
        return """你是一位专业的网文创作助手，擅长$style写作。
你的任务是根据提供的大纲、人物设定、世界观设定，续写小说内容。
要求：
1. 保持人物性格一致，不崩人设
2. 剧情紧凑，有冲突有爽点
3. 文笔流畅，符合网文阅读习惯
4. 严格按照大纲推进剧情
5. 注意前后文连贯，不出现矛盾
6. 直接输出续写内容，不要有任何解释说明"""
    }
    
    private fun buildUserPrompt(
        context: String,
        outline: String,
        characters: String,
        worldSetting: String,
        targetWords: Int
    ): String {
        val sb = StringBuilder()
        sb.append("请续写以下内容，目标字数约${targetWords}字：\n\n")
        
        if (characters.isNotBlank()) {
            sb.append("【人物设定】\n$characters\n\n")
        }
        
        if (worldSetting.isNotBlank()) {
            sb.append("【世界观设定】\n$worldSetting\n\n")
        }
        
        if (outline.isNotBlank()) {
            sb.append("【本章大纲】\n$outline\n\n")
        }
        
        sb.append("【前文内容】\n$context\n\n")
        sb.append("请续写：")
        
        return sb.toString()
    }
    
    fun updateApiKey(newKey: String) {
        this.apiKey = newKey
    }
    
    fun updateBaseUrl(newUrl: String) {
        this.baseUrl = newUrl
        this.retrofit = createRetrofit()
    }
    
    companion object {
        const val DEFAULT_API_KEY = "sk-632f27c66a4445e091a101b29da605f3"
        const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        const val DEFAULT_MODEL = "deepseek-chat"
    }
}
