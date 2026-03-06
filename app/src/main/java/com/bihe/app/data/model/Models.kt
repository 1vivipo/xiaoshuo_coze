package com.bihe.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: ProjectType,
    val description: String = "",
    val totalWordGoal: Int = 100000,
    val chapterWordGoal: Int = 10000,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val chapterCount: Int = 0
)

enum class ProjectType {
    NOVEL,      // 小说
    SHORT_DRAMA, // 短剧剧本
    COMIC_DRAMA, // 漫剧剧本
    PROMO_COPY   // 推文文案
}

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val volumeId: Long? = null,
    val title: String,
    val content: String = "",
    val outline: String = "",
    val wordCount: Int = 0,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val status: ChapterStatus = ChapterStatus.DRAFT
)

enum class ChapterStatus {
    DRAFT,      // 草稿
    OUTLINE,    // 大纲
    WRITING,    // 写作中
    COMPLETED   // 已完成
}

@Entity(tableName = "volumes")
data class Volume(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val title: String,
    val outline: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val alias: String = "",
    val description: String = "",
    val personality: String = "",
    val background: String = "",
    val relationships: String = "",
    val avatarPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "world_settings")
data class WorldSetting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val category: String,
    val title: String,
    val content: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "plot_nodes")
data class PlotNode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val chapterId: Long? = null,
    val title: String,
    val description: String = "",
    val nodeType: PlotNodeType,
    val orderIndex: Int = 0
)

enum class PlotNodeType {
    MAIN_PLOT,      // 主线剧情
    CONFLICT,       // 冲突点
    CLIMAX,         // 高潮
    FORESHADOW,     // 伏笔
    FORESHADOW_PAYOFF // 伏笔回收
}

@Entity(tableName = "drama_episodes")
data class DramaEpisode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val episodeNumber: Int,
    val title: String,
    val script: String = "",
    val duration: Int = 0,
    val status: EpisodeStatus = EpisodeStatus.SCRIPT,
    val createdAt: Long = System.currentTimeMillis()
)

enum class EpisodeStatus {
    SCRIPT,     // 剧本
    STORYBOARD, // 分镜
    RENDERING,  // 渲染中
    COMPLETED   // 已完成
}

@Entity(tableName = "storyboards")
data class Storyboard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val episodeId: Long,
    val shotNumber: Int,
    val shotType: ShotType,
    val description: String = "",
    val dialogue: String = "",
    val narration: String = "",
    val duration: Float = 3f,
    val imagePrompt: String = "",
    val imagePath: String? = null,
    val audioPath: String? = null
)

enum class ShotType {
    CLOSE_UP,   // 特写
    MEDIUM,     // 中景
    FULL,       // 全景
    LONG        // 远景
}

@Entity(tableName = "prompts")
data class PromptTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: PromptCategory,
    val template: String,
    val isDefault: Boolean = false
)

enum class PromptCategory {
    NOVEL_CONTINUE,     // 网文续写
    SCRIPT_CREATE,      // 剧本创作
    STORYBOARD_GEN,     // 分镜生成
    POLISH,             // 润色优化
    ANALYZE_EMULATE     // 拆书仿写
}
