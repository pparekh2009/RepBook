package com.priyanshparekh.repbook.ui.screen.rest

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.SetDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.SetEntity
import com.priyanshparekh.repbook.data.preferences.AppPreferences
import com.priyanshparekh.repbook.data.preferences.AppPreferencesDataStore
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.SetRepository
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeSetDao: FakeSetDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExerciseDao = FakeExerciseDao()
        fakeSetDao = FakeSetDao()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        workoutId: Long = 1L,
        exerciseId: Long = 1L,
        completedSetNo: Int = 1,
        prefs: AppPreferences = AppPreferences()
    ): RestViewModel = RestViewModel(
        workoutId = workoutId,
        exerciseId = exerciseId,
        completedSetNo = completedSetNo,
        exerciseRepository = ExerciseRepository(fakeExerciseDao, testDispatcher),
        setRepository = SetRepository(fakeSetDao, testDispatcher),
        preferencesDataStore = FakeAppPreferencesDataStore(prefs)
    )

    // ── Rest duration selection ────────────────────────────────────────────────

    @Test
    fun init_usesBetweenSetsDuration_whenMoreSetsRemainInExercise() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))  // 3 sets

        val viewModel = buildViewModel(
            exerciseId = 1,
            completedSetNo = 1, // set 1 done, sets 2 & 3 remain
            prefs = AppPreferences(restBetweenSets = 45, restBetweenExercises = 90)
        )
        advanceUntilIdle()

        assertEquals(45, viewModel.uiState.value.restDurationSec)
        assertEquals(45, viewModel.uiState.value.remainingTimeSec)
    }

    @Test
    fun init_usesBetweenExercisesDuration_whenLastSetOfExercise() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))
        fakeSetDao.addAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(
            exerciseId = 1,
            completedSetNo = 3, // last set of exercise 1
            prefs = AppPreferences(restBetweenSets = 45, restBetweenExercises = 90)
        )
        advanceUntilIdle()

        assertEquals(90, viewModel.uiState.value.restDurationSec)
    }

    @Test
    fun init_usesBetweenExercisesDuration_whenLastExerciseLastSet() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            exerciseId = 1,
            completedSetNo = 3, // last set of last exercise
            prefs = AppPreferences(restBetweenSets = 45, restBetweenExercises = 90)
        )
        advanceUntilIdle()

        assertEquals(90, viewModel.uiState.value.restDurationSec)
    }

    // ── Timer behaviour ────────────────────────────────────────────────────────

    @Test
    fun timer_decrementsRemainingTimeSec_byOnePerSecond() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(restBetweenSets = 10)
        )
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.remainingTimeSec)

        advanceTimeBy(1000L)
        assertEquals(9, viewModel.uiState.value.remainingTimeSec)

        advanceTimeBy(1000L)
        assertEquals(8, viewModel.uiState.value.remainingTimeSec)
    }

    @Test
    fun timer_setsIsNextButtonEnabled_whenRemainingTimeReachesZero() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(restBetweenSets = 5)
        )
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isNextButtonEnabled)

        advanceTimeBy(5_000L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNextButtonEnabled)
        assertEquals(0, viewModel.uiState.value.remainingTimeSec)
    }

    @Test
    fun autoAdvance_emitsNavEvent_whenTimerExpires() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(autoAdvance = true, restBetweenSets = 5)
        )
        advanceUntilIdle()

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        advanceTimeBy(5_000L)
        advanceUntilIdle()

        assertEquals(1, events.size)
    }

    @Test
    fun noAutoAdvance_doesNotEmitNavEvent_whenTimerExpires() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(autoAdvance = false, restBetweenSets = 5)
        )
        advanceUntilIdle()

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        advanceTimeBy(5_000L)
        advanceUntilIdle()

        assertEquals(0, events.size)
        assertTrue(viewModel.uiState.value.isNextButtonEnabled)
    }

    // ── Skip and Next ──────────────────────────────────────────────────────────

    @Test
    fun onSkipRestClick_emitsNavEvent_withoutWaitingForTimer() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(restBetweenSets = 30)
        )
        advanceUntilIdle()

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.onSkipRestClick()
        advanceUntilIdle()

        assertEquals(1, events.size)
    }

    @Test
    fun onNextClick_emitsNavEvent() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            completedSetNo = 1,
            prefs = AppPreferences(restBetweenSets = 5)
        )
        advanceUntilIdle()
        advanceTimeBy(5_000L)
        advanceUntilIdle()

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.onNextClick()
        advanceUntilIdle()

        assertEquals(1, events.size)
    }

    // ── Next destination routing ───────────────────────────────────────────────

    @Test
    fun navigation_toSameExerciseNextSet_whenMoreSetsRemain() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1)) // 3 sets

        val viewModel = buildViewModel(
            workoutId = 1,
            exerciseId = 1,
            completedSetNo = 2 // set 2 done, set 3 remains
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Bench Press", state.nextExerciseName)
        assertEquals(3, state.nextSetNo)
        assertEquals(3, state.nextTotalSets)

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }
        viewModel.onSkipRestClick()
        advanceUntilIdle()

        val event = events[0] as RestNavEvent.ToExercise
        assertEquals(1L, event.workoutId)
        assertEquals(1L, event.exerciseId)
        assertEquals(3, event.setNo)
    }

    @Test
    fun navigation_toNextExercise_whenLastSetOfCurrentExercise() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeExerciseDao.add(ExerciseEntity(id = 2, workoutId = 1, name = "Shoulder Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))
        fakeSetDao.addAll(defaultSets(exerciseId = 2))

        val viewModel = buildViewModel(
            workoutId = 1,
            exerciseId = 1,
            completedSetNo = 3 // last set of exercise 1
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Shoulder Press", state.nextExerciseName)
        assertEquals(1, state.nextSetNo)
        assertEquals(3, state.nextTotalSets)

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }
        viewModel.onSkipRestClick()
        advanceUntilIdle()

        val event = events[0] as RestNavEvent.ToExercise
        assertEquals(2L, event.exerciseId)
        assertEquals(1, event.setNo)
    }

    @Test
    fun navigation_toFinish_whenLastExerciseLastSet() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 1, workoutId = 1, name = "Bench Press"))
        fakeSetDao.addAll(defaultSets(exerciseId = 1))

        val viewModel = buildViewModel(
            workoutId = 1,
            exerciseId = 1,
            completedSetNo = 3 // last set of last exercise
        )
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.nextExerciseName)

        val events = mutableListOf<RestNavEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }
        viewModel.onSkipRestClick()
        advanceUntilIdle()

        val event = events[0] as RestNavEvent.ToFinish
        assertEquals(1L, event.workoutId)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun defaultSets(exerciseId: Long): List<SetEntity> =
    (1..3).map { i -> SetEntity(exerciseId = exerciseId, setNo = i, weight = 0f, reps = 0) }

