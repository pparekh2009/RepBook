package com.priyanshparekh.repbook.data.export

import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.repository.WorkoutScheduleRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class ExportSerializer(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setRepository: SetRepository,
    private val scheduleRepository: WorkoutScheduleRepository
) {
    suspend fun serialize(): String {
        val workouts = workoutRepository.getAllWorkouts().first()
        val exercises = exerciseRepository.getAllExercises().first()
        val sets = setRepository.getAllSets()
        val schedule = scheduleRepository.getAllSchedules()

        val exportData = ExportData(
            appId = APP_ID,
            version = VERSION,
            exportedAt = Instant.now().toString(),
            workouts = workouts.map { WorkoutDto(id = it.id, name = it.name) },
            exercises = exercises.map { ExerciseDto(id = it.id, workoutId = it.workoutId, name = it.name) },
            sets = sets.map { SetDto(id = it.id, exerciseId = it.exerciseId, setNo = it.setNo, weight = it.weight, reps = it.reps) },
            schedule = schedule.map { ScheduleDto(day = it.day, workoutId = it.workoutId) }
        )

        return json.encodeToString(exportData)
    }

    companion object {
        const val APP_ID = "com.priyanshparekh.repbook"
        const val VERSION = 1
        private val json = Json { encodeDefaults = true }
    }
}
