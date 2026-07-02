package com.priyanshparekh.repbook.ui.screen.rest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.preferences.AppPreferencesDataStore
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RestViewModel(
    private val workoutId: Long,
    private val exerciseId: Long,
    private val completedSetNo: Int,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val preferencesDataStore: AppPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestUiState())
    val uiState: StateFlow<RestUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RestNavEvent>()
    val navigationEvent: SharedFlow<RestNavEvent> = _navigationEvent.asSharedFlow()

    private sealed class NextDestination {
        data class Exercise(val exerciseId: Long, val setNo: Int) : NextDestination()
        object Finish : NextDestination()
    }

    private var nextDestination: NextDestination = NextDestination.Finish
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val prefs = preferencesDataStore.appPreferencesFlow.first()
            val exercises = exerciseRepository.getExercisesForWorkout(workoutId).first()
            val currentSets = setRepository.getSetsForExerciseOnce(exerciseId)
            val totalSetsForCurrentExercise = currentSets.size

            val restDuration: Int
            val nextExerciseName: String
            val nextSetNo: Int
            val nextTotalSets: Int

            if (completedSetNo < totalSetsForCurrentExercise) {
                restDuration = prefs.restBetweenSets
                val currentExercise = exercises.firstOrNull { it.id == exerciseId }
                nextExerciseName = currentExercise?.name ?: ""
                nextSetNo = completedSetNo + 1
                nextTotalSets = totalSetsForCurrentExercise
                nextDestination = NextDestination.Exercise(exerciseId, completedSetNo + 1)
            } else {
                restDuration = prefs.restBetweenExercises
                val currentIndex = exercises.indexOfFirst { it.id == exerciseId }
                val nextExercise = exercises.getOrNull(currentIndex + 1)

                if (nextExercise != null) {
                    val nextSets = setRepository.getSetsForExerciseOnce(nextExercise.id)
                    nextExerciseName = nextExercise.name
                    nextSetNo = 1
                    nextTotalSets = nextSets.size
                    nextDestination = NextDestination.Exercise(nextExercise.id, 1)
                } else {
                    nextExerciseName = ""
                    nextSetNo = 0
                    nextTotalSets = 0
                    nextDestination = NextDestination.Finish
                }
            }

            _uiState.value = RestUiState(
                restDurationSec = restDuration,
                remainingTimeSec = restDuration,
                nextExerciseName = nextExerciseName,
                nextSetNo = nextSetNo,
                nextTotalSets = nextTotalSets,
                isNextButtonEnabled = false,
                autoAdvance = prefs.autoAdvance
            )

            startTimer(restDuration, prefs.autoAdvance)
        }
    }

    private fun startTimer(durationSec: Int, autoAdvance: Boolean) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = durationSec
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(remainingTimeSec = remaining) }
            }
            _uiState.update { it.copy(isNextButtonEnabled = true) }
            if (autoAdvance) {
                navigate()
            }
        }
    }

    fun onNextClick() {
        viewModelScope.launch { navigate() }
    }

    fun onSkipRestClick() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch { navigate() }
    }

    private suspend fun navigate() {
        val event = when (val dest = nextDestination) {
            is NextDestination.Exercise -> RestNavEvent.ToExercise(workoutId, dest.exerciseId, dest.setNo)
            is NextDestination.Finish -> RestNavEvent.ToFinish(workoutId)
        }
        _navigationEvent.emit(event)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(
            workoutId: Long,
            exerciseId: Long,
            completedSetNo: Int,
            container: AppContainer
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RestViewModel(
                    workoutId = workoutId,
                    exerciseId = exerciseId,
                    completedSetNo = completedSetNo,
                    exerciseRepository = container.exerciseRepository,
                    setRepository = container.setRepository,
                    preferencesDataStore = container.preferencesDataStore
                ) as T
        }
    }
}
