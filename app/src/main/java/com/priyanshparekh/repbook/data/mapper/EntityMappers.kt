package com.priyanshparekh.repbook.data.mapper

import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import com.priyanshparekh.repbook.domain.model.Exercise
import com.priyanshparekh.repbook.domain.model.WorkoutHistoryEntry
import com.priyanshparekh.repbook.domain.model.WorkoutSchedule
import com.priyanshparekh.repbook.domain.model.WorkoutSet
import com.priyanshparekh.repbook.domain.model.Workout

fun WorkoutEntity.toDomain() = Workout(id = id, name = name)
fun Workout.toEntity() = WorkoutEntity(id = id, name = name)

fun ExerciseEntity.toDomain() = Exercise(id = id, workoutId = workoutId, name = name, isTimeBased = isTimeBased)
fun Exercise.toEntity() = ExerciseEntity(id = id, workoutId = workoutId, name = name, isTimeBased = isTimeBased)

fun SetEntity.toDomain() = WorkoutSet(id = id, exerciseId = exerciseId, setNo = setNo, weight = weight, reps = reps, durationSeconds = durationSeconds)
fun WorkoutSet.toEntity() = SetEntity(id = id, exerciseId = exerciseId, setNo = setNo, weight = weight, reps = reps, durationSeconds = durationSeconds)

fun WorkoutScheduleEntity.toDomain() = WorkoutSchedule(day = day, workoutId = workoutId)
fun WorkoutSchedule.toEntity() = WorkoutScheduleEntity(day = day, workoutId = workoutId)

fun WorkoutHistoryEntity.toDomain() = WorkoutHistoryEntry(
    id = id,
    workoutId = workoutId,
    workoutName = workoutName,
    completedAt = completedAt,
    durationSeconds = durationSeconds,
    totalVolume = totalVolume
)
