package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EntrantAuthenticationProfileTest{
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun buttons_on_Welcome_screen(){
        composeTestRule
            .onNodeWithText("Entrant")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Campsite Owner")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Entrant")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 6000){
            composeTestRule
                .onAllNodesWithText("Welcome!")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Welcome!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("App Policy and Guidelines")
            .performClick()

        composeTestRule
            .onNodeWithText("App Policy and Guidelines")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Go back")
            .performClick()

        composeTestRule
            .onNodeWithText("Welcome!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Signout")
            .performClick()

        composeTestRule
            .onNodeWithText("Entrant")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Campsite Owner")
            .assertIsDisplayed()

    }
}