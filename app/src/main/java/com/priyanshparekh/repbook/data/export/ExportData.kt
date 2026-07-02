package com.priyanshparekh.repbook.data.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val appId: String,
    val version: Int,
    val exportedAt: String,
    val workouts: List<WorkoutDto>,
    val exercises: List<ExerciseDto>,
    val sets: List<SetDto>,
    val schedule: List<ScheduleDto>
)

@Serializable
data class WorkoutDto(val id: Long, val name: String)

@Serializable
data class ExerciseDto(val id: Long, val workoutId: Long, val name: String)

@Serializable
data class SetDto(val id: Long, val exerciseId: Long, val setNo: Int, val weight: Float, val reps: Int)

@Serializable
data class ScheduleDto(val day: Int, val workoutId: Long)
