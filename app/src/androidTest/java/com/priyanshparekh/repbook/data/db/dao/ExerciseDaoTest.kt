package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var db: RepBookDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var exerciseDao: ExerciseDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepBookDatabase::class.java
        ).allowMainThreadQueries().build()
        workoutDao = db.workoutDao()
        exerciseDao = db.exerciseDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_getExercisesForWorkout_returnsInsertedItem() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Bench Press"))
        val result = exerciseDao.getExercisesForWorkout(workoutId).first()
        assertEquals(1, result.size)
        assertEquals("Bench Press", result[0].name)
    }

    @Test
    fun getExercisesForWorkout_onlyReturnsExercisesForThatWorkout() = runTest {
        val id1 = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        val id2 = workoutDao.insert(WorkoutEntity(name = "Pull Day"))
        exerciseDao.insert(ExerciseEntity(workoutId = id1, name = "Bench Press"))
        exerciseDao.insert(ExerciseEntity(workoutId = id2, name = "Pull Up"))
        val result = exerciseDao.getExercisesForWorkout(id1).first()
        assertEquals(1, result.size)
        assertEquals("Bench Press", result[0].name)
    }

    @Test
    fun getAllExercises_returnsAllExercisesOrderedByName() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Tricep Pushdown"))
        exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Bench Press"))
        val result = exerciseDao.getAllExercises().first()
        assertEquals(2, result.size)
        assertEquals("Bench Press", result[0].name)
        assertEquals("Tricep Pushdown", result[1].name)
    }

    @Test
    fun delete_removesExercise() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        val exId = exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Bench Press"))
        exerciseDao.delete(ExerciseEntity(id = exId, workoutId = workoutId, name = "Bench Press"))
        val result = exerciseDao.getExercisesForWorkout(workoutId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun cascadeDelete_onWorkoutDelete_removesExercises() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Bench Press"))
        exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Shoulder Press"))
        workoutDao.delete(WorkoutEntity(id = workoutId, name = "Push Day"))
        val result = exerciseDao.getExercisesForWorkout(workoutId).first()
        assertTrue(result.isEmpty())
    }
}
