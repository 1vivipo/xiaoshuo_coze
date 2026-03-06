package com.bihe.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.bihe.app.data.database.AppDatabase
import com.bihe.app.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BiHeApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    lateinit var database: AppDatabase
        private set
    
    lateinit var settingsRepository: SettingsRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        database = AppDatabase.getDatabase(this)
        settingsRepository = SettingsRepository(this)
        
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_WRITING,
                "续写服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台续写任务通知"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID_WRITING = "writing_service"
        
        lateinit var instance: BiHeApplication
            private set
    }
}
