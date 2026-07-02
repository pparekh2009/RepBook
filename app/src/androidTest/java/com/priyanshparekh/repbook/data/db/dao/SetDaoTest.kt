package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
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
class SetDaoTest {

    private lateinit var db: RepBookDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var setDao: SetDao

    private var workoutId = 0L
    private var exerciseId = 0L

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepBookDatabase::class.java
        ).allowMainThreadQueries().build()
        workoutDao = db.workoutDao()
        exerciseDao = db.exerciseDao()
        setDao = db.setDao()
        workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        exerciseId = exerciseDao.insert(ExerciseEntity(workoutId = workoutId, name = "Bench Press"))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAll_and_getSetsForExercise_returnsInSetNoOrder() = runTest {
        val sets = listOf(
            SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10),
            SetEntity(exerciseId = exerciseId, setNo = 2, weight = 70f, reps = 8),
            SetEntity(exerciseId = exerciseId, setNo = 3, weight = 80f, reps = 6),
        )
        setDao.insertAll(sets)
        val result = setDao.getSetsForExercise(exerciseId).first()
        assertEquals(3, result.size)
        assertEquals(1, result[0].setNo)
        assertEquals(2, result[1].setNo)
        assertEquals(3, result[2].setNo)
    }

    @Test
    fun update_changesSetValues() = runTest {
        val id = setDao.insert(SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10))
        setDao.update(SetEntity(id = id, exerciseId = exerciseId, setNo = 1, weight = 80f, reps = 8))
        val result = setDao.getSetsForExercise(exerciseId).first()
        assertEquals(80f, result[0].weight)
        assertEquals(8, result[0].reps)
    }

    @Test
    fun delete_removesSet() = runTest {
        val id = setDao.insert(SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10))
        setDao.delete(SetEntity(id = id, exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10))
        val result = setDao.getSetsForExercise(exerciseId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getSetsForExerciseOnce_returnsSameAsFlow() = runTest {
        setDao.insertAll(listOf(
            SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10),
            SetEntity(exerciseId = exerciseId, setNo = 2, weight = 70f, reps = 8),
        ))
        val fromFlow = setDao.getSetsForExercise(exerciseId).first()
        val fromOnce = setDao.getSetsForExerciseOnce(exerciseId)
        assertEquals(fromFlow.map { it.setNo }, fromOnce.map { it.setNo })
    }

    @Test
    fun cascadeDelete_onExerciseDelete_removesSets() = runTest {
        setDao.insertAll(listOf(
            SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10),
            SetEntity(exerciseId = exerciseId, setNo = 2, weight = 70f, reps = 8),
        ))
        exerciseDao.delete(ExerciseEntity(id = exerciseId, workoutId = workoutId, name = "Bench Press"))
        val result = setDao.getSetsForExercise(exerciseId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun cascadeDelete_onWorkoutDelete_removesSets() = runTest {
        setDao.insertAll(listOf(
            SetEntity(exerciseId = exerciseId, setNo = 1, weight = 60f, reps = 10),
        ))
        workoutDao.delete(WorkoutEntity(id = workoutId, name = "Push Day"))
        val result = setDao.getSetsForExercise(exerciseId).first()
        assertTrue(result.isEmpty())
    }
}
