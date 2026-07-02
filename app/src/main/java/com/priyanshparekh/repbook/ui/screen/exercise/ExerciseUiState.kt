package com.priyanshparekh.repbook.ui.screen.exercise

data class ExerciseUiState(
    val workoutName: String = "",
    val exerciseName: String = "",
    val setNo: Int = 1,
    val totalSets: Int = 0,
    val weight: Float = 0f,
    val reps: Int = 0,
    val isTimeBased: Boolean = false,
    val durationSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val timerFinished: Boolean = false,
    val completedExerciseCount: Int = 0,
    val totalExerciseCount: Int = 0,
    val progressPercent: Float = 0f
)

data class RestNavArgs(
    val workoutId: Long,
    val exerciseId: Long,
    val completedSetNo: Int
)
