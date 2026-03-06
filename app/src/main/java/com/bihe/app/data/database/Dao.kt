package com.bihe.app.data.database

import androidx.room.*
import com.bihe.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE projectId = :projectId ORDER BY orderIndex")
    fun getChaptersByProject(projectId: Long): Flow<List<Chapter>>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): Chapter?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long
    
    @Update
    suspend fun updateChapter(chapter: Chapter)
    
    @Delete
    suspend fun deleteChapter(chapter: Chapter)
    
    @Query("SELECT MAX(orderIndex) FROM chapters WHERE projectId = :projectId")
    suspend fun getMaxOrderIndex(projectId: Long): Int?
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE projectId = :projectId ORDER BY orderIndex")
    fun getChaptersByProject(projectId: Long): Flow<List<Chapter>>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): Chapter?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long
    
    @Update
    suspend fun updateChapter(chapter: Chapter)
    
    @Delete
    suspend fun deleteChapter(chapter: Chapter)
    
    @Query("SELECT MAX(orderIndex) FROM chapters WHERE projectId = :projectId")
    suspend fun getMaxOrderIndex(projectId: Long): Int?
}

@Dao
interface VolumeDao {
    @Query("SELECT * FROM volumes WHERE projectId = :projectId ORDER BY orderIndex")
    fun getVolumesByProject(projectId: Long): Flow<List<Volume>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolume(volume: Volume): Long
    
    @Update
    suspend fun updateVolume(volume: Volume)
    
    @Delete
    suspend fun deleteVolume(volume: Volume)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE projectId = :projectId")
    fun getCharactersByProject(projectId: Long): Flow<List<Character>>
    
    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Long): Character?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long
    
    @Update
    suspend fun updateCharacter(character: Character)
    
    @Delete
    suspend fun deleteCharacter(character: Character)
}

@Dao
interface WorldSettingDao {
    @Query("SELECT * FROM world_settings WHERE projectId = :projectId ORDER BY category, orderIndex")
    fun getWorldSettingsByProject(projectId: Long): Flow<List<WorldSetting>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldSetting(setting: WorldSetting): Long
    
    @Update
    suspend fun updateWorldSetting(setting: WorldSetting)
    
    @Delete
    suspend fun deleteWorldSetting(setting: WorldSetting)
}

@Dao
interface PlotNodeDao {
    @Query("SELECT * FROM plot_nodes WHERE projectId = :projectId ORDER BY orderIndex")
    fun getPlotNodesByProject(projectId: Long): Flow<List<PlotNode>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlotNode(node: PlotNode): Long
    
    @Update
    suspend fun updatePlotNode(node: PlotNode)
    
    @Delete
    suspend fun deletePlotNode(node: PlotNode)
}

@Dao
interface DramaDao {
    @Query("SELECT * FROM drama_episodes WHERE projectId = :projectId ORDER BY episodeNumber")
    fun getEpisodesByProject(projectId: Long): Flow<List<DramaEpisode>>
    
    @Query("SELECT * FROM drama_episodes WHERE id = :id")
    suspend fun getEpisodeById(id: Long): DramaEpisode?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: DramaEpisode): Long
    
    @Update
    suspend fun updateEpisode(episode: DramaEpisode)
    
    @Delete
    suspend fun deleteEpisode(episode: DramaEpisode)
}

@Dao
interface StoryboardDao {
    @Query("SELECT * FROM storyboards WHERE episodeId = :episodeId ORDER BY shotNumber")
    fun getStoryboardsByEpisode(episodeId: Long): Flow<List<Storyboard>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryboard(storyboard: Storyboard): Long
    
    @Update
    suspend fun updateStoryboard(storyboard: Storyboard)
    
    @Delete
    suspend fun deleteStoryboard(storyboard: Storyboard)
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE category = :category")
    fun getPromptsByCategory(category: PromptCategory): Flow<List<PromptTemplate>>
    
    @Query("SELECT * FROM prompts WHERE isDefault = 1 AND category = :category LIMIT 1")
    suspend fun getDefaultPrompt(category: PromptCategory): PromptTemplate?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplate): Long
    
    @Update
    suspend fun updatePrompt(prompt: PromptTemplate)
    
    @Delete
    suspend fun deletePrompt(prompt: PromptTemplate)
}
