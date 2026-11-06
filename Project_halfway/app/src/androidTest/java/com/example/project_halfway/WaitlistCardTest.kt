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

/**
 * Intent test for WaitlistCard on the Waitlisted tab of My Reservations & Entries.
 *
 * It runs on an emulator or phone.
 *
 * What it checks: the card shows the campsite name, days left and people on
 * the list, and pressing Leave Waitlist calls onLeave.
 *
 * Used for US 01.02.02.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */

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
