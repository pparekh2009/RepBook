package com.priyanshparekh.repbook.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

open class AppPreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val AUTO_ADVANCE = booleanPreferencesKey("auto_advance")
        val REST_BETWEEN_SETS = intPreferencesKey("rest_between_sets")
        val REST_BETWEEN_EXERCISES = intPreferencesKey("rest_between_exercises")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    open val appPreferencesFlow: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            autoAdvance = prefs[Keys.AUTO_ADVANCE] ?: false,
            restBetweenSets = prefs[Keys.REST_BETWEEN_SETS] ?: 30,
            restBetweenExercises = prefs[Keys.REST_BETWEEN_EXERCISES] ?: 90,
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        )
    }

    open suspend fun updateAutoAdvance(value: Boolean) {
        dataStore.edit { it[Keys.AUTO_ADVANCE] = value }
    }

    open suspend fun updateRestBetweenSets(value: Int) {
        dataStore.edit { it[Keys.REST_BETWEEN_SETS] = value }
    }

    open suspend fun updateRestBetweenExercises(value: Int) {
        dataStore.edit { it[Keys.REST_BETWEEN_EXERCISES] = value }
    }

    open suspend fun updateThemeMode(value: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = value.name }
    }
}
