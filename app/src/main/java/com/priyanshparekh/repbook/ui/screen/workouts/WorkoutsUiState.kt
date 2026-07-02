package com.priyanshparekh.repbook.ui.screen.workouts

import com.priyanshparekh.repbook.domain.model.Workout

data class WorkoutsUiState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: WorkoutsDialogState = WorkoutsDialogState.None
)

sealed class WorkoutsDialogState {
    object None : WorkoutsDialogState()
    object Create : WorkoutsDialogState()
    data class Rename(val workout: Workout) : WorkoutsDialogState()
    data class DeleteConfirm(val workout: Workout) : WorkoutsDialogState()
}
