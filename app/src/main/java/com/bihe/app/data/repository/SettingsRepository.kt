package com.bihe.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val BASE_URL = stringPreferencesKey("base_url")
        private val MODEL_NAME = stringPreferencesKey("model_name")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val AUTO_SAVE = booleanPreferencesKey("auto_save")
        private val PASSWORD_ENABLED = booleanPreferencesKey("password_enabled")
        private val POWER_SAVING = booleanPreferencesKey("power_saving")
    }
    
    val apiKey: Flow<String> = context.dataStore.data.map { 
        it[API_KEY] ?: "sk-632f27c66a4445e091a101b29da605f3"
    }
    
    val baseUrl: Flow<String> = context.dataStore.data.map { 
        it[BASE_URL] ?: "https://api.deepseek.com"
    }
    
    val modelName: Flow<String> = context.dataStore.data.map { 
        it[MODEL_NAME] ?: "deepseek-chat"
    }
    
    val darkMode: Flow<Boolean> = context.dataStore.data.map { 
        it[DARK_MODE] ?: false
    }
    
    val autoSave: Flow<Boolean> = context.dataStore.data.map { 
        it[AUTO_SAVE] ?: true
    }
    
    val passwordEnabled: Flow<Boolean> = context.dataStore.data.map { 
        it[PASSWORD_ENABLED] ?: false
    }
    
    val powerSaving: Flow<Boolean> = context.dataStore.data.map { 
        it[POWER_SAVING] ?: false
    }
    
    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[API_KEY] = key }
    }
    
    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[BASE_URL] = url }
    }
    
    suspend fun setModelName(name: String) {
        context.dataStore.edit { it[MODEL_NAME] = name }
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }
    
    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_SAVE] = enabled }
    }
    
    suspend fun setPasswordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PASSWORD_ENABLED] = enabled }
    }
    
    suspend fun setPowerSaving(enabled: Boolean) {
        context.dataStore.edit { it[POWER_SAVING] = enabled }
    }
}
