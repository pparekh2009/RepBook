package com.priyanshparekh.repbook.ui.screen.settings

import com.priyanshparekh.repbook.data.preferences.ThemeMode

data class SettingsUiState(
    val autoAdvance: Boolean = false,
    val restBetweenSets: Int = 30,
    val restBetweenExercises: Int = 90,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appVersion: String = ""
)
