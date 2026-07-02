package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: RepBookDatabase
    private lateinit var dao: WorkoutDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepBookDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.workoutDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_getAllWorkouts_returnsInsertedItem() = runTest {
        dao.insert(WorkoutEntity(name = "Push Day"))
        val result = dao.getAllWorkouts().first()
        assertEquals(1, result.size)
        assertEquals("Push Day", result[0].name)
    }

    @Test
    fun getAllWorkouts_orderedByNameAscending() = runTest {
        dao.insert(WorkoutEntity(name = "Push Day"))
        dao.insert(WorkoutEntity(name = "Leg Day"))
        dao.insert(WorkoutEntity(name = "Back Day"))
        val result = dao.getAllWorkouts().first()
        assertEquals(listOf("Back Day", "Leg Day", "Push Day"), result.map { it.name })
    }

    @Test
    fun update_changesWorkoutName() = runTest {
        val id = dao.insert(WorkoutEntity(name = "Push Day"))
        dao.update(WorkoutEntity(id = id, name = "Pull Day"))
        val result = dao.getAllWorkouts().first()
        assertEquals("Pull Day", result[0].name)
    }

    @Test
    fun delete_removesWorkout() = runTest {
        val id = dao.insert(WorkoutEntity(name = "Push Day"))
        dao.delete(WorkoutEntity(id = id, name = "Push Day"))
        val result = dao.getAllWorkouts().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getById_returnsCorrectWorkout() = runTest {
        val id = dao.insert(WorkoutEntity(name = "Leg Day"))
        val result = dao.getById(id)
        assertNotNull(result)
        assertEquals("Leg Day", result!!.name)
        assertEquals(id, result.id)
    }

    @Test
    fun getById_returnsNull_whenNotFound() = runTest {
        val result = dao.getById(999L)
        assertNull(result)
    }
}