// ── Fakes ─────────────────────────────────────────────────────────────────────

private object DummyDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = emptyFlow()
    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences
    ): Preferences = emptyPreferences()
}

private class FakeAppPreferencesDataStore(
    initialPrefs: AppPreferences = AppPreferences()
) : AppPreferencesDataStore(DummyDataStore) {
    private val _prefs = MutableStateFlow(initialPrefs)
    override val appPreferencesFlow: Flow<AppPreferences> = _prefs
}

private class FakeExerciseDao : ExerciseDao {
    private var nextId = 1L
    private val _exercises = mutableListOf<ExerciseEntity>()
    private val _flow = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    fun add(entity: ExerciseEntity) {
        val id = if (entity.id != 0L) entity.id else nextId++
        _exercises.removeIf { it.id == id }
        _exercises.add(entity.copy(id = id))
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

    override suspend fun getById(id: Long): ExerciseEntity? =
        _exercises.firstOrNull { it.id == id }
}

private class FakeSetDao : SetDao {
    private var nextId = 1L
    private val _sets = mutableListOf<SetEntity>()
    private val _flow = MutableStateFlow<List<SetEntity>>(emptyList())

    fun addAll(sets: List<SetEntity>) {
        sets.forEach { set ->
            val id = nextId++
            _sets.add(set.copy(id = id))
        }
        _flow.value = _sets.toList()
    }

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
