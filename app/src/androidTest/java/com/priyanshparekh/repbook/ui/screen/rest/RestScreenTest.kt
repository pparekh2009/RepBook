package com.priyanshparekh.repbook.ui.screen.rest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
class RestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── TimerDisplay ───────────────────────────────────────────────────────────

    @Test
    fun timerDisplay_showsSecondsOnly_whenUnderOneMinute() {
        composeTestRule.setContent {
            RepBookTheme {
                TimerDisplay(remainingTimeSec = 30, restDurationSec = 30)
            }
        }

        composeTestRule.onNodeWithText("30").assertIsDisplayed()
    }

    @Test
    fun timerDisplay_showsMinutesAndSeconds_whenOverOneMinute() {
        composeTestRule.setContent {
            RepBookTheme {
                TimerDisplay(remainingTimeSec = 90, restDurationSec = 90)
            }
        }

        composeTestRule.onNodeWithText("1:30").assertIsDisplayed()
    }

    @Test
    fun timerDisplay_showsZero_whenTimerExpired() {
        composeTestRule.setContent {
            RepBookTheme {
                TimerDisplay(remainingTimeSec = 0, restDurationSec = 30)
            }
        }

        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    // ── NextExercisePreview ────────────────────────────────────────────────────

    @Test
    fun nextExercisePreview_showsUpNextLabel() {
        composeTestRule.setContent {
            RepBookTheme {
                NextExercisePreview(
                    nextExerciseName = "Shoulder Press",
                    nextSetNo = 1,
                    nextTotalSets = 3
                )
            }
        }

        composeTestRule.onNodeWithText("Up Next").assertIsDisplayed()
    }

    @Test
    fun nextExercisePreview_showsExerciseNameAndSetNumbers() {
        composeTestRule.setContent {
            RepBookTheme {
                NextExercisePreview(
                    nextExerciseName = "Shoulder Press",
                    nextSetNo = 2,
                    nextTotalSets = 3
                )
            }
        }

        composeTestRule.onNodeWithText("Shoulder Press — Set 2 of 3").assertIsDisplayed()
    }

    @Test
    fun nextExercisePreview_showsReadyToFinish_whenNoNextExercise() {
        composeTestRule.setContent {
            RepBookTheme {
                NextExercisePreview(
                    nextExerciseName = "",
                    nextSetNo = 0,
                    nextTotalSets = 0
                )
            }
        }

        composeTestRule.onNodeWithText("Ready to finish!").assertIsDisplayed()
    }

    // ── NextButton ─────────────────────────────────────────────────────────────

    @Test
    fun nextButton_isDisabled_whenEnabledIsFalse() {
        composeTestRule.setContent {
            RepBookTheme {
                NextButton(enabled = false, onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Next").assertIsNotEnabled()
    }

    @Test
    fun nextButton_isEnabled_whenEnabledIsTrue() {
        composeTestRule.setContent {
            RepBookTheme {
                NextButton(enabled = true, onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Next").assertIsEnabled()
    }

    @Test
    fun nextButton_click_callsCallback_whenEnabled() {
        var clicked = false
        composeTestRule.setContent {
            RepBookTheme {
                NextButton(enabled = true, onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()

        assertTrue(clicked)
    }

    // ── SkipRestButton ─────────────────────────────────────────────────────────

    @Test
    fun skipRestButton_isDisplayed() {
        composeTestRule.setContent {
            RepBookTheme {
                SkipRestButton(onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Skip Rest").assertIsDisplayed()
    }

    @Test
    fun skipRestButton_click_callsCallback() {
        var clicked = false
        composeTestRule.setContent {
            RepBookTheme {
                SkipRestButton(onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("Skip Rest").performClick()

        assertTrue(clicked)
    }
}
