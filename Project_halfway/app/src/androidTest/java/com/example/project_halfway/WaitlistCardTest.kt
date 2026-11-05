package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Intent test for leaving a waitlist from My Reservations & Entries (US 01.02.02).
// Runs on an emulator or phone.

@RunWith(AndroidJUnit4::class)
class WaitlistCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsWaitlistInfo_andLeaveButtonWorks() {
        var leaveClicked = false

        composeRule.setContent {
            WaitlistCard(
                campsiteName = "Site 56A",
                daysText = "Lottery closes in 3 days",
                waitingListSize = 12,
                onLeave = { leaveClicked = true }
            )
        }

        composeRule.onNodeWithText("Site 56A").assertIsDisplayed()
        composeRule.onNodeWithText("Lottery closes in 3 days").assertIsDisplayed()
        composeRule.onNodeWithText("People on waiting list: 12").assertIsDisplayed()

        composeRule.onNodeWithText("Leave Waitlist").performClick()
        assertTrue(leaveClicked)
    }
}
