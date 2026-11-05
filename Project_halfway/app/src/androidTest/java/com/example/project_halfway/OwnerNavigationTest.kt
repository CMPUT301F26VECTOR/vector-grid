package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for getting around the owner's screens (US 02.01.01, US 02.03.01):
 * the owner home's buttons and OwnerScreens opening Register Campsite.
 * Nothing here is saved, so they do not need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class OwnerNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ownerHome_registerButton_callsOnRegisterClick() {
        var pressed = false
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = {}, onRegisterClick = { pressed = true })
        }

        composeRule.onNodeWithText("Register a Campsite").performClick()

        assertTrue(pressed)
    }

    @Test
    fun ownerHome_myCampsitesButton_callsOnMyCampsitesClick() {
        var pressed = false
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = {}, onMyCampsitesClick = { pressed = true })
        }

        composeRule.onNodeWithText("My Campsites").performClick()

        assertTrue(pressed)
    }

    @Test
    fun ownerScreens_registerOpens_andBackArrowReturnsHome() {
        composeRule.setContent {
            OwnerScreens(ownerId = "owner_7", onSignOut = {})
        }

        composeRule.onNodeWithText("Register a Campsite").performClick()
        composeRule.onNodeWithTag("save_button").assertExists()

        composeRule.onNodeWithTag("owner_back_button").performClick()
        composeRule.onNodeWithText("Welcome, Campsite Owner!").assertIsDisplayed()
    }
}
