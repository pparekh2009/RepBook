package com.priyanshparekh.repbook.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.priyanshparekh.repbook.AppContainer
import com.priyanshparekh.repbook.data.preferences.WorkoutCompletionRepository
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.repository.WorkoutScheduleRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import com.priyanshparekh.repbook.domain.model.WorkoutSchedule
import com.priyanshparekh.repbook.domain.model.WorkoutStatus
import com.priyanshparekh.repbook.domain.model.WorkoutWithStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class HomeViewModel(
    private val scheduleRepository: WorkoutScheduleRepository,
    workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    completionRepository: WorkoutCompletionRepository,
    sessionRepository: WorkoutSessionRepository,
    private val today: LocalDate = LocalDate.now()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                scheduleRepository.getScheduleForDays((1..7).toList()),
                workoutRepository.getAllWorkouts(),
                completionRepository.completionsFlow(),
                sessionRepository.activeWorkoutId
            ) { schedules, workouts, completions, activeWorkoutId ->
                val workoutMap = workouts.associateBy { it.id }
                val mondayOfWeek = today.with(DayOfWeek.MONDAY)

                val workoutsWithStatus = schedules.mapNotNull { schedule ->
                    val workout = workoutMap[schedule.workoutId] ?: return@mapNotNull null
                    val scheduledDate = mondayOfWeek.plusDays((schedule.day - 1).toLong())
                    val completionKey = "${workout.id}:$scheduledDate"
                    val status = when {
                        activeWorkoutId == workout.id -> WorkoutStatus.IN_PROGRESS
                        completions.contains(completionKey) -> WorkoutStatus.COMPLETED
                        !scheduledDate.isBefore(today) -> WorkoutStatus.SCHEDULED
                        else -> WorkoutStatus.INCOMPLETE
                    }
                    WorkoutWithStatus(workout = workout, day = schedule.day, status = status)
                }.sortedBy { it.day }

                val currentSchedule = schedules.associate { it.day to it.workoutId }

                Triple(workoutsWithStatus, workouts, currentSchedule)
            }.collect { (workoutsWithStatus, allWorkouts, currentSchedule) ->
                _uiState.update { state ->
                    state.copy(
                        workoutsWithStatus = workoutsWithStatus,
                        allWorkouts = allWorkouts,
                        currentSchedule = currentSchedule,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openScheduleSheet() {
        _uiState.update { state ->
            state.copy(
                isScheduleSheetOpen = true,
                pendingSchedule = state.currentSchedule.entries.associate { it.key to it.value as Long? }
            )
        }
    }

    fun dismissScheduleSheet() {
        _uiState.update { it.copy(isScheduleSheetOpen = false, pendingSchedule = emptyMap(), pickerDay = null) }
    }

    fun onDayTapped(day: Int) {
        _uiState.update { it.copy(pickerDay = day) }
    }

    fun onWorkoutSelectedForDay(day: Int, workoutId: Long?) {
        _uiState.update {
            it.copy(
                pendingSchedule = it.pendingSchedule + (day to workoutId),
                pickerDay = null
            )
        }
    }

    fun dismissPickerDialog() {
        _uiState.update { it.copy(pickerDay = null) }
    }

    fun onWorkoutCardTapped(workoutWithStatus: WorkoutWithStatus) {
        viewModelScope.launch {
            val exercises = exerciseRepository.getExercisesForWorkout(workoutWithStatus.workout.id).first()
            val firstExercise = exercises.firstOrNull() ?: return@launch
            val mondayOfWeek = today.with(DayOfWeek.MONDAY)
            val scheduledDate = mondayOfWeek.plusDays((workoutWithStatus.day - 1).toLong())
            val dialog = if (!scheduledDate.isAfter(today)) {
                StartWorkoutDialog.ForToday(
                    workoutId = workoutWithStatus.workout.id,
                    workoutName = workoutWithStatus.workout.name,
                    exerciseId = firstExercise.id
                )
            } else {
                StartWorkoutDialog.ForFutureDay(
                    workoutId = workoutWithStatus.workout.id,
                    workoutName = workoutWithStatus.workout.name,
                    exerciseId = firstExercise.id
                )
            }
            _uiState.update { it.copy(startWorkoutDialog = dialog) }
        }
    }

    fun dismissStartWorkoutDialog() {
        _uiState.update { it.copy(startWorkoutDialog = null) }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            val current = _uiState.value.currentSchedule
            val pending = _uiState.value.pendingSchedule

            pending.forEach { (day, workoutId) ->
                if (workoutId != null) {
                    scheduleRepository.upsert(WorkoutSchedule(day = day, workoutId = workoutId))
                } else {
                    val existingId = current[day]
                    if (existingId != null) {
                        scheduleRepository.delete(WorkoutSchedule(day = day, workoutId = existingId))
                    }
                }
            }
            dismissScheduleSheet()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(
                        scheduleRepository = container.scheduleRepository,
                        workoutRepository = container.workoutRepository,
                        exerciseRepository = container.exerciseRepository,
                        completionRepository = container.completionRepository,
                        sessionRepository = container.sessionRepository
                    ) as T
            }
    }
}
