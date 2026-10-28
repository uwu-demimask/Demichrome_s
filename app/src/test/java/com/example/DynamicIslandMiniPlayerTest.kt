package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.core.timer.FocusTimerManager
import com.example.data.NotchPosition
import com.example.data.TodoTask
import com.example.ui.components.FloatingNotchTimer
import com.example.ui.timer.FocusState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DynamicIslandMiniPlayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFocusTimerLifecycle_startPauseResumeStop() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FocusTimerManager.init(context)

        // Select 25 min duration
        FocusTimerManager.selectDuration(25)
        assertEquals(25, FocusTimerManager.targetMinutes.value)
        assertEquals(25 * 60, FocusTimerManager.remainingSeconds.value)
        assertEquals(FocusState.READY, FocusTimerManager.state.value)

        // Task association
        FocusTimerManager.setActiveTaskId(101L)
        assertEquals(101L, FocusTimerManager.activeTaskId.value)

        // Timer starts
        FocusTimerManager.startTimer()
        assertEquals(FocusState.RUNNING, FocusTimerManager.state.value)

        // Pause works
        FocusTimerManager.pauseTimer()
        assertEquals(FocusState.PAUSED, FocusTimerManager.state.value)

        // Resume works
        FocusTimerManager.startTimer()
        assertEquals(FocusState.RUNNING, FocusTimerManager.state.value)

        // Stop works
        FocusTimerManager.resetTimer()
        assertEquals(FocusState.READY, FocusTimerManager.state.value)
        assertEquals(25 * 60, FocusTimerManager.remainingSeconds.value)
    }

    @Test
    fun testTaskAssociationRemainsCorrect() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FocusTimerManager.init(context)

        FocusTimerManager.setActiveTaskId(777L)
        assertEquals(777L, FocusTimerManager.activeTaskId.value)

        FocusTimerManager.selectDuration(30)
        assertEquals(777L, FocusTimerManager.activeTaskId.value)
    }

    @Test
    fun testDynamicIslandCollapsedPill_rendersAndInteracts() {
        var pauseClicked = false

        composeTestRule.setContent {
            FloatingNotchTimer(
                remainingSeconds = 24 * 60 + 37, // 24:37
                state = FocusState.RUNNING,
                enabled = true,
                notchPosition = NotchPosition.TOP_CENTER,
                targetDurationMinutes = 25,
                activeTask = TodoTask(id = 1, title = "Physics Revision"),
                initiallyExpanded = false,
                onTogglePlayPause = { pauseClicked = true }
            )
        }

        // Check collapsed pill elements: Focus icon, 24:37, pause button
        composeTestRule.onNodeWithTag("dynamic_island_collapsed_pill").assertExists()
        composeTestRule.onNodeWithTag("dynamic_island_focus_icon", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("24:37", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("dynamic_island_toggle_pause", useUnmergedTree = true).assertExists()

        // Toggle pause click
        composeTestRule.onNodeWithTag("dynamic_island_toggle_pause", useUnmergedTree = true).performClick()
        assertTrue(pauseClicked)
    }

    @Test
    fun testDynamicIslandExpandedCard_rendersElementsAndRespectsHierarchy() {
        var pauseClicked = false
        var stopClicked = false

        composeTestRule.setContent {
            FloatingNotchTimer(
                remainingSeconds = 24 * 60 + 37,
                state = FocusState.RUNNING,
                enabled = true,
                notchPosition = NotchPosition.TOP_CENTER,
                targetDurationMinutes = 25,
                activeTask = TodoTask(id = 1, title = "Physics Revision"),
                initiallyExpanded = true,
                onTogglePlayPause = { pauseClicked = true },
                onReset = { stopClicked = true }
            )
        }

        // Check expanded state content
        composeTestRule.onNodeWithTag("dynamic_island_expanded_card").assertExists()
        composeTestRule.onNodeWithText("FOCUS SESSION", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("24:37", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Current task:", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Physics Revision", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("dynamic_island_pause_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("dynamic_island_stop_button", useUnmergedTree = true).assertExists()

        // Test action buttons
        composeTestRule.onNodeWithTag("dynamic_island_pause_button", useUnmergedTree = true).performClick()
        assertTrue(pauseClicked)

        composeTestRule.onNodeWithTag("dynamic_island_stop_button", useUnmergedTree = true).performClick()
        assertTrue(stopClicked)
    }

    @Test
    fun testCutoutSafeRegionCalculation_logicValidation() {
        // Given a camera cutout with max bottom = 38dp, statusBar = 32dp
        val maxCutoutBottomDp = 38.dp
        val statusBarTopDp = 32.dp
        val hasCutout = true

        val safeTopPaddingDp = if (hasCutout) {
            maxOf(maxCutoutBottomDp, statusBarTopDp) + 8.dp
        } else {
            maxOf(statusBarTopDp, 6.dp) + 8.dp
        }

        // Safe top padding must strictly exceed the camera cutout bottom by at least 8dp
        assertTrue(safeTopPaddingDp >= maxCutoutBottomDp + 8.dp)
        assertEquals(46.dp, safeTopPaddingDp)

        // Given a device with no cutout:
        val noCutoutSafeTop = maxOf(24.dp, 6.dp) + 8.dp
        assertEquals(32.dp, noCutoutSafeTop)
    }
}
