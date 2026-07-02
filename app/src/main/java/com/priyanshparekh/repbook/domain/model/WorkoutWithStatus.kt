package com.priyanshparekh.repbook.domain.model

data class WorkoutWithStatus(
    val workout: Workout,
    val day: Int,
    val status: WorkoutStatus
)
