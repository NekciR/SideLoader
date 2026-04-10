package com.rickendy.sideloader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val KEY_THEME = stringPreferencesKey("theme")

    //AUTH
    fun getLoggedInUser(context: Context): Flow<SavedUser?> {
        return context.dataStore.data.map { prefs ->
            val username = prefs[KEY_USERNAME]
            val displayName = prefs[KEY_DISPLAY_NAME]
            val accessToken = prefs[KEY_ACCESS_TOKEN]
            val refreshToken = prefs[KEY_REFRESH_TOKEN]
            if (username != null && accessToken != null) {
                SavedUser(username, displayName ?: username, accessToken, refreshToken ?: "")
            } else {
                null
            }
        }
    }

    suspend fun saveLoggedInUser(
        context: Context,
        username: String,
        displayName: String,
        accessToken: String,
        refreshToken: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_DISPLAY_NAME] = displayName
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearLoggedInUser(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    //Theme
    fun getTheme(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[UserPreferences.KEY_THEME] ?: "system"
        }
    }

    suspend fun saveTheme(context: Context, theme: String) {
        context.dataStore.edit { prefs ->
            prefs[UserPreferences.KEY_THEME] = theme
        }
    }
}

data class SavedUser(
    val username: String,
    val displayName: String,
    val accessToken: String,
    val refreshToken: String
)


