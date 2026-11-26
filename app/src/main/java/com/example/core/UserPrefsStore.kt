package com.example.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.syloPrefsDataStore by preferencesDataStore(name = "sylo_user_prefs")

data class LocalAppPreferences(
    val darkTheme: Boolean = true,
    val lowDataMode: Boolean = false,
    val interfaceLanguage: String = "en",
    val notifyFollows: Boolean = true,
    val notifyLikesComments: Boolean = true,
    val notifyLiveAlerts: Boolean = true,
    val notifyNewsAlerts: Boolean = true,
    val onlineStatusVisible: Boolean = true
)

class UserPrefsStore(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val LOW_DATA_MODE = booleanPreferencesKey("low_data_mode")
        val INTERFACE_LANG = stringPreferencesKey("interface_lang")
        val NOTIFY_FOLLOWS = booleanPreferencesKey("notify_follows")
        val NOTIFY_LIKES = booleanPreferencesKey("notify_likes")
        val NOTIFY_LIVE = booleanPreferencesKey("notify_live")
        val NOTIFY_NEWS = booleanPreferencesKey("notify_news")
        val ONLINE_STATUS = booleanPreferencesKey("online_status")
    }

    val preferencesFlow: Flow<LocalAppPreferences> = context.syloPrefsDataStore.data.map { prefs ->
        LocalAppPreferences(
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            lowDataMode = prefs[Keys.LOW_DATA_MODE] ?: false,
            interfaceLanguage = prefs[Keys.INTERFACE_LANG] ?: "en",
            notifyFollows = prefs[Keys.NOTIFY_FOLLOWS] ?: true,
            notifyLikesComments = prefs[Keys.NOTIFY_LIKES] ?: true,
            notifyLiveAlerts = prefs[Keys.NOTIFY_LIVE] ?: true,
            notifyNewsAlerts = prefs[Keys.NOTIFY_NEWS] ?: true,
            onlineStatusVisible = prefs[Keys.ONLINE_STATUS] ?: true
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.syloPrefsDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setLowDataMode(enabled: Boolean) {
        context.syloPrefsDataStore.edit { it[Keys.LOW_DATA_MODE] = enabled }
    }

    suspend fun setInterfaceLanguage(code: String) {
        context.syloPrefsDataStore.edit { it[Keys.INTERFACE_LANG] = code }
    }

    suspend fun setNotificationPref(key: String, enabled: Boolean) {
        context.syloPrefsDataStore.edit {
            when (key) {
                "follows" -> it[Keys.NOTIFY_FOLLOWS] = enabled
                "likes" -> it[Keys.NOTIFY_LIKES] = enabled
                "live" -> it[Keys.NOTIFY_LIVE] = enabled
                "news" -> it[Keys.NOTIFY_NEWS] = enabled
                "online" -> it[Keys.ONLINE_STATUS] = enabled
            }
        }
    }
}
