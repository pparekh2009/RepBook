package com.priyanshparekh.repbook.ui.screen.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.preferences.WorkoutCompletionRepository
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutHistoryRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import com.priyanshparekh.repbook.domain.model.WorkoutHistoryEntry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class FinishViewModel(
    private val workoutId: Long,
    private val completionRepository: WorkoutCompletionRepository,
    private val sessionRepository: WorkoutSessionRepository,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val historyRepository: WorkoutHistoryRepository,
    private val today: LocalDate = LocalDate.now(),
    private val nowMs: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent: SharedFlow<Unit> = _navigationEvent.asSharedFlow()

    fun onDoneClick() {
        viewModelScope.launch {
            val durationSeconds = sessionRepository.getSessionDurationSeconds()
            val completedAtMs = nowMs()

            val workout = workoutRepository.getById(workoutId)
            val workoutName = workout?.name ?: ""

            val exercises = exerciseRepository.getExercisesForWorkout(workoutId).first()
            var totalVolume = 0f
            for (exercise in exercises) {
                val sets = setRepository.getSetsForExerciseOnce(exercise.id)
                for (set in sets) {
                    totalVolume += if (exercise.isTimeBased) {
                        set.weight * (set.durationSeconds ?: 0)
                    } else {
                        set.weight * set.reps
                    }
                }
            }

            historyRepository.insert(
                WorkoutHistoryEntry(
                    id = 0,
                    workoutId = workoutId,
                    workoutName = workoutName,
                    completedAt = completedAtMs,
                    durationSeconds = durationSeconds,
                    totalVolume = totalVolume
                )
            )

            completionRepository.markCompleted(workoutId, today)
            sessionRepository.resetSession()
            _navigationEvent.emit(Unit)
        }
    }

    companion object {
        fun factory(
            workoutId: Long,
            container: AppContainer
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FinishViewModel(
                    workoutId = workoutId,
                    completionRepository = container.completionRepository,
                    sessionRepository = container.sessionRepository,
                    workoutRepository = container.workoutRepository,
                    exerciseRepository = container.exerciseRepository,
                    setRepository = container.setRepository,
                    historyRepository = container.historyRepository
                ) as T
        }
    }
}
