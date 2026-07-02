package com.priyanshparekh.repbook.ui.screen.exercise

import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeSetDao: FakeSetDao
    private lateinit var sessionRepository: WorkoutSessionRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkoutDao = FakeWorkoutDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeSetDao = FakeSetDao()
        sessionRepository = WorkoutSessionRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(workoutId: Long, exerciseId: Long, setNo: Int): ExerciseViewModel =
        ExerciseViewModel(
            workoutId = workoutId,
            exerciseId = exerciseId,
            setNo = setNo,
            workoutRepository = WorkoutRepository(fakeWorkoutDao, testDispatcher),
            exerciseRepository = ExerciseRepository(fakeExerciseDao, testDispatcher),
            setRepository = SetRepository(fakeSetDao, testDispatcher),
            sessionRepository = sessionRepository
        )

    // ── startWorkout ───────────────────────────────────────────────────────────

    @Test
    fun init_callsStartWorkout_setsActiveWorkoutId() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))

        buildViewModel(workoutId = 1, exerciseId = 1, setNo = 1)

        assertEquals(1L, sessionRepository.activeWorkoutId.value)
    }

    @Test
    fun init_doesNotResetCompletedSets_whenSameWorkoutAlreadyActive() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))

        // Simulate one set already completed from a prior ExerciseScreen visit
        sessionRepository.startWorkout(1)
        sessionRepository.markSetCompleted(exerciseId = 1, setNo = 1)

        // Navigate to set 2 (same workout) — should NOT clear completed sets
        buildViewModel(workoutId = 1, exerciseId = 1, setNo = 2)

        assertTrue(sessionRepository.completedSets.value.contains(1L to 1))
    }

    // ── UI state loading ───────────────────────────────────────────────────────

    @Test
    fun uiState_loadsWorkoutName_exerciseName_andSetData() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1, weight = 80f, reps = 8))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Push Day", state.workoutName)
        assertEquals("Bench Press", state.exerciseName)
        assertEquals(2, state.setNo)
        assertEquals(3, state.totalSets)
        assertEquals(80f, state.weight)
        assertEquals(8, state.reps)
    }

    @Test
    fun uiState_showsTotalExerciseCount() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 3, workoutId = 1, name = "Tricep Dips"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))
        fakeSetDao.insertAll(defaultSets(exerciseId = 2))
        fakeSetDao.insertAll(defaultSets(exerciseId = 3))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 1)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.totalExerciseCount)
    }

    // ── Progress percent ───────────────────────────────────────────────────────

    @Test
    fun progressPercent_isZero_whenNoSetsCompleted() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))
        fakeSetDao.insertAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 1)
        advanceUntilIdle()

        assertEquals(0f, viewModel.uiState.value.progressPercent)
    }

    @Test
    fun progressPercent_isHalf_afterFirstOfTwoExercisesCompleted() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))
        fakeSetDao.insertAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 2, setNo = 1)
        advanceUntilIdle()

        // Complete all 3 sets of exercise 1
        sessionRepository.markSetCompleted(1L, 1)
        sessionRepository.markSetCompleted(1L, 2)
        sessionRepository.markSetCompleted(1L, 3)
        advanceUntilIdle()

        assertEquals(0.5f, viewModel.uiState.value.progressPercent)
        assertEquals(1, viewModel.uiState.value.completedExerciseCount)
    }

    @Test
    fun progressPercent_isOne_afterAllExercisesCompleted() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))
        fakeSetDao.insertAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 2, setNo = 1)
        advanceUntilIdle()

        listOf(1L, 2L).forEach { eId ->
            repeat(3) { i -> sessionRepository.markSetCompleted(eId, i + 1) }
        }
        advanceUntilIdle()

        assertEquals(1f, viewModel.uiState.value.progressPercent)
        assertEquals(2, viewModel.uiState.value.completedExerciseCount)
    }

    @Test
    fun progressPercent_partialSetDone_doesNotCountExerciseAsComplete() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))
        fakeSetDao.insertAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 1)
        advanceUntilIdle()

        // Only 2 of 3 sets done — exercise is NOT complete
        sessionRepository.markSetCompleted(1L, 1)
        sessionRepository.markSetCompleted(1L, 2)
        advanceUntilIdle()

        assertEquals(0f, viewModel.uiState.value.progressPercent)
        assertEquals(0, viewModel.uiState.value.completedExerciseCount)
    }

    // ── onDoneClick ────────────────────────────────────────────────────────────

    @Test
    fun onDoneClick_callsMarkSetCompleted_withCorrectArgs() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 2)
        advanceUntilIdle()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertTrue(sessionRepository.completedSets.value.contains(1L to 2))
    }

    @Test
    fun onDoneClick_emitsNavigationEvent_withCorrectArgs() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 3)
        advanceUntilIdle()

        val events = mutableListOf<RestNavArgs>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertEquals(RestNavArgs(workoutId = 1, exerciseId = 1, completedSetNo = 3), events[0])
    }

    @Test
    fun onDoneClick_marksSetAndEmitsEvent_independently() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.insertAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(workoutId = 1, exerciseId = 1, setNo = 1)
        advanceUntilIdle()

        val events = mutableListOf<RestNavArgs>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.onDoneClick()
        advanceUntilIdle()

        // Both effects must happen on a single click
        assertTrue(sessionRepository.completedSets.value.contains(1L to 1))
        assertEquals(1, events.size)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun defaultSets(exerciseId: Long, weight: Float = 0f, reps: Int = 0): List<SetEntity> =
    (1..3).map { i -> SetEntity(exerciseId = exerciseId, setNo = i, weight = weight, reps = reps) }

