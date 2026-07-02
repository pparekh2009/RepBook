package com.priyanshparekh.repbook.ui.screen.history

import com.priyanshparekh.repbook.domain.model.WorkoutHistoryEntry

data class HistoryUiState(
    val entries: List<WorkoutHistoryEntry> = emptyList()
) {
    val totalWorkouts: Int get() = entries.size
    val totalVolume: Float get() = entries.sumOf { it.totalVolume.toDouble() }.toFloat()
    val totalDurationSeconds: Int get() = entries.sumOf { it.durationSeconds }
}
