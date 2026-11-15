package com.brainrush.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.brainrush.app.domain.models.AppSettings
import com.brainrush.app.domain.models.UserProgress
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

val Context.brainRushDataStore: DataStore<Preferences> by preferencesDataStore(name = "brain_rush_offline_store")

class BrainRushDataStore(private val dataStore: DataStore<Preferences>) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private object Keys {
        val USER_PROGRESS = stringPreferencesKey("user_progress_json")
        val APP_SETTINGS = stringPreferencesKey("app_settings_json")
    }

    val userProgressFlow: Flow<UserProgress> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.USER_PROGRESS]?.let { runCatching { json.decodeFromString<UserProgress>(it) }.getOrNull() } ?: UserProgress() }

    val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[Keys.APP_SETTINGS]?.let { runCatching { json.decodeFromString<AppSettings>(it) }.getOrNull() } ?: AppSettings() }

    suspend fun saveUserProgress(progress: UserProgress) {
        dataStore.edit { it[Keys.USER_PROGRESS] = json.encodeToString(progress) }
    }

    suspend fun saveAppSettings(settings: AppSettings) {
        dataStore.edit { it[Keys.APP_SETTINGS] = json.encodeToString(settings) }
    }

    suspend fun resetAllProgress() {
        dataStore.edit { it[Keys.USER_PROGRESS] = json.encodeToString(UserProgress()) }
    }
}
