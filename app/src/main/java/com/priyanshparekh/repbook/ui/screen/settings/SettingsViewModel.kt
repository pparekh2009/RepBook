package com.priyanshparekh.repbook.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.BuildConfig
import com.priyanshparekh.repbook.data.preferences.AppPreferencesDataStore
import com.priyanshparekh.repbook.data.preferences.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesDataStore: AppPreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesDataStore.appPreferencesFlow
        .map { prefs ->
            SettingsUiState(
                autoAdvance = prefs.autoAdvance,
                restBetweenSets = prefs.restBetweenSets,
                restBetweenExercises = prefs.restBetweenExercises,
                themeMode = prefs.themeMode,
                appVersion = BuildConfig.VERSION_NAME
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(appVersion = BuildConfig.VERSION_NAME)
        )

    fun onAutoAdvanceChanged(value: Boolean) {
        viewModelScope.launch { preferencesDataStore.updateAutoAdvance(value) }
    }

    fun onRestBetweenSetsChanged(value: Int) {
        viewModelScope.launch { preferencesDataStore.updateRestBetweenSets(value) }
    }

    fun onRestBetweenExercisesChanged(value: Int) {
        viewModelScope.launch { preferencesDataStore.updateRestBetweenExercises(value) }
    }

    fun onThemeModeChanged(value: ThemeMode) {
        viewModelScope.launch { preferencesDataStore.updateThemeMode(value) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(container.preferencesDataStore) as T
            }
    }
}
