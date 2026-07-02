package com.priyanshparekh.repbook.ui.screen.home

import com.priyanshparekh.repbook.domain.model.Workout
import com.priyanshparekh.repbook.domain.model.WorkoutWithStatus

sealed class StartWorkoutDialog {
    abstract val workoutId: Long
    abstract val workoutName: String
    abstract val exerciseId: Long

    data class ForToday(
        override val workoutId: Long,
        override val workoutName: String,
        override val exerciseId: Long
    ) : StartWorkoutDialog()

    data class ForFutureDay(
        override val workoutId: Long,
        override val workoutName: String,
        override val exerciseId: Long
    ) : StartWorkoutDialog()
}

data class HomeUiState(
    val workoutsWithStatus: List<WorkoutWithStatus> = emptyList(),
    val allWorkouts: List<Workout> = emptyList(),
    val currentSchedule: Map<Int, Long> = emptyMap(),
    val isScheduleSheetOpen: Boolean = false,
    val pendingSchedule: Map<Int, Long?> = emptyMap(),
    val pickerDay: Int? = null,
    val startWorkoutDialog: StartWorkoutDialog? = null,
    val isLoading: Boolean = true
)
