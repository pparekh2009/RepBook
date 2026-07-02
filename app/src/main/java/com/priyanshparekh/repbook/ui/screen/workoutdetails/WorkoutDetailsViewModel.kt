package com.priyanshparekh.repbook.ui.screen.workoutdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.domain.model.Exercise
import com.priyanshparekh.repbook.domain.model.WorkoutSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val workoutId: Long,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutDetailsUiState())
    val uiState: StateFlow<WorkoutDetailsUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercisesWithSetsFlow: Flow<List<ExerciseWithSets>> =
        exerciseRepository.getExercisesForWorkout(workoutId)
            .flatMapLatest { exercises ->
                if (exercises.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        exercises.map { exercise ->
                            setRepository.getSetsForExercise(exercise.id)
                                .map { sets -> ExerciseWithSets(exercise, sets) }
                        }
                    ) { it.toList() }
                }
            }

    init {
        viewModelScope.launch {
            val workout = workoutRepository.getById(workoutId)
            _uiState.update { it.copy(workout = workout) }
        }
        viewModelScope.launch {
            exercisesWithSetsFlow.collect { exercisesWithSets ->
                _uiState.update { it.copy(exercises = exercisesWithSets) }
            }
        }
    }

    fun openBottomSheet() {
        _uiState.update { it.copy(isBottomSheetOpen = true, selectedIds = emptySet()) }
    }

    fun closeBottomSheet() {
        _uiState.update { it.copy(isBottomSheetOpen = false, selectedIds = emptySet()) }
    }

    fun toggleSelection(exerciseId: Long) {
        _uiState.update { state ->
            val updated = if (exerciseId in state.selectedIds) {
                state.selectedIds - exerciseId
            } else {
                state.selectedIds + exerciseId
            }
            state.copy(selectedIds = updated)
        }
    }

    fun saveSelectedExercises() {
        val toAdd = ExerciseData.exercises
            .filter { it.id in _uiState.value.selectedIds }
        viewModelScope.launch {
            toAdd.forEach { exercise ->
                val newId = exerciseRepository.insert(
                    Exercise(id = 0, workoutId = workoutId, name = exercise.name, isTimeBased = exercise.isTimeBased)
                )
                setRepository.insertDefaultSets(
                    newId,
                    durationSeconds = if (exercise.isTimeBased) 0 else null
                )
            }
            _uiState.update { it.copy(isBottomSheetOpen = false, selectedIds = emptySet()) }
        }
    }

    fun showEditDialog(exercise: Exercise, sets: List<WorkoutSet>) {
        _uiState.update { it.copy(dialogState = WorkoutDetailsDialogState.Edit(exercise, sets)) }
    }

    fun showRemoveDialog(exercise: Exercise) {
        _uiState.update { it.copy(dialogState = WorkoutDetailsDialogState.RemoveConfirm(exercise)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = WorkoutDetailsDialogState.None) }
    }

    fun updateSets(updatedSets: List<WorkoutSet>) {
        viewModelScope.launch {
            updatedSets.forEach { set -> setRepository.update(set) }
            _uiState.update { it.copy(dialogState = WorkoutDetailsDialogState.None) }
        }
    }

    fun removeExercise(exercise: Exercise) {
        viewModelScope.launch {
            exerciseRepository.delete(exercise)
            _uiState.update { it.copy(dialogState = WorkoutDetailsDialogState.None) }
        }
    }

    companion object {
        fun factory(workoutId: Long, container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    WorkoutDetailsViewModel(
                        workoutId = workoutId,
                        workoutRepository = container.workoutRepository,
                        exerciseRepository = container.exerciseRepository,
                        setRepository = container.setRepository
                    ) as T
            }
    }
}
