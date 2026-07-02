package com.priyanshparekh.repbook.data.export

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.RepBookDatabase
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportServiceTest {

    private lateinit var db: RepBookDatabase
    private lateinit var service: ImportService

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RepBookDatabase::class.java
        ).allowMainThreadQueries().build()
        service = ImportService(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun sampleExportData() = ExportData(
        appId = ExportSerializer.APP_ID,
        version = ExportSerializer.VERSION,
        exportedAt = "2024-01-01T00:00:00Z",
        workouts = listOf(
            WorkoutDto(id = 1, name = "Push Day"),
            WorkoutDto(id = 2, name = "Pull Day")
        ),
        exercises = listOf(
            ExerciseDto(id = 10, workoutId = 1, name = "Bench Press"),
            ExerciseDto(id = 11, workoutId = 2, name = "Pull-up")
        ),
        sets = listOf(
            SetDto(id = 100, exerciseId = 10, setNo = 1, weight = 80f, reps = 8),
            SetDto(id = 101, exerciseId = 10, setNo = 2, weight = 82.5f, reps = 6),
            SetDto(id = 102, exerciseId = 11, setNo = 1, weight = 0f, reps = 10)
        ),
        schedule = listOf(
            ScheduleDto(day = 1, workoutId = 1),
            ScheduleDto(day = 3, workoutId = 2)
        )
    )

    @Test
    fun import_insertsAllWorkoutsWithOriginalIds() = runTest {
        service.import(sampleExportData())

        val workouts = db.workoutDao().getAllWorkouts().first()
        assertEquals(2, workouts.size)
        assertTrue(workouts.any { it.id == 1L && it.name == "Push Day" })
        assertTrue(workouts.any { it.id == 2L && it.name == "Pull Day" })
    }

    @Test
    fun import_insertsAllExercisesWithOriginalIds() = runTest {
        service.import(sampleExportData())

        val exercises = db.exerciseDao().getAllExercises().first()
        assertEquals(2, exercises.size)
        assertTrue(exercises.any { it.id == 10L && it.workoutId == 1L && it.name == "Bench Press" })
        assertTrue(exercises.any { it.id == 11L && it.workoutId == 2L && it.name == "Pull-up" })
    }

    @Test
    fun import_insertsAllSetsWithCorrectFields() = runTest {
        service.import(sampleExportData())

        val sets = db.setDao().getAllSets()
        assertEquals(3, sets.size)
        assertTrue(sets.any { it.id == 100L && it.exerciseId == 10L && it.setNo == 1 && it.weight == 80f && it.reps == 8 })
        assertTrue(sets.any { it.id == 101L && it.weight == 82.5f && it.reps == 6 })
        assertTrue(sets.any { it.id == 102L && it.exerciseId == 11L && it.reps == 10 })
    }

    @Test
    fun import_insertsScheduleEntries() = runTest {
        service.import(sampleExportData())

        val schedule = db.workoutScheduleDao().getAllSchedules()
        assertEquals(2, schedule.size)
        assertTrue(schedule.any { it.day == 1 && it.workoutId == 1L })
        assertTrue(schedule.any { it.day == 3 && it.workoutId == 2L })
    }

    @Test
    fun import_replacesAllExistingData() = runTest {
        // Pre-load different data
        db.workoutDao().insert(WorkoutEntity(id = 99, name = "Old Workout"))
        db.exerciseDao().insert(ExerciseEntity(id = 990, workoutId = 99, name = "Old Exercise"))
        db.workoutScheduleDao().upsert(WorkoutScheduleEntity(day = 7, workoutId = 99))

        service.import(sampleExportData())

        val workouts = db.workoutDao().getAllWorkouts().first()
        assertEquals(2, workouts.size)
        assertTrue(workouts.none { it.id == 99L })

        val schedule = db.workoutScheduleDao().getAllSchedules()
        assertTrue(schedule.none { it.day == 7 })
    }

    @Test
    fun import_secondImport_replacesFirstImportData() = runTest {
        service.import(sampleExportData())

        val replacement = ExportData(
            appId = ExportSerializer.APP_ID,
            version = ExportSerializer.VERSION,
            exportedAt = "2024-06-01T00:00:00Z",
            workouts = listOf(WorkoutDto(id = 50, name = "Leg Day")),
            exercises = listOf(ExerciseDto(id = 500, workoutId = 50, name = "Squat")),
            sets = listOf(SetDto(id = 5000, exerciseId = 500, setNo = 1, weight = 100f, reps = 5)),
            schedule = listOf(ScheduleDto(day = 5, workoutId = 50))
        )
        service.import(replacement)

        val workouts = db.workoutDao().getAllWorkouts().first()
        assertEquals(1, workouts.size)
        assertEquals("Leg Day", workouts[0].name)

        val sets = db.setDao().getAllSets()
        assertEquals(1, sets.size)
        assertEquals(100f, sets[0].weight)
    }
}
