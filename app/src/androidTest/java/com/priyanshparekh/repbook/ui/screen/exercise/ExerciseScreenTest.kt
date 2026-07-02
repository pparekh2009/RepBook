package com.priyanshparekh.repbook.ui.screen.exercise

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.ui.theme.RepBookTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExerciseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── WorkoutProgressSection ─────────────────────────────────────────────────

    @Test
    fun workoutProgressSection_showsCorrectExerciseCounter() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutProgressSection(
                    completedExerciseCount = 1,
                    totalExerciseCount = 3,
                    progressPercent = 0.33f
                )
            }
        }

        composeTestRule.onNodeWithText("Exercise 1 of 3").assertIsDisplayed()
    }

    @Test
    fun workoutProgressSection_showsZeroProgress_atStart() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutProgressSection(
                    completedExerciseCount = 0,
                    totalExerciseCount = 4,
                    progressPercent = 0f
                )
            }
        }

        composeTestRule.onNodeWithText("Exercise 0 of 4").assertIsDisplayed()
    }

    // ── ExerciseInfoCard ───────────────────────────────────────────────────────

    @Test
    fun exerciseInfoCard_showsExerciseName() {
        composeTestRule.setContent {
            RepBookTheme {
                ExerciseInfoCard(
                    uiState = ExerciseUiState(exerciseName = "Bench Press")
                )
            }
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
    }

    @Test
    fun exerciseInfoCard_showsSetProgress() {
        composeTestRule.setContent {
            RepBookTheme {
                ExerciseInfoCard(
                    uiState = ExerciseUiState(setNo = 2, totalSets = 4)
                )
            }
        }

        composeTestRule.onNodeWithText("Set 2 of 4").assertIsDisplayed()
    }

    @Test
    fun exerciseInfoCard_showsWeightAndReps_forRepBasedExercise() {
        composeTestRule.setContent {
            RepBookTheme {
                ExerciseInfoCard(
                    uiState = ExerciseUiState(weight = 80f, reps = 8, isTimeBased = false)
                )
            }
        }

        composeTestRule.onNodeWithText("80 kg").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weight").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reps").assertIsDisplayed()
    }

    @Test
    fun exerciseInfoCard_showsDecimalWeight_whenNotWholeNumber() {
        composeTestRule.setContent {
            RepBookTheme {
                ExerciseInfoCard(
                    uiState = ExerciseUiState(weight = 62.5f, reps = 5)
                )
            }
        }

        composeTestRule.onNodeWithText("62.5 kg").assertIsDisplayed()
    }

    // ── DoneButton ─────────────────────────────────────────────────────────────

    @Test
    fun doneButton_isDisplayed() {
        composeTestRule.setContent {
            RepBookTheme {
                DoneButton(onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun doneButton_click_callsCallback() {
        var clicked = false
        composeTestRule.setContent {
            RepBookTheme {
                DoneButton(onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("Done").performClick()

        assertTrue(clicked)
    }
}
