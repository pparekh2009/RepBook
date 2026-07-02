package com.priyanshparekh.repbook.ui.screen.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseViewModel(
    private val workoutId: Long,
    private val exerciseId: Long,
    private val setNo: Int,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val sessionRepository: WorkoutSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RestNavArgs>()
    val navigationEvent: SharedFlow<RestNavArgs> = _navigationEvent.asSharedFlow()

    private var timerJob: Job? = null

    init {
        sessionRepository.startWorkout(workoutId)

        viewModelScope.launch {
            val workout = workoutRepository.getById(workoutId) ?: return@launch
            val exercise = exerciseRepository.getById(exerciseId) ?: return@launch

            // Load set counts for all exercises once — used for progress calculation.
            // Exercises do not change during an active workout session.
            val allExercises = exerciseRepository.getExercisesForWorkout(workoutId).first()
            val setCountMap: Map<Long, Int> = allExercises.associate { ex ->
                ex.id to setRepository.getSetsForExerciseOnce(ex.id).size
            }

            combine(
                setRepository.getSetsForExercise(exerciseId),
                exerciseRepository.getExercisesForWorkout(workoutId),
                sessionRepository.completedSets
            ) { currentSets, exercises, completedSets ->
                val totalSets = currentSets.size
                val currentSet = currentSets.firstOrNull { it.setNo == setNo }

                val completedExerciseCount = exercises.count { ex ->
                    val total = setCountMap[ex.id] ?: 0
                    total > 0 && (1..total).all { n -> completedSets.contains(ex.id to n) }
                }

                val duration = currentSet?.durationSeconds ?: 0

                ExerciseUiState(
                    workoutName = workout.name,
                    exerciseName = exercise.name,
                    setNo = setNo,
                    totalSets = totalSets,
                    weight = currentSet?.weight ?: 0f,
                    reps = currentSet?.reps ?: 0,
                    isTimeBased = exercise.isTimeBased,
                    durationSeconds = duration,
                    remainingSeconds = duration,
                    timerFinished = false,
                    completedExerciseCount = completedExerciseCount,
                    totalExerciseCount = exercises.size,
                    progressPercent = if (exercises.isEmpty()) 0f
                    else completedExerciseCount.toFloat() / exercises.size.toFloat()
                )
            }.collect { newState ->
                val wasTimeBased = _uiState.value.isTimeBased
                val newDuration = newState.durationSeconds
                _uiState.value = newState

                // Start the countdown only on the first emission for a time-based set.
                if (newState.isTimeBased && !wasTimeBased && newDuration > 0) {
                    startTimer(newDuration)
                }
            }
        }
    }

    private fun startTimer(durationSec: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = durationSec
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(remainingSeconds = remaining) }
            }
            _uiState.update { it.copy(timerFinished = true) }
        }
    }

    fun onDoneClick() {
        timerJob?.cancel()
        sessionRepository.markSetCompleted(exerciseId, setNo)
        viewModelScope.launch {
            _navigationEvent.emit(RestNavArgs(workoutId, exerciseId, setNo))
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(
            workoutId: Long,
            exerciseId: Long,
            setNo: Int,
            container: AppContainer
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ExerciseViewModel(
                    workoutId = workoutId,
                    exerciseId = exerciseId,
                    setNo = setNo,
                    workoutRepository = container.workoutRepository,
                    exerciseRepository = container.exerciseRepository,
                    setRepository = container.setRepository,
                    sessionRepository = container.sessionRepository
                ) as T
        }
    }
}
