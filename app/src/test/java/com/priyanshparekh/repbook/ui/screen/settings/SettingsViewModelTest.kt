package com.priyanshparekh.repbook.ui.screen.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.priyanshparekh.repbook.data.preferences.AppPreferences
import com.priyanshparekh.repbook.data.preferences.AppPreferencesDataStore
import com.priyanshparekh.repbook.data.preferences.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class SettingsViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakePrefs: FakeAppPreferencesDataStore
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePrefs = FakeAppPreferencesDataStore()
        viewModel = SettingsViewModel(fakePrefs)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ──────────────────────────────────────────────────────────

    @Test
    fun initialState_reflectsDataStoreDefaults() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.autoAdvance)
        assertEquals(30, state.restBetweenSets)
        assertEquals(90, state.restBetweenExercises)
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
    }

    @Test
    fun initialState_appVersionIsNotEmpty() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.appVersion.isNotEmpty())
    }

    // ── Auto-advance ───────────────────────────────────────────────────────────

    @Test
    fun onAutoAdvanceChanged_writesToDataStore() = runTest(testScheduler) {
        viewModel.onAutoAdvanceChanged(true)
        advanceUntilIdle()

        assertTrue(fakePrefs.lastAutoAdvance)
    }

    @Test
    fun onAutoAdvanceChanged_stateUpdatesReactively() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onAutoAdvanceChanged(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.autoAdvance)
    }

    @Test
    fun onAutoAdvanceChanged_false_stateUpdates() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onAutoAdvanceChanged(true)
        advanceUntilIdle()
        viewModel.onAutoAdvanceChanged(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.autoAdvance)
    }

    // ── Rest between sets ──────────────────────────────────────────────────────

    @Test
    fun onRestBetweenSetsChanged_writesToDataStore() = runTest(testScheduler) {
        viewModel.onRestBetweenSetsChanged(60)
        advanceUntilIdle()

        assertEquals(60, fakePrefs.lastRestBetweenSets)
    }

    @Test
    fun onRestBetweenSetsChanged_stateUpdatesReactively() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onRestBetweenSetsChanged(60)
        advanceUntilIdle()

        assertEquals(60, viewModel.uiState.value.restBetweenSets)
    }

    // ── Rest between exercises ─────────────────────────────────────────────────

    @Test
    fun onRestBetweenExercisesChanged_writesToDataStore() = runTest(testScheduler) {
        viewModel.onRestBetweenExercisesChanged(120)
        advanceUntilIdle()

        assertEquals(120, fakePrefs.lastRestBetweenExercises)
    }

    @Test
    fun onRestBetweenExercisesChanged_stateUpdatesReactively() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onRestBetweenExercisesChanged(120)
        advanceUntilIdle()

        assertEquals(120, viewModel.uiState.value.restBetweenExercises)
    }

    // ── Theme mode ─────────────────────────────────────────────────────────────

    @Test
    fun onThemeModeChanged_writesToDataStore() = runTest(testScheduler) {
        viewModel.onThemeModeChanged(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, fakePrefs.lastThemeMode)
    }

    @Test
    fun onThemeModeChanged_stateUpdatesReactively() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onThemeModeChanged(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }

    @Test
    fun onThemeModeChanged_allModesRoundtrip() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        ThemeMode.entries.forEach { mode ->
            viewModel.onThemeModeChanged(mode)
            advanceUntilIdle()
            assertEquals(mode, viewModel.uiState.value.themeMode)
        }
    }

    // ── Independent updates ────────────────────────────────────────────────────

    @Test
    fun updatingOneField_doesNotAffectOthers() = runTest(testScheduler) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        viewModel.onAutoAdvanceChanged(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.autoAdvance)
        assertEquals(30, state.restBetweenSets)
        assertEquals(90, state.restBetweenExercises)
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
    }
}

// ── Fakes ─────────────────────────────────────────────────────────────────────

private object DummyDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = emptyFlow()
    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences
    ): Preferences = emptyPreferences()
}

private class FakeAppPreferencesDataStore : AppPreferencesDataStore(DummyDataStore) {
    private val _prefs = MutableStateFlow(AppPreferences())

    override val appPreferencesFlow: Flow<AppPreferences> = _prefs

    var lastAutoAdvance: Boolean = AppPreferences().autoAdvance
        private set
    var lastRestBetweenSets: Int = AppPreferences().restBetweenSets
        private set
    var lastRestBetweenExercises: Int = AppPreferences().restBetweenExercises
        private set
    var lastThemeMode: ThemeMode = AppPreferences().themeMode
        private set

    override suspend fun updateAutoAdvance(value: Boolean) {
        lastAutoAdvance = value
        _prefs.value = _prefs.value.copy(autoAdvance = value)
    }

    override suspend fun updateRestBetweenSets(value: Int) {
        lastRestBetweenSets = value
        _prefs.value = _prefs.value.copy(restBetweenSets = value)
    }

    override suspend fun updateRestBetweenExercises(value: Int) {
        lastRestBetweenExercises = value
        _prefs.value = _prefs.value.copy(restBetweenExercises = value)
    }

    override suspend fun updateThemeMode(value: ThemeMode) {
        lastThemeMode = value
        _prefs.value = _prefs.value.copy(themeMode = value)
    }
}
