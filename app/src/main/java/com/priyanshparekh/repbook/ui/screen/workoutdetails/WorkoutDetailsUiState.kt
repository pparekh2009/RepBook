package com.priyanshparekh.repbook.ui.screen.workoutdetails

import com.priyanshparekh.repbook.domain.model.Exercise
import com.priyanshparekh.repbook.domain.model.Workout
import com.priyanshparekh.repbook.domain.model.WorkoutSet

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
)

data class WorkoutDetailsUiState(
    val workout: Workout? = null,
    val exercises: List<ExerciseWithSets> = emptyList(),
    val isBottomSheetOpen: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val dialogState: WorkoutDetailsDialogState = WorkoutDetailsDialogState.None
)

sealed class WorkoutDetailsDialogState {
    object None : WorkoutDetailsDialogState()
    data class Edit(val exercise: Exercise, val sets: List<WorkoutSet>) : WorkoutDetailsDialogState()
    data class RemoveConfirm(val exercise: Exercise) : WorkoutDetailsDialogState()
}