// ── Fakes ─────────────────────────────────────────────────────────────────────

private class FakeWorkoutDao : WorkoutDao {
    private var nextId = 1L
    private val _workouts = mutableListOf<WorkoutEntity>()
    private val _flow = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    fun add(entity: WorkoutEntity): Long {
        val id = if (entity.id != 0L) entity.id else nextId++
        _workouts.removeIf { it.id == id }
        _workouts.add(entity.copy(id = id))
        _flow.value = _workouts.toList()
        return id
    }

    override suspend fun insert(workout: WorkoutEntity): Long {
        val id = if (workout.id != 0L) workout.id else nextId++
        _workouts.removeIf { it.id == id }
        _workouts.add(workout.copy(id = id))
        _flow.value = _workouts.toList()
        return id
    }

    override suspend fun update(workout: WorkoutEntity) {}
    override suspend fun delete(workout: WorkoutEntity) {}
    override suspend fun deleteAll() { _workouts.clear(); _flow.value = emptyList() }
    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = _flow
    override suspend fun getById(id: Long): WorkoutEntity? = _workouts.firstOrNull { it.id == id }
}

private class FakeExerciseDao : ExerciseDao {
    private var nextId = 1L
    private val _exercises = mutableListOf<ExerciseEntity>()
    private val _flow = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    fun add(entity: ExerciseEntity): Long {
        val id = if (entity.id != 0L) entity.id else nextId++
        _exercises.removeIf { it.id == id }
        _exercises.add(entity.copy(id = id))
        _flow.value = _exercises.toList()
        return id
    }

    override suspend fun insert(exercise: ExerciseEntity): Long {
        val id = if (exercise.id != 0L) exercise.id else nextId++
        _exercises.removeIf { it.id == id }
        _exercises.add(exercise.copy(id = id))
        _flow.value = _exercises.toList()
        return id
    }

    override suspend fun update(exercise: ExerciseEntity) {}
    override suspend fun delete(exercise: ExerciseEntity) {}
    override fun getExercisesForWorkout(workoutId: Long): Flow<List<ExerciseEntity>> =
        _flow.map { list -> list.filter { it.workoutId == workoutId }.sortedBy { it.id } }
    override fun getAllExercises(): Flow<List<ExerciseEntity>> = _flow
    override suspend fun getById(id: Long): ExerciseEntity? = _exercises.firstOrNull { it.id == id }
}

private class FakeSetDao : SetDao {
    private var nextId = 1L
    private val _sets = mutableListOf<SetEntity>()
    private val _flow = MutableStateFlow<List<SetEntity>>(emptyList())

    override suspend fun insert(set: SetEntity): Long {
        val id = nextId++
        _sets.add(set.copy(id = id))
        _flow.value = _sets.toList()
        return id
    }

    override suspend fun insertAll(sets: List<SetEntity>) {
        sets.forEach { set ->
            val id = nextId++
            _sets.add(set.copy(id = id))
        }
        _flow.value = _sets.toList()
    }

    override suspend fun update(set: SetEntity) {}
    override suspend fun delete(set: SetEntity) {}

    override fun getSetsForExercise(exerciseId: Long): Flow<List<SetEntity>> =
        _flow.map { list -> list.filter { it.exerciseId == exerciseId }.sortedBy { it.setNo } }

    override suspend fun getSetsForExerciseOnce(exerciseId: Long): List<SetEntity> =
        _sets.filter { it.exerciseId == exerciseId }.sortedBy { it.setNo }

    override suspend fun getAllSets(): List<SetEntity> = _sets.toList()
}
