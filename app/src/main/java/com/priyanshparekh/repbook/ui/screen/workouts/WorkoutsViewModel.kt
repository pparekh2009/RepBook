package com.priyanshparekh.repbook.ui.screen.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.domain.model.Workout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutsViewModel(private val workoutRepository: WorkoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Long>()
    val navigationEvent: SharedFlow<Long> = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            workoutRepository.getAllWorkouts().collect { workouts ->
                _uiState.update { it.copy(workouts = workouts, isLoading = false) }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(dialogState = WorkoutsDialogState.Create) }
    }

    fun showRenameDialog(workout: Workout) {
        _uiState.update { it.copy(dialogState = WorkoutsDialogState.Rename(workout)) }
    }

    fun showDeleteDialog(workout: Workout) {
        _uiState.update { it.copy(dialogState = WorkoutsDialogState.DeleteConfirm(workout)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = WorkoutsDialogState.None) }
    }

    fun createWorkout(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = workoutRepository.insert(Workout(id = 0, name = name.trim()))
            _uiState.update { it.copy(dialogState = WorkoutsDialogState.None) }
            _navigationEvent.emit(id)
        }
    }

    fun renameWorkout(workout: Workout, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            workoutRepository.update(workout.copy(name = newName.trim()))
            _uiState.update { it.copy(dialogState = WorkoutsDialogState.None) }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.delete(workout)
            _uiState.update { it.copy(dialogState = WorkoutsDialogState.None) }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    WorkoutsViewModel(container.workoutRepository) as T
            }
    }
}
