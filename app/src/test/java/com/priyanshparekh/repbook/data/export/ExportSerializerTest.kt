package com.priyanshparekh.repbook.data.export

import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutScheduleDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.repository.WorkoutScheduleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportSerializerTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeSetDao: FakeSetDao
    private lateinit var fakeScheduleDao: FakeScheduleDao
    private lateinit var serializer: ExportSerializer

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkoutDao = FakeWorkoutDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeSetDao = FakeSetDao()
        fakeScheduleDao = FakeScheduleDao()
        serializer = ExportSerializer(
            workoutRepository = WorkoutRepository(fakeWorkoutDao, testDispatcher),
            exerciseRepository = ExerciseRepository(fakeExerciseDao, testDispatcher),
            setRepository = SetRepository(fakeSetDao, testDispatcher),
            scheduleRepository = WorkoutScheduleRepository(fakeScheduleDao, testDispatcher)
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun serialize_includesCorrectAppIdAndVersion() = runTest(testScheduler) {
        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertEquals(ExportSerializer.APP_ID, data.appId)
        assertEquals(ExportSerializer.VERSION, data.version)
    }

    @Test
    fun serialize_exportedAtIsNonEmpty() = runTest(testScheduler) {
        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertTrue(data.exportedAt.isNotEmpty())
    }

    @Test
    fun serialize_includesAllWorkoutsWithCorrectFields() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeWorkoutDao.add(WorkoutEntity(id = 2, name = "Pull Day"))

        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertEquals(2, data.workouts.size)
        assertTrue(data.workouts.any { it.id == 1L && it.name == "Push Day" })
        assertTrue(data.workouts.any { it.id == 2L && it.name == "Pull Day" })
    }

    @Test
    fun serialize_includesAllExercisesWithCorrectFields() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 11, workoutId = 1, name = "Shoulder Press"))

        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertEquals(2, data.exercises.size)
        assertTrue(data.exercises.any { it.id == 10L && it.workoutId == 1L && it.name == "Bench Press" })
        assertTrue(data.exercises.any { it.id == 11L && it.workoutId == 1L && it.name == "Shoulder Press" })
    }

    @Test
    fun serialize_includesAllSetsWithCorrectFields() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))
        fakeSetDao.add(SetEntity(id = 100, exerciseId = 10, setNo = 1, weight = 80f, reps = 8))
        fakeSetDao.add(SetEntity(id = 101, exerciseId = 10, setNo = 2, weight = 82.5f, reps = 6))

        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertEquals(2, data.sets.size)
        assertTrue(data.sets.any { it.id == 100L && it.setNo == 1 && it.weight == 80f && it.reps == 8 })
        assertTrue(data.sets.any { it.id == 101L && it.setNo == 2 && it.weight == 82.5f && it.reps == 6 })
    }

    @Test
    fun serialize_includesScheduleWithCorrectFields() = runTest(testScheduler) {
        fakeScheduleDao.add(WorkoutScheduleEntity(day = 1, workoutId = 1))
        fakeScheduleDao.add(WorkoutScheduleEntity(day = 3, workoutId = 2))

        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertEquals(2, data.schedule.size)
        assertTrue(data.schedule.any { it.day == 1 && it.workoutId == 1L })
        assertTrue(data.schedule.any { it.day == 3 && it.workoutId == 2L })
    }

    @Test
    fun serialize_emptyDatabase_producesEmptyArrays() = runTest(testScheduler) {
        val json = serializer.serialize()
        advanceUntilIdle()

        val data = Json.decodeFromString<ExportData>(json)
        assertTrue(data.workouts.isEmpty())
        assertTrue(data.exercises.isEmpty())
        assertTrue(data.sets.isEmpty())
        assertTrue(data.schedule.isEmpty())
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

private class FakeWorkoutDao : WorkoutDao {
    private val _workouts = mutableListOf<WorkoutEntity>()
    private val _flow = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    fun add(entity: WorkoutEntity) {
        _workouts.removeIf { it.id == entity.id }
        _workouts.add(entity)
        _flow.value = _workouts.toList()
    }

    override suspend fun insert(workout: WorkoutEntity): Long {
        add(workout)
        return workout.id
    }

    override suspend fun update(workout: WorkoutEntity) {}
    override suspend fun delete(workout: WorkoutEntity) {}
    override suspend fun deleteAll() { _workouts.clear(); _flow.value = emptyList() }
    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = _flow
    override suspend fun getById(id: Long): WorkoutEntity? = _workouts.firstOrNull { it.id == id }
}

private class FakeExerciseDao : ExerciseDao {
    private val _exercises = mutableListOf<ExerciseEntity>()
    private val _flow = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    fun add(entity: ExerciseEntity) {
        _exercises.removeIf { it.id == entity.id }
        _exercises.add(entity)
        _flow.value = _exercises.toList()
    }

    override suspend fun insert(exercise: ExerciseEntity): Long {
        add(exercise)
        return exercise.id
    }

    override suspend fun update(exercise: ExerciseEntity) {}
    override suspend fun delete(exercise: ExerciseEntity) {}
    override fun getExercisesForWorkout(workoutId: Long): Flow<List<ExerciseEntity>> =
        _flow.map { it.filter { e -> e.workoutId == workoutId } }
    override fun getAllExercises(): Flow<List<ExerciseEntity>> = _flow
    override suspend fun getById(id: Long): ExerciseEntity? = _exercises.firstOrNull { it.id == id }
}

private class FakeSetDao : SetDao {
    private val _sets = mutableListOf<SetEntity>()
    private val _flow = MutableStateFlow<List<SetEntity>>(emptyList())

    fun add(entity: SetEntity) {
        _sets.removeIf { it.id == entity.id }
        _sets.add(entity)
        _flow.value = _sets.toList()
    }

    override suspend fun insert(set: SetEntity): Long { add(set); return set.id }
    override suspend fun insertAll(sets: List<SetEntity>) { sets.forEach { add(it) } }
    override suspend fun update(set: SetEntity) {}
    override suspend fun delete(set: SetEntity) {}
    override fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntity>> =
        _flow.map { it.filter { s -> s.exerciseId == exerciseId }.sortedBy { s -> s.setNo } }
    override suspend fun getSetsForExerciseOnce(exerciseId: Long): List<SetEntity> =
        _sets.filter { it.exerciseId == exerciseId }.sortedBy { it.setNo }
    override suspend fun getAllSets(): List<SetEntity> = _sets.toList()
}

private class FakeScheduleDao : WorkoutScheduleDao {
    private val _schedules = mutableListOf<WorkoutScheduleEntity>()
    private val _flow = MutableStateFlow<List<WorkoutScheduleEntity>>(emptyList())

    fun add(entity: WorkoutScheduleEntity) {
        _schedules.removeIf { it.day == entity.day }
        _schedules.add(entity)
        _flow.value = _schedules.toList()
    }

    override suspend fun upsert(schedule: WorkoutScheduleEntity) = add(schedule)
    override suspend fun delete(schedule: WorkoutScheduleEntity) {
        _schedules.removeIf { it.day == schedule.day }
        _flow.value = _schedules.toList()
    }
    override fun getScheduleForDays(days: List<Int>): Flow<List<WorkoutScheduleEntity>> =
        _flow.map { it.filter { s -> s.day in days } }
    override fun getScheduleForDay(day: Int): Flow<WorkoutScheduleEntity?> =
        _flow.map { it.firstOrNull { s -> s.day == day } }
    override suspend fun getAllSchedules(): List<WorkoutScheduleEntity> = _schedules.toList()
    override suspend fun deleteAll() { _schedules.clear(); _flow.value = emptyList() }
}
