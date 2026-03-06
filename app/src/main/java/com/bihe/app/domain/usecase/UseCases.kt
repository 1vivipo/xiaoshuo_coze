package com.bihe.app.domain.usecase

import com.bihe.app.data.database.AppDatabase
import com.bihe.app.data.model.*
import com.bihe.app.domain.ai.DeepSeekService
import kotlinx.coroutines.flow.first

class ContinueWritingUseCase(
    private val database: AppDatabase,
    private val deepSeekService: DeepSeekService
) {
    suspend operator fun invoke(
        chapterId: Long,
        targetWords: Int = 2000,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            val chapter = database.chapterDao().getChapterById(chapterId)
                ?: return Result.failure(Exception("章节不存在"))
            
            val project = database.projectDao().getProjectById(chapter.projectId)
                ?: return Result.failure(Exception("项目不存在"))
            
            // 获取人物设定
            val characters = database.characterDao()
                .getCharactersByProject(project.id)
                .first()
                .joinToString("\n") { "${it.name}：${it.description}" }
            
            // 获取世界观设定
            val worldSettings = database.worldSettingDao()
                .getWorldSettingsByProject(project.id)
                .first()
                .joinToString("\n") { "${it.title}：${it.content}" }
            
            val result = deepSeekService.continueWriting(
                context = chapter.content.takeLast(2000),
                outline = chapter.outline,
                characters = characters,
                worldSetting = worldSettings,
                targetWords = targetWords
            )
            
            result.fold(
                onSuccess = { newContent ->
                    val updatedContent = chapter.content + "\n" + newContent
                    database.chapterDao().updateChapter(
                        chapter.copy(
                            content = updatedContent,
                            wordCount = updatedContent.length,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    database.projectDao().updateWordCount(project.id)
                    Result.success(newContent)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GenerateScriptUseCase(
    private val deepSeekService: DeepSeekService
) {
    suspend operator fun invoke(
        title: String,
        episode: Int,
        totalEpisodes: Int,
        characters: String,
        outline: String,
        previousContent: String = ""
    ): Result<String> {
        val prompt = """
            你是一位专业的短剧编剧，请创作第${episode}集剧本。
            
            剧名：$title
            集数：第${episode}集 / 共${totalEpisodes}集
            
            人物设定：
            $characters
            
            剧情大纲：
            $outline
            
            前情提要：
            $previousContent
            
            要求：
            1. 单集时长2-3分钟，字数800-1200字
            2. 格式：镜号、画面内容、台词、旁白、时长、音效
            3. 每集必须有明确的冲突点和悬念
            4. 台词简洁有力
            
            请创作剧本：
        """.trimIndent()
        
        return deepSeekService.chat(
            messages = listOf(
                com.bihe.app.domain.ai.Message("user", prompt)
            ),
            maxTokens = 4096
        )
    }
}

class GenerateStoryboardUseCase(
    private val deepSeekService: DeepSeekService
) {
    suspend operator fun invoke(script: String): Result<String> {
        val prompt = """
            请将以下剧本转换为详细的分镜脚本：
            
            $script
            
            要求：
            1. 每个镜头2-5秒
            2. 标注镜头类型：特写/中景/全景/远景
            3. 描述画面构图、人物动作、表情
            4. 生成文生图提示词（英文）
            5. 标注台词、旁白、音效
            
            格式：
            【镜号】
            【镜头类型】
            【画面描述】
            【台词】
            【旁白】
            【音效】
            【时长】
            【文生图提示词】
        """.trimIndent()
        
        return deepSeekService.chat(
            messages = listOf(
                com.bihe.app.domain.ai.Message("user", prompt)
            ),
            maxTokens = 4096
        )
    }
}

class ExtractHighlightsUseCase(
    private val deepSeekService: DeepSeekService
) {
    suspend operator fun invoke(content: String, duration: Int = 60): Result<String> {
        val prompt = """
            请从以下小说内容中提取最吸引人的片段，生成${duration}秒的推文视频文案：
            
            $content
            
            要求：
            1. 提取最吸引人的冲突点、爽点
            2. 文案节奏快，有悬念
            3. 适合抖音/快手等短视频平台
            4. 时长约${duration}秒
            
            请生成文案：
        """.trimIndent()
        
        return deepSeekService.chat(
            messages = listOf(
                com.bihe.app.domain.ai.Message("user", prompt)
            ),
            maxTokens = 2048
        )
    }
}
