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

    fun getLoggedInUser(context: Context): Flow<Pair<String, String>?> {
        return context.dataStore.data.map { prefs ->
            val username = prefs[KEY_USERNAME]
            val displayName = prefs[KEY_DISPLAY_NAME]
            if (username != null && displayName != null) {
                Pair(username, displayName)
            } else {
                null
            }
        }
    }

    suspend fun saveLoggedInUser(context: Context, username: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_DISPLAY_NAME] = displayName
        }
    }

    suspend fun clearLoggedInUser(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_DISPLAY_NAME)
        }
    }
}