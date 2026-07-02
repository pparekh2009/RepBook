package com.priyanshparekh.repbook.ui.screen.history

import com.priyanshparekh.repbook.data.db.dao.WorkoutHistoryDao
import com.priyanshparekh.repbook.data.db.entity.WorkoutHistoryEntity
import com.priyanshparekh.repbook.data.repository.WorkoutHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeDao: FakeWorkoutHistoryDao

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeWorkoutHistoryDao()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() =
        HistoryViewModel(WorkoutHistoryRepository(fakeDao, testDispatcher))

    @Test
    fun uiState_isEmpty_whenNoHistory() = runTest(testScheduler) {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.entries.size)
    }

    @Test
    fun uiState_reflectsInsertedEntries() = runTest(testScheduler) {
        fakeDao.emit(listOf(
            historyEntity(id = 1L, workoutName = "Push Day", completedAt = 2000L),
            historyEntity(id = 2L, workoutName = "Pull Day", completedAt = 1000L)
        ))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val entries = viewModel.uiState.value.entries
        assertEquals(2, entries.size)
        assertEquals("Push Day", entries[0].workoutName)
        assertEquals("Pull Day", entries[1].workoutName)
    }

    @Test
    fun summaryStats_totalWorkouts() = runTest(testScheduler) {
        fakeDao.emit(listOf(
            historyEntity(id = 1L),
            historyEntity(id = 2L),
            historyEntity(id = 3L)
        ))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.totalWorkouts)
    }

    @Test
    fun summaryStats_totalVolume() = runTest(testScheduler) {
        fakeDao.emit(listOf(
            historyEntity(id = 1L, totalVolume = 500f),
            historyEntity(id = 2L, totalVolume = 300f)
        ))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(800f, viewModel.uiState.value.totalVolume)
    }

    @Test
    fun summaryStats_totalDurationSeconds() = runTest(testScheduler) {
        fakeDao.emit(listOf(
            historyEntity(id = 1L, durationSeconds = 1800),
            historyEntity(id = 2L, durationSeconds = 2700)
        ))
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(4500, viewModel.uiState.value.totalDurationSeconds)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun historyEntity(
    id: Long = 0L,
    workoutName: String = "Test",
    completedAt: Long = 0L,
    durationSeconds: Int = 0,
    totalVolume: Float = 0f
) = WorkoutHistoryEntity(
    id = id,
    workoutId = 1L,
    workoutName = workoutName,
    completedAt = completedAt,
    durationSeconds = durationSeconds,
    totalVolume = totalVolume
)

// ── Fakes ─────────────────────────────────────────────────────────────────────

private class FakeWorkoutHistoryDao : WorkoutHistoryDao {
    private val _flow = MutableStateFlow<List<WorkoutHistoryEntity>>(emptyList())

    fun emit(entities: List<WorkoutHistoryEntity>) { _flow.value = entities }

    override fun getAll(): Flow<List<WorkoutHistoryEntity>> = _flow
    override suspend fun insert(entry: WorkoutHistoryEntity) { _flow.value = _flow.value + entry }
}
