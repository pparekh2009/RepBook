package com.priyanshparekh.repbook.data.preferences

data class AppPreferences(
    val autoAdvance: Boolean = false,
    val restBetweenSets: Int = 30,
    val restBetweenExercises: Int = 90,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
