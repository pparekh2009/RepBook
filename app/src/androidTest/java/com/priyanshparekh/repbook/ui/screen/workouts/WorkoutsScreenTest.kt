package com.priyanshparekh.repbook.ui.screen.workouts

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.data.db.dao.WorkoutDao
import com.priyanshparekh.repbook.data.db.entity.WorkoutEntity
import com.priyanshparekh.repbook.data.repository.WorkoutRepository
import com.priyanshparekh.repbook.ui.theme.RepBookTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: WorkoutsViewModel

    @Before
    fun setup() {
        viewModel = WorkoutsViewModel(WorkoutRepository(FakeWorkoutDao()))
    }

    @Test
    fun fab_click_opensCreateDialog() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutsScreen(viewModel = viewModel, onNavigateToDetails = {})
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Create workout")
            .performClick()

        composeTestRule
            .onNodeWithText("Create Workout")
            .assertIsDisplayed()
    }

    @Test
    fun createDialog_cancel_dismissesDialog() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutsScreen(viewModel = viewModel, onNavigateToDetails = {})
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Create workout")
            .performClick()

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        composeTestRule
            .onAllNodesWithText("Create Workout")
            .assertCountEquals(0)
    }

    @Test
    fun createDialog_blankInput_createButtonIsDisabled() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutsScreen(viewModel = viewModel, onNavigateToDetails = {})
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Create workout")
            .performClick()

        composeTestRule
            .onNodeWithText("Create")
            .assertIsNotEnabled()
    }

    @Test
    fun emptyState_shownWhenNoWorkouts() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutsScreen(viewModel = viewModel, onNavigateToDetails = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("No workouts yet")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("No workouts yet")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Tap + to create your first workout")
            .assertIsDisplayed()
    }
}

// ── Fake ──────────────────────────────────────────────────────────────────────

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

    override fun getAllWorkouts(): Flow<List<WorkoutEntity>> = _flow

    override suspend fun getById(id: Long): WorkoutEntity? =
        _workouts.firstOrNull { it.id == id }

    override suspend fun deleteAll() {
        _workouts.clear()
        _flow.value = emptyList()
    }
}
