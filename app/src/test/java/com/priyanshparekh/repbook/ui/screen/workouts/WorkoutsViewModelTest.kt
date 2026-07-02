package com.priyanshparekh.repbook.ui.screen.workouts

import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.domain.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutsViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeDao: FakeWorkoutDao
    private lateinit var viewModel: WorkoutsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeWorkoutDao()
        viewModel = WorkoutsViewModel(WorkoutRepository(fakeDao, testDispatcher))
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun afterInit_isLoadingFalse_workoutsEmpty() = runTest(testScheduler) {
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.workouts.isEmpty())
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Test
    fun createWorkout_insertsWorkoutInRepository() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        assertEquals(1, fakeDao.workouts.size)
        assertEquals("Push Day", fakeDao.workouts[0].name)
    }

    @Test
    fun createWorkout_trimmedNameIsPersisted() = runTest(testScheduler) {
        viewModel.createWorkout("  Push Day  ")
        advanceUntilIdle()
        assertEquals("Push Day", fakeDao.workouts[0].name)
    }

    @Test
    fun createWorkout_emitsNavigationEventWithNewId() = runTest(testScheduler) {
        val events = mutableListOf<Long>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.createWorkout("Push Day")
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertEquals(fakeDao.workouts[0].id, events[0])
    }

    @Test
    fun createWorkout_dismissesDialog() = runTest(testScheduler) {
        viewModel.showCreateDialog()
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        assertEquals(WorkoutsDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun createWorkout_blankName_doesNotInsert() = runTest(testScheduler) {
        val events = mutableListOf<Long>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.navigationEvent.collect { events.add(it) }
        }

        viewModel.showCreateDialog()
        viewModel.createWorkout("")
        viewModel.createWorkout("   ")
        advanceUntilIdle()

        assertTrue(fakeDao.workouts.isEmpty())
        assertTrue(events.isEmpty())
        assertEquals(WorkoutsDialogState.Create, viewModel.uiState.value.dialogState)
    }

    // ── Rename ─────────────────────────────────────────────────────────────────

    @Test
    fun renameWorkout_updatesName() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()

        viewModel.renameWorkout(workout, "Pull Day")
        advanceUntilIdle()

        assertEquals("Pull Day", viewModel.uiState.value.workouts.first().name)
    }

    @Test
    fun renameWorkout_trimmedNameIsPersisted() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()

        viewModel.renameWorkout(workout, "  Pull Day  ")
        advanceUntilIdle()

        assertEquals("Pull Day", viewModel.uiState.value.workouts.first().name)
    }

    @Test
    fun renameWorkout_dismissesDialog() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()
        viewModel.showRenameDialog(workout)

        viewModel.renameWorkout(workout, "Pull Day")
        advanceUntilIdle()

        assertEquals(WorkoutsDialogState.None, viewModel.uiState.value.dialogState)
    }

    @Test
    fun renameWorkout_blankName_doesNotUpdate() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()
        viewModel.showRenameDialog(workout)

        viewModel.renameWorkout(workout, "")
        viewModel.renameWorkout(workout, "   ")
        advanceUntilIdle()

        assertEquals("Push Day", viewModel.uiState.value.workouts.first().name)
        assertTrue(viewModel.uiState.value.dialogState is WorkoutsDialogState.Rename)
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Test
    fun deleteWorkout_removesWorkout() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()

        viewModel.deleteWorkout(workout)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.workouts.isEmpty())
        assertTrue(fakeDao.workouts.isEmpty())
    }

    @Test
    fun deleteWorkout_dismissesDialog() = runTest(testScheduler) {
        viewModel.createWorkout("Push Day")
        advanceUntilIdle()
        val workout = viewModel.uiState.value.workouts.first()
        viewModel.showDeleteDialog(workout)

        viewModel.deleteWorkout(workout)
        advanceUntilIdle()

        assertEquals(WorkoutsDialogState.None, viewModel.uiState.value.dialogState)
    }

    // ── Dialog state ───────────────────────────────────────────────────────────

    @Test
    fun showCreateDialog_setsDialogState() {
        viewModel.showCreateDialog()
        assertEquals(WorkoutsDialogState.Create, viewModel.uiState.value.dialogState)
    }

    @Test
    fun showRenameDialog_setsDialogState() {
        val workout = Workout(id = 1, name = "Push Day")
        viewModel.showRenameDialog(workout)
        assertEquals(WorkoutsDialogState.Rename(workout), viewModel.uiState.value.dialogState)
    }

    @Test
    fun showDeleteDialog_setsDialogState() {
        val workout = Workout(id = 1, name = "Push Day")
        viewModel.showDeleteDialog(workout)
        assertEquals(WorkoutsDialogState.DeleteConfirm(workout), viewModel.uiState.value.dialogState)
    }

    @Test
    fun dismissDialog_clearsDialogState() {
        viewModel.showCreateDialog()
        viewModel.dismissDialog()
        assertEquals(WorkoutsDialogState.None, viewModel.uiState.value.dialogState)
    }
}

// ── Fake ──────────────────────────────────────────────────────────────────────

private class FakeWorkoutDao : WorkoutDao {
    private var nextId = 1L
    private val _workouts = mutableListOf<WorkoutEntity>()
    private val _flow = MutableStateFlow<List<WorkoutEntity>>(emptyList())

    val workouts: List<WorkoutEntity> get() = _workouts.toList()

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

    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = _flow

    override suspend fun getById(id: Long): WorkoutEntity? =
        _workouts.firstOrNull { it.id == id }

    override suspend fun deleteAll() {
        _workouts.clear()
        _flow.value = emptyList()
    }
}
