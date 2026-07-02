package com.priyanshparekh.repbook.ui.screen.rest

data class RestUiState(
    val restDurationSec: Int = 30,
    val remainingTimeSec: Int = 30,
    val nextExerciseName: String = "",
    val nextSetNo: Int = 1,
    val nextTotalSets: Int = 0,
    val isNextButtonEnabled: Boolean = false,
    val autoAdvance: Boolean = false
)

sealed class RestNavEvent {
    data class ToExercise(val workoutId: Long, val exerciseId: Long, val setNo: Int) : RestNavEvent()
    data class ToFinish(val workoutId: Long) : RestNavEvent()
}
