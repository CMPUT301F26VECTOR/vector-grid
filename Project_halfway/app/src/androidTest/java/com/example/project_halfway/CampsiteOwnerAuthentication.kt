package com.example.project_halfway


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CampsiteOwnerLogin{
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testingLogin(){

        composeTestRule
            .onNodeWithText("Campsite Owner")
            .performClick()

        composeTestRule
            .onNodeWithText("LOGIN")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Email")
            .performTextInput("parneetkaur@gmail.com")

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("abcdef")

        composeTestRule
            .onNodeWithText("SIGN IN ")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 6000){
            composeTestRule
                .onAllNodesWithText("Welcome, Campsite Owner!")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Welcome, Campsite Owner!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("SIGN OUT")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 6000){
            composeTestRule
                .onAllNodesWithText("Campsite Owner")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Campsite Owner")
            .assertIsDisplayed()


    }

    //PLEASE CHANGE THIS EMAIL TO RUN THE TEST FOR THIS FUCNTION
    @Test
    fun testingCreateAccount(){
        composeTestRule
            .onNodeWithText("Entrant")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Campsite Owner")
            .performClick()

        composeTestRule
            .onNodeWithText("LOGIN")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()

        composeTestRule
            .onNodeWithText("SIGN UP")
            .assertIsDisplayed()


        composeTestRule
            .onNodeWithText("First Name")
            .performTextInput("Guddu")

        composeTestRule
            .onNodeWithText("Last Name")
            .performTextInput("bhaiya")


        //PLEASE CHANGE THIS EMAIL TO RUN THE TEST//
        composeTestRule
            .onNodeWithText("Email")
            .performTextInput("baldev@gmail.com")


        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("abcdef")

        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 6000) {
            composeTestRule
                .onAllNodesWithText("Welcome, Campsite Owner!")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule
            .onNodeWithText("Welcome, Campsite Owner!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("SIGN OUT")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 6000){
            composeTestRule
                .onAllNodesWithText("Campsite Owner")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithText("Campsite Owner")
            .assertIsDisplayed()

    }


}