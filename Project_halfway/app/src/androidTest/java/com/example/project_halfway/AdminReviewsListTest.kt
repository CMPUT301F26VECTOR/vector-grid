package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Tests for the admin review list (US 03.10.01). They use fake reviews,
 * so no Firebase is needed.
 */
class AdminReviewsListTest {

    @get:Rule
    val rule = createComposeRule()

    private val first = Review("r1", "camp1", "entrant1", "Lovely quiet spot", 2L)
    private val second = Review("r2", "camp1", "entrant2", "Too many mosquitoes", 1L)

    @Test
    fun eachReview_isShownWithARemoveButton() {
        rule.setContent {
            AdminReviewsList(reviews = listOf(first, second), message = "", onRemove = {})
        }
        rule.onNodeWithText("Lovely quiet spot").assertIsDisplayed()
        rule.onNodeWithText("Too many mosquitoes").assertIsDisplayed()
        rule.onNodeWithTag("admin_review_remove_r1").assertIsDisplayed()
        rule.onNodeWithTag("admin_review_remove_r2").assertIsDisplayed()
    }

    @Test
    fun noReviews_showsNoReviewsYet() {
        rule.setContent {
            AdminReviewsList(reviews = emptyList(), message = "", onRemove = {})
        }
        rule.onNodeWithTag("admin_review_empty").assertIsDisplayed()
    }

    @Test
    fun tappingRemove_givesBackThatReview() {
        var removed: Review? = null
        rule.setContent {
            AdminReviewsList(
                reviews = listOf(first, second),
                message = "",
                onRemove = { removed = it }
            )
        }
        rule.onNodeWithTag("admin_review_remove_r2").performClick()
        assertEquals("r2", removed?.id)
    }

    @Test
    fun message_isShown() {
        rule.setContent {
            AdminReviewsList(reviews = listOf(first), message = "Review removed", onRemove = {})
        }
        rule.onNodeWithTag("admin_review_message").assertIsDisplayed()
    }
}