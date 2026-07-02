package com.priyanshparekh.repbook.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutScheduleDaoTest {

    private lateinit var db: RepBookDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var scheduleDao: WorkoutScheduleDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepBookDatabase::class.java
        ).allowMainThreadQueries().build()
        workoutDao = db.workoutDao()
        scheduleDao = db.workoutScheduleDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun upsert_insertsSchedule() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = workoutId))
        val result = scheduleDao.getScheduleForDay(1).first()
        assertEquals(workoutId, result?.workoutId)
    }

    @Test
    fun upsert_replacesExistingScheduleForSameDay() = runTest {
        val id1 = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        val id2 = workoutDao.insert(WorkoutEntity(name = "Pull Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id1))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id2))
        val result = scheduleDao.getScheduleForDay(1).first()
        assertEquals(id2, result?.workoutId)
    }

    @Test
    fun delete_removesSchedule() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = workoutId))
        scheduleDao.delete(WorkoutScheduleEntity(day = 1, workoutId = workoutId))
        val result = scheduleDao.getScheduleForDay(1).first()
        assertNull(result)
    }

    @Test
    fun getScheduleForDays_returnsOnlyRequestedDays() = runTest {
        val id1 = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        val id2 = workoutDao.insert(WorkoutEntity(name = "Pull Day"))
        val id3 = workoutDao.insert(WorkoutEntity(name = "Leg Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id1))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 3, workoutId = id2))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 5, workoutId = id3))
        val result = scheduleDao.getScheduleForDays(listOf(1, 3)).first()
        assertEquals(2, result.size)
        assertTrue(result.map { it.day }.containsAll(listOf(1, 3)))
    }

    @Test
    fun cascadeDelete_onWorkoutDelete_removesSchedule() = runTest {
        val workoutId = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = workoutId))
        workoutDao.delete(WorkoutEntity(id = workoutId, name = "Push Day"))
        val result = scheduleDao.getScheduleForDay(1).first()
        assertNull(result)
    }

    @Test
    fun getAllSchedules_returnsAllInserted() = runTest {
        val id1 = workoutDao.insert(WorkoutEntity(name = "Push Day"))
        val id2 = workoutDao.insert(WorkoutEntity(name = "Leg Day"))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id1))
        scheduleDao.upsert(WorkoutScheduleEntity(day = 4, workoutId = id2))
        val result = scheduleDao.getAllSchedules()
        assertEquals(2, result.size)
    }
}
