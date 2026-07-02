package com.priyanshparekh.repbook.domain.model

data class Exercise(
    val id: Long,
    val workoutId: Long,
    val name: String,
    val isTimeBased: Boolean = false
)
