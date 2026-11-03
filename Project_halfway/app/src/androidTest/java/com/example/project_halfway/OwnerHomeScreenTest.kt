package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the owner home screen.
 * They check that the buttons are shown and that My Campsites and Register
 * Campsite open the right screens, and that Back returns to the home screen.
 * The list test asks Firestore for campsites but only checks the title.
 */
@RunWith(AndroidJUnit4::class)
class OwnerHomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeScreen_showsTheButtons() {
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = {})
        }

        composeRule.onNodeWithTag("my_campsites_button").assertIsDisplayed()
        composeRule.onNodeWithTag("register_campsite_button").assertIsDisplayed()
        composeRule.onNodeWithText("SIGN OUT").assertIsDisplayed()
    }

    @Test
    fun registerButton_opensTheForm_andBackReturnsHome() {
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = {})
        }

        composeRule.onNodeWithTag("register_campsite_button").performClick()
        composeRule.onNodeWithTag("name_field").assertIsDisplayed()

        composeRule.onNodeWithTag("back_button").performClick()
        composeRule.onNodeWithTag("register_campsite_button").assertIsDisplayed()
    }

    @Test
    fun myCampsitesButton_opensTheList() {
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = {})
        }

        composeRule.onNodeWithTag("my_campsites_button").performClick()
        composeRule.onNodeWithText("Registered Campsites").assertIsDisplayed()
    }

    @Test
    fun signOutButton_callsOnSignOut() {
        var signedOut = false
        composeRule.setContent {
            OwnerHomeScreenUI(onSignOut = { signedOut = true })
        }

        composeRule.onNodeWithText("SIGN OUT").performClick()

        assert(signedOut)
    }
}