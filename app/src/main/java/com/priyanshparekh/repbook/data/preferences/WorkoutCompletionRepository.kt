package com.priyanshparekh.repbook.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

open class WorkoutCompletionRepository(private val dataStore: DataStore<Preferences>) {

    private val completionsKey = stringSetPreferencesKey("workout_completions")

    open fun completionsFlow(): Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[completionsKey] ?: emptySet() }

    open suspend fun markCompleted(workoutId: Long, date: LocalDate) {
        dataStore.edit { prefs ->
            val current = prefs[completionsKey] ?: emptySet()
            prefs[completionsKey] = current + "$workoutId:$date"
        }
    }
}
