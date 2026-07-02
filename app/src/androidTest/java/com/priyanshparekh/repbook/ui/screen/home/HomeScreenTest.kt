package com.priyanshparekh.repbook.ui.screen.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.priyanshparekh.repbook.ui.theme.RepBookTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── WeeklyCalendar ─────────────────────────────────────────────────────────

    @Test
    fun weeklyCalendar_todayCellHasDistinctHighlight() {
        composeTestRule.setContent {
            RepBookTheme {
                WeeklyCalendar()
            }
        }

        composeTestRule
            .onNodeWithTag("today_highlight")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun weeklyCalendar_exactlyOneCellIsHighlighted() {
        composeTestRule.setContent {
            RepBookTheme {
                WeeklyCalendar()
            }
        }

        val highlightedNodes = composeTestRule
            .onAllNodes(hasTestTag("today_highlight"))
            .fetchSemanticsNodes()
        assert(highlightedNodes.size == 1) {
            "Expected exactly 1 highlighted cell but found ${highlightedNodes.size}"
        }
    }

    @Test
    fun weeklyCalendar_todayCellShowsCorrectDayNumber() {
        composeTestRule.setContent {
            RepBookTheme {
                WeeklyCalendar()
            }
        }

        val todayDayOfMonth = LocalDate.now().dayOfMonth.toString()

        composeTestRule
            .onNodeWithTag("today_highlight")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(todayDayOfMonth)
            .assertIsDisplayed()
    }

    // ── StartTodayWorkoutDialog ────────────────────────────────────────────────

    @Test
    fun startTodayWorkoutDialog_showsWorkoutNameStartAndCancelButtons() {
        composeTestRule.setContent {
            RepBookTheme {
                StartTodayWorkoutDialog(
                    workoutName = "Push Day",
                    onStart = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ready to start Push Day?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun startTodayWorkoutDialog_cancelButton_callsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            RepBookTheme {
                StartTodayWorkoutDialog(
                    workoutName = "Push Day",
                    onStart = {},
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun startTodayWorkoutDialog_startButton_callsOnStart() {
        var started = false
        composeTestRule.setContent {
            RepBookTheme {
                StartTodayWorkoutDialog(
                    workoutName = "Push Day",
                    onStart = { started = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start").performClick()

        assertTrue(started)
    }

    // ── StartFutureDayWorkoutDialog ────────────────────────────────────────────

    @Test
    fun startFutureDayWorkoutDialog_showsWorkoutNameStartAnywayAndCancelButtons() {
        composeTestRule.setContent {
            RepBookTheme {
                StartFutureDayWorkoutDialog(
                    workoutName = "Leg Day",
                    onStartAnyway = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Not Scheduled for Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leg Day is scheduled for a future day. Start it anyway?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Anyway").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun startFutureDayWorkoutDialog_cancelButton_callsOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            RepBookTheme {
                StartFutureDayWorkoutDialog(
                    workoutName = "Leg Day",
                    onStartAnyway = {},
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun startFutureDayWorkoutDialog_startAnywayButton_callsOnStartAnyway() {
        var started = false
        composeTestRule.setContent {
            RepBookTheme {
                StartFutureDayWorkoutDialog(
                    workoutName = "Leg Day",
                    onStartAnyway = { started = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start Anyway").performClick()

        assertTrue(started)
    }
}
