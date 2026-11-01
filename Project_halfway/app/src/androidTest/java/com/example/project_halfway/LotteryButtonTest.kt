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

// Intent tests for joining / leaving a lottery waiting list
// (US 01.01.06, 01.02.01, 01.02.02). Runs on an emulator or phone.

@RunWith(AndroidJUnit4::class)
class LotteryButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notOnList_showsJoinButton_andClickJoins() {
        var joinClicked = false

        composeRule.setContent {
            LotteryButton(
                isOnWaitingList = false,
                waitingListSize = 4,
                daysLeft = 3,
                message = "",
                onJoin = { joinClicked = true },
                onLeave = {}
            )
        }

        composeRule.onNodeWithText("People on waiting list: 4").assertIsDisplayed()
        composeRule.onNodeWithText("Join Waiting List").performClick()
        assertTrue(joinClicked)
    }

    @Test
    fun onList_showsLeaveButton_andClickLeaves() {
        var leaveClicked = false

        composeRule.setContent {
            LotteryButton(
                isOnWaitingList = true,
                waitingListSize = 5,
                daysLeft = 3,
                message = "You joined the waiting list!",
                onJoin = {},
                onLeave = { leaveClicked = true }
            )
        }

        composeRule.onNodeWithText("You joined the waiting list!").assertIsDisplayed()
        composeRule.onNodeWithText("Leave Waiting List").performClick()
        assertTrue(leaveClicked)
    }
}
