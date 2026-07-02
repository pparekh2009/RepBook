package com.priyanshparekh.repbook.ui.screen.finish

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
class FinishScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── WorkoutFinishedText ────────────────────────────────────────────────────

    @Test
    fun workoutFinishedText_isDisplayed() {
        composeTestRule.setContent {
            RepBookTheme {
                WorkoutFinishedText()
            }
        }

        composeTestRule.onNodeWithText("Workout Finished!").assertIsDisplayed()
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
