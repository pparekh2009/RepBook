package com.priyanshparekh.repbook.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutSessionRepository {
    internal val _activeWorkoutId = MutableStateFlow<Long?>(null)
    val activeWorkoutId: StateFlow<Long?> = _activeWorkoutId.asStateFlow()

    private val _completedSets = MutableStateFlow<Set<Pair<Long, Int>>>(emptySet())
    val completedSets: StateFlow<Set<Pair<Long, Int>>> = _completedSets.asStateFlow()

    private var sessionStartTimeMs: Long? = null

    fun startWorkout(workoutId: Long) {
        if (_activeWorkoutId.value != workoutId) {
            _activeWorkoutId.value = workoutId
            _completedSets.value = emptySet()
            sessionStartTimeMs = System.currentTimeMillis()
        }
    }

    fun getSessionDurationSeconds(): Int {
        val start = sessionStartTimeMs ?: return 0
        return ((System.currentTimeMillis() - start) / 1000).toInt()
    }

    fun markSetCompleted(exerciseId: Long, setNo: Int) {
        _completedSets.value = _completedSets.value + (exerciseId to setNo)
    }

    fun resetSession() {
        _activeWorkoutId.value = null
        _completedSets.value = emptySet()
        sessionStartTimeMs = null
    }
}
