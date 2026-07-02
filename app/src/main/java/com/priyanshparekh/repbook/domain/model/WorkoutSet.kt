package com.priyanshparekh.repbook.domain.model

data class WorkoutSet(
    val id: Long,
    val exerciseId: Long,
    val setNo: Int,
    val weight: Float,
    val reps: Int,
    val durationSeconds: Int? = null
)
