package com.priyanshparekh.repbook.ui.screen.home

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.priyanshparekh.repbook.data.db.dao.ExerciseDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.dao.WorkoutScheduleDao
import com.priyanshparekh.repbook.data.db.entity.ExerciseEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.db.entity.WorkoutScheduleEntity
import com.priyanshparekh.repbook.data.preferences.WorkoutCompletionRepository
import com.priyanshparekh.repbook.data.repository.ExerciseRepository
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.data.repository.WorkoutScheduleRepository
import com.priyanshparekh.repbook.data.session.WorkoutSessionRepository
import com.priyanshparekh.repbook.domain.model.Workout
import com.priyanshparekh.repbook.domain.model.WorkoutStatus
import com.priyanshparekh.repbook.domain.model.WorkoutWithStatus
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
class HomeViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    // Fixed Wednesday June 5 2024 (ISO day-of-week 3).
    // Monday of this week is 2024-06-03.
    private val fixedToday = LocalDate.of(2024, 6, 5)

    private lateinit var fakeScheduleDao: FakeWorkoutScheduleDao
    private lateinit var fakeWorkoutDao: FakeWorkoutDao
    private lateinit var fakeExerciseDao: FakeExerciseDao
    private lateinit var fakeCompletionRepo: FakeCompletionRepository
    private lateinit var sessionRepository: WorkoutSessionRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeScheduleDao = FakeWorkoutScheduleDao()
        fakeWorkoutDao = FakeWorkoutDao()
        fakeExerciseDao = FakeExerciseDao()
        fakeCompletionRepo = FakeCompletionRepository()
        sessionRepository = WorkoutSessionRepository()
        viewModel = HomeViewModel(
            scheduleRepository = WorkoutScheduleRepository(fakeScheduleDao, testDispatcher),
            workoutRepository = WorkoutRepository(fakeWorkoutDao, testDispatcher),
            exerciseRepository = ExerciseRepository(fakeExerciseDao, testDispatcher),
            completionRepository = fakeCompletionRepo,
            sessionRepository = sessionRepository,
            today = fixedToday
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── Status: SCHEDULED ──────────────────────────────────────────────────────

    @Test
    fun status_isScheduled_whenDayIsToday() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Push Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 3, workoutId = id)) // Wednesday = today
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.SCHEDULED), statuses)
    }

    @Test
    fun status_isScheduled_whenDayIsFuture() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Leg Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 5, workoutId = id)) // Friday = future
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.SCHEDULED), statuses)
    }

    // ── Status: INCOMPLETE ─────────────────────────────────────────────────────

    @Test
    fun status_isIncomplete_whenDayIsPastAndNotCompleted() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Pull Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id)) // Monday = past
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.INCOMPLETE), statuses)
    }

    // ── Status: COMPLETED ──────────────────────────────────────────────────────

    @Test
    fun status_isCompleted_whenCompletionKeyPresent() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Pull Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 1, workoutId = id)) // Monday 2024-06-03
        fakeCompletionRepo.setCompletions(setOf("$id:2024-06-03"))
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.COMPLETED), statuses)
    }

    // ── Status: IN_PROGRESS ────────────────────────────────────────────────────

    @Test
    fun status_isInProgress_whenActiveWorkoutMatches() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Push Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 3, workoutId = id))
        sessionRepository._activeWorkoutId.value = id
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.IN_PROGRESS), statuses)
    }

    @Test
    fun status_isInProgress_whenBothActiveAndCompletionKeyPresent() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id = fakeWorkoutDao.insert(WorkoutEntity(name = "Push Day"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 3, workoutId = id))
        fakeCompletionRepo.setCompletions(setOf("$id:2024-06-05"))
        sessionRepository._activeWorkoutId.value = id
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.workoutsWithStatus.map { it.status }
        assertEquals(listOf(WorkoutStatus.IN_PROGRESS), statuses)
    }

    // ── Ordering and filtering ─────────────────────────────────────────────────

    @Test
    fun workoutsWithStatus_areSortedByDay() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        val id1 = fakeWorkoutDao.insert(WorkoutEntity(name = "Workout A"))
        val id2 = fakeWorkoutDao.insert(WorkoutEntity(name = "Workout B"))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 5, workoutId = id1))
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 2, workoutId = id2))
        advanceUntilIdle()

        val days = viewModel.uiState.value.workoutsWithStatus.map { it.day }
        assertEquals(listOf(2, 5), days)
    }

    @Test
    fun workoutsWithStatus_excludesScheduleEntriesWithNoMatchingWorkout() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        fakeScheduleDao.upsert(WorkoutScheduleEntity(day = 3, workoutId = 999))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.workoutsWithStatus.isEmpty())
    }

    @Test
    fun workoutsWithStatus_isEmpty_whenNoScheduleExists() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.workoutsWithStatus.isEmpty())
    }

    // ── onWorkoutCardTapped: dialog type ──────────────────────────────────────

    @Test
    fun onWorkoutCardTapped_opensForTodayDialog_whenScheduledDateIsToday() = runTest(testScheduler) {
        // day=3 is Wednesday, fixedToday is Wednesday
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))

        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Push Day"), day = 3, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.startWorkoutDialog is StartWorkoutDialog.ForToday)
    }

    @Test
    fun onWorkoutCardTapped_opensForTodayDialog_whenScheduledDateIsPast() = runTest(testScheduler) {
        // day=1 is Monday — past relative to Wednesday
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))

        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Push Day"), day = 1, status = WorkoutStatus.INCOMPLETE)
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.startWorkoutDialog is StartWorkoutDialog.ForToday)
    }

    @Test
    fun onWorkoutCardTapped_opensForFutureDayDialog_whenScheduledDateIsFuture() = runTest(testScheduler) {
        // day=5 is Friday — future relative to Wednesday
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))

        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Push Day"), day = 5, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.startWorkoutDialog is StartWorkoutDialog.ForFutureDay)
    }

    // ── onWorkoutCardTapped: dialog content ───────────────────────────────────

    @Test
    fun onWorkoutCardTapped_dialogContainsCorrectWorkoutIdAndFirstExerciseId() = runTest(testScheduler) {
        // Two exercises for workout 42; first by insertion order (ID 10) should be chosen
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 42, name = "Squat"))
        fakeExerciseDao.add(ExerciseEntity(id = 20, workoutId = 42, name = "Deadlift"))

        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 42, name = "Leg Day"), day = 3, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        val dialog = viewModel.uiState.value.startWorkoutDialog as StartWorkoutDialog.ForToday
        assertEquals(42L, dialog.workoutId)
        assertEquals(10L, dialog.exerciseId)
    }

    @Test
    fun onWorkoutCardTapped_dialogContainsWorkoutName() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))

        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Push Day"), day = 3, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        assertEquals("Push Day", viewModel.uiState.value.startWorkoutDialog?.workoutName)
    }

    // ── onWorkoutCardTapped: edge cases ───────────────────────────────────────

    @Test
    fun onWorkoutCardTapped_doesNotSetDialog_whenWorkoutHasNoExercises() = runTest(testScheduler) {
        // No exercises inserted for workout 1
        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Empty Workout"), day = 3, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.startWorkoutDialog)
    }

    // ── dismissStartWorkoutDialog ─────────────────────────────────────────────

    @Test
    fun dismissStartWorkoutDialog_clearsDialog() = runTest(testScheduler) {
        fakeExerciseDao.add(ExerciseEntity(id = 10, workoutId = 1, name = "Bench Press"))
        viewModel.onWorkoutCardTapped(
            WorkoutWithStatus(Workout(id = 1, name = "Push Day"), day = 3, status = WorkoutStatus.SCHEDULED)
        )
        advanceUntilIdle()

        viewModel.dismissStartWorkoutDialog()

        assertNull(viewModel.uiState.value.startWorkoutDialog)
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

private class FakeWorkoutScheduleDao : WorkoutScheduleDao {
    private val _schedules = MutableStateFlow<List<WorkoutScheduleEntity>>(emptyList())

    override suspend fun upsert(schedule: WorkoutScheduleEntity) {
        _schedules.value = _schedules.value.filter { it.day != schedule.day } + schedule
    }

    override suspend fun delete(schedule: WorkoutScheduleEntity) {
        _schedules.value = _schedules.value.filter { it.day != schedule.day }
    }

    override fun getScheduleForDays(days: List<Int>): Flow<List<WorkoutScheduleEntity>> =
        _schedules.map { list -> list.filter { it.day in days } }

    override fun getScheduleForDay(day: Int): Flow<WorkoutScheduleEntity?> =
        _schedules.map { list -> list.firstOrNull { it.day == day } }

    override suspend fun getAllSchedules(): List<WorkoutScheduleEntity> = _schedules.value

    override suspend fun deleteAll() { _schedules.value = emptyList() }
}

private class FakeWorkoutDao : WorkoutDao {
    private var nextId = 1L
    private val _workouts = mutableListOf<WorkoutEntity>()
    private val _flow = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    override suspend fun insert(workout: WorkoutEntity): Long {
        val id = nextId++
        _workouts.add(workout.copy(id = id))
        _flow.value = _workouts.toList()
        return id
    }

    override suspend fun update(workout: WorkoutEntity) {
        val index = _workouts.indexOfFirst { it.id == workout.id }
        if (index != -1) {
            _workouts[index] = workout
            _flow.value = _workouts.toList()
        }
    }

    override suspend fun delete(workout: WorkoutEntity) {
        _workouts.removeIf { it.id == workout.id }
        _flow.value = _workouts.toList()
    }

    override suspend fun deleteAll() {
        _workouts.clear()
        _flow.value = emptyList()
    }

    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = _flow

    override suspend fun getById(id: Long): WorkoutEntity? =
        _workouts.firstOrNull { it.id == id }
}

private class FakeExerciseDao : ExerciseDao {
    private var nextId = 1L
    private val _exercises = mutableListOf<ExerciseEntity>()
    private val _flow = MutableStateFlow<List<ExerciseEntity>>(emptyList())

    // Called from test code (non-suspend context). Updates state synchronously.
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

private object DummyDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = emptyFlow()
    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences
    ): Preferences = emptyPreferences()
}

private class FakeCompletionRepository : WorkoutCompletionRepository(DummyDataStore) {
    private val _completions = MutableStateFlow<Set<String>>(emptySet())

    override fun completionsFlow(): Flow<Set<String>> = _completions

    fun setCompletions(completions: Set<String>) {
        _completions.value = completions
    }
}
