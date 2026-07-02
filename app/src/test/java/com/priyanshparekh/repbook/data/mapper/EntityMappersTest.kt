package com.priyanshparekh.repbook.data.mapper

import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import com.priyanshparekh.repbook.domain.model.Exercise
import com.priyanshparekh.repbook.domain.model.WorkoutSchedule
import com.priyanshparekh.repbook.domain.model.WorkoutSet
import com.priyanshparekh.repbook.domain.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {

    @Test
    fun workout_roundTrip() {
        val entity = WorkoutEntity(id = 1L, name = "Push Day")
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun workout_toEntity_preservesZeroId() {
        val domain = Workout(id = 0L, name = "New Workout")
        val entity = domain.toEntity()
        assertEquals(0L, entity.id)
        assertEquals("New Workout", entity.name)
    }

    @Test
    fun exercise_roundTrip() {
        val entity = ExerciseEntity(id = 5L, workoutId = 2L, name = "Bench Press")
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.workoutId, domain.workoutId)
        assertEquals(entity.name, domain.name)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun exercise_toEntity_preservesWorkoutId() {
        val domain = Exercise(id = 0L, workoutId = 3L, name = "Squat")
        assertEquals(3L, domain.toEntity().workoutId)
    }

    @Test
    fun workoutSet_roundTrip() {
        val entity = SetEntity(id = 10L, exerciseId = 4L, setNo = 2, weight = 75.5f, reps = 8)
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.exerciseId, domain.exerciseId)
        assertEquals(entity.setNo, domain.setNo)
        assertEquals(entity.weight, domain.weight)
        assertEquals(entity.reps, domain.reps)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun workoutSet_toEntity_preservesAllFields() {
        val domain = WorkoutSet(id = 0L, exerciseId = 7L, setNo = 3, weight = 100f, reps = 5)
        val entity = domain.toEntity()
        assertEquals(7L, entity.exerciseId)
        assertEquals(3, entity.setNo)
        assertEquals(100f, entity.weight)
        assertEquals(5, entity.reps)
    }

    @Test
    fun workoutSchedule_roundTrip() {
        val entity = WorkoutScheduleEntity(day = 2, workoutId = 6L)
        val domain = entity.toDomain()
        assertEquals(entity.day, domain.day)
        assertEquals(entity.workoutId, domain.workoutId)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun workoutSchedule_toEntity_preservesDay() {
        val domain = WorkoutSchedule(day = 5, workoutId = 9L)
        assertEquals(5, domain.toEntity().day)
        assertEquals(9L, domain.toEntity().workoutId)
    }
}
