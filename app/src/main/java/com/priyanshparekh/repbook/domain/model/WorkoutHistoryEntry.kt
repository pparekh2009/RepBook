package com.priyanshparekh.repbook.domain.model

data class WorkoutHistoryEntry(
    val id: Long,
    val workoutId: Long,
    val workoutName: String,
    val completedAt: Long,
    val durationSeconds: Int,
    val totalVolume: Float
)
