package com.priyanshparekh.repbook.ui.screen.finish

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutHistoryDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import com.priyanshparekh.repbook.data.preferences.WorkoutCompletionRepository
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
import com.priyanshparekh.repbook.data.repository.WorkoutHistoryRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import com.priyanshparekh.repbook.domain.model.WorkoutHistoryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FinishViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeCompletionRepo: FakeWorkoutCompletionRepository
    private lateinit var sessionRepository: WorkoutSessionRepository
    private lateinit var fakeHistoryDao: FakeWorkoutHistoryDao
    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeSetDao: FakeSetDao

    private val fixedToday = LocalDate.of(2024, 6, 5)
    private val fixedNowMs = 1_000_000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCompletionRepo = FakeWorkoutCompletionRepository()
        sessionRepository = WorkoutSessionRepository()
        fakeHistoryDao = FakeWorkoutHistoryDao()
        fakeWorkoutDao = FakeWorkoutDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeSetDao = FakeSetDao()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(workoutId: Long = 1L): FinishViewModel =
        FinishViewModel(
            workoutId = workoutId,
            completionRepository = fakeCompletionRepo,
            sessionRepository = sessionRepository,
            workoutRepository = WorkoutRepository(fakeWorkoutDao, testDispatcher),
            exerciseRepository = ExerciseRepository(fakeExerciseDao, testDispatcher),
            setRepository = SetRepository(fakeSetDao, testDispatcher),
            historyRepository = WorkoutHistoryRepository(fakeHistoryDao, testDispatcher),
            today = fixedToday,
            nowMs = { fixedNowMs }
        )

    @Test
    fun onDoneClick_callsMarkCompleted_withCorrectWorkoutId() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 7L, name = "Push Day"))
        val viewModel = buildViewModel(workoutId = 7L)

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(7L, fakeCompletionRepo.lastMarkedWorkoutId)
    }

    @Test
    fun onDoneClick_callsMarkCompleted_withTodaysDate() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Push Day"))
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(fixedToday, fakeCompletionRepo.lastMarkedDate)
    }

    @Test
    fun onDoneClick_callsResetSession() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Push Day"))
        sessionRepository.startWorkout(1L)
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertNull(sessionRepository.activeWorkoutId.value)
        assertTrue(sessionRepository.completedSets.value.isEmpty())
    }

    @Test
    fun onDoneClick_emitsNavigationEvent() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Push Day"))
        val viewModel = buildViewModel()

        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(1, events.size)
    }

    @Test
    fun onDoneClick_insertsHistoryRecord_withCorrectTimestampAndName() = runTest(testScheduler) {
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Push Day"))
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        val inserted = fakeHistoryDao.inserted.last()
        assertEquals(fixedNowMs, inserted.completedAt)
        assertEquals("Push Day", inserted.workoutName)
        assertEquals(1L, inserted.workoutId)
    }

    @Test
    fun onDoneClick_calculatesVolume_repBasedExercises() = runTest(testScheduler) {
        // exercise A: weight=50, reps=10 → 500
        // exercise B: weight=20, reps=8  → 160
        // expected: 660
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Push Day"))
        fakeExerciseDao.add(ExerciseEntity(id = 10L, workoutId = 1L, name = "Bench Press", isTimeBased = false))
        fakeExerciseDao.add(ExerciseEntity(id = 11L, workoutId = 1L, name = "Fly", isTimeBased = false))
        fakeSetDao.insertAll(listOf(
            SetEntity(exerciseId = 10L, setNo = 1, weight = 50f, reps = 10),
            SetEntity(exerciseId = 11L, setNo = 1, weight = 20f, reps = 8)
        ))
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(660f, fakeHistoryDao.inserted.last().totalVolume)
    }

    @Test
    fun onDoneClick_calculatesVolume_timeBasedExercises() = runTest(testScheduler) {
        // time-based: weight=10, durationSeconds=30 → 300
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Yoga"))
        fakeExerciseDao.add(ExerciseEntity(id = 20L, workoutId = 1L, name = "Plank", isTimeBased = true))
        fakeSetDao.insertAll(listOf(
            SetEntity(exerciseId = 20L, setNo = 1, weight = 10f, reps = 0, durationSeconds = 30)
        ))
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(300f, fakeHistoryDao.inserted.last().totalVolume)
    }

    @Test
    fun onDoneClick_calculatesVolume_mixedExercises() = runTest(testScheduler) {
        // rep-based: weight=100, reps=5 → 500
        // time-based: weight=5, durationSeconds=60 → 300
        // expected: 800
        fakeWorkoutDao.add(WorkoutEntity(id = 1L, name = "Legs"))
        fakeExerciseDao.add(ExerciseEntity(id = 30L, workoutId = 1L, name = "Squat", isTimeBased = false))
        fakeExerciseDao.add(ExerciseEntity(id = 31L, workoutId = 1L, name = "Wall Sit", isTimeBased = true))
        fakeSetDao.insertAll(listOf(
            SetEntity(exerciseId = 30L, setNo = 1, weight = 100f, reps = 5),
            SetEntity(exerciseId = 31L, setNo = 1, weight = 5f, reps = 0, durationSeconds = 60)
        ))
        val viewModel = buildViewModel()

        viewModel.onDoneClick()
        advanceUntilIdle()

        assertEquals(800f, fakeHistoryDao.inserted.last().totalVolume)
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

private object DummyDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = emptyFlow()
    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences
    ): Preferences = emptyPreferences()
}

private class FakeWorkoutCompletionRepository : WorkoutCompletionRepository(DummyDataStore) {
    var lastMarkedWorkoutId: Long? = null
        private set
    var lastMarkedDate: LocalDate? = null
        private set

    override suspend fun markCompleted(workoutId: Long, date: LocalDate) {
        lastMarkedWorkoutId = workoutId
        lastMarkedDate = date
    }
}

private class FakeWorkoutHistoryDao : WorkoutHistoryDao {
    val inserted = mutableListOf<WorkoutHistoryEntity>()

    override fun getAll(): Flow<List<WorkoutHistoryEntity>> = MutableStateFlow(inserted.toList())
    override suspend fun insert(entry: WorkoutHistoryEntity) { inserted.add(entry) }
}

private class FakeWorkoutDao : WorkoutDao {
    private var nextId = 1L
    private val _workouts = mutableListOf<WorkoutEntity>()
    private val _flow = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    fun add(entity: WorkoutEntity) {
        _workouts.removeIf { it.id == entity.id }
        _workouts.add(entity)
        _flow.value = _workouts.toList()
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

    fun add(entity: ExerciseEntity) {
        _exercises.removeIf { it.id == entity.id }
        _exercises.add(entity)
        _flow.value = _exercises.toList()
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
        sets.forEach { _sets.add(it.copy(id = nextId++)) }
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
