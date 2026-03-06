package com.bihe.app.data.database

import android.content.Context
import androidx.room.*
import com.bihe.app.data.model.*

@Database(
    entities = [
        Project::class,
        Chapter::class,
        Volume::class,
        Character::class,
        WorldSetting::class,
        PlotNode::class,
        DramaEpisode::class,
        Storyboard::class,
        PromptTemplate::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun volumeDao(): VolumeDao
    abstract fun characterDao(): CharacterDao
    abstract fun worldSettingDao(): WorldSettingDao
    abstract fun plotNodeDao(): PlotNodeDao
    abstract fun dramaDao(): DramaDao
    abstract fun storyboardDao(): StoryboardDao
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bihe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
