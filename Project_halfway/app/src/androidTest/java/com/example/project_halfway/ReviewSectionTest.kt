package com.example.project_halfway

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for ReviewSection (US 01.13.02). Saving is replaced with a fake,
 * so no Firestore is needed.
 */
@RunWith(AndroidJUnit4::class)
class ReviewSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyText_showsErrorAndDoesNotSave() {
        var saved: Review? = null
        composeRule.setContent {
            ReviewSection(campsiteId = "camp1", entrantId = "e1", save = { review, _ -> saved = review })
        }

        composeRule.onNodeWithTag("review_post").performClick()

        composeRule.onNodeWithTag("review_message").assertTextEquals("Please write something before posting.")
        assertNull(saved)
    }

    @Test
    fun goodReview_isSavedForTheCampsiteAndEntrant() {
        var saved: Review? = null
        var finish: ((Boolean) -> Unit)? = null
        composeRule.setContent {
            ReviewSection(campsiteId = "camp1", entrantId = "e1", save = { review, onDone ->
                saved = review
                finish = onDone
            })
        }

        composeRule.onNodeWithTag("review_text").performTextInput("Lovely and quiet")
        composeRule.onNodeWithTag("review_post").performClick()

        assertEquals("camp1", saved?.campsiteId)
        assertEquals("e1", saved?.entrantId)
        assertEquals("Lovely and quiet", saved?.text)

        composeRule.runOnIdle { finish?.invoke(true) }
        composeRule.onNodeWithTag("review_message").assertTextEquals("Review posted")
    }

    @Test
    fun failedSave_showsErrorAndKeepsTheText() {
        var finish: ((Boolean) -> Unit)? = null
        composeRule.setContent {
            ReviewSection(campsiteId = "camp1", entrantId = "e1", save = { _, onDone -> finish = onDone })
        }

        composeRule.onNodeWithTag("review_text").performTextInput("Nice spot")
        composeRule.onNodeWithTag("review_post").performClick()
        composeRule.runOnIdle { finish?.invoke(false) }

        composeRule.onNodeWithTag("review_message").assertTextEquals("Could not post, please try again.")
        composeRule.onNodeWithTag("review_text").assertTextEquals("Nice spot")
    }

    @Test
    fun nobodySignedIn_asksToSignIn() {
        var saved: Review? = null
        composeRule.setContent {
            ReviewSection(campsiteId = "camp1", entrantId = "", save = { review, _ -> saved = review })
        }

        composeRule.onNodeWithTag("review_text").performTextInput("Nice")
        composeRule.onNodeWithTag("review_post").performClick()

        composeRule.onNodeWithTag("review_message").assertTextEquals("Please sign in first")
        assertNull(saved)
    }
}