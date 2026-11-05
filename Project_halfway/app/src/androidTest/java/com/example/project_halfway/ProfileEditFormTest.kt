package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Intent tests for updating the profile (US 01.07.03). Runs on an emulator or phone.

@RunWith(AndroidJUnit4::class)
class ProfileEditFormTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun startsFilledIn_andSaveSendsNewPhone() {
        var savedPhone = ""

        composeRule.setContent {
            ProfileEditForm("Parneet Kaur", "parneet@ualberta.ca", "", { _, _, phone ->
                savedPhone = phone
            }, {})
        }

        composeRule.onNodeWithText("Parneet Kaur").assertIsDisplayed()
        composeRule.onNodeWithText("Phone Number").performTextInput("7805551234")
        composeRule.onNodeWithText("Save").performClick()

        assertEquals("7805551234", savedPhone)
    }

    @Test
    fun badEmail_showsError_andDoesNotSave() {
        var saved = false

        composeRule.setContent {
            ProfileEditForm("Parneet Kaur", "parneet@ualberta.ca", "", { _, _, _ ->
                saved = true
            }, {})
        }

        composeRule.onNodeWithText("Email").performTextClearance()
        composeRule.onNodeWithText("Email").performTextInput("not an email")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.onNodeWithText("Please enter a valid email").assertIsDisplayed()
        assertFalse(saved)
    }

    @Test
    fun backButton_callsCancel() {
        var cancelled = false

        composeRule.setContent {
            ProfileEditForm("Parneet Kaur", "", "", { _, _, _ -> }, { cancelled = true })
        }

        composeRule.onNodeWithText("Back").performClick()
        assertTrue(cancelled)
    }
}
