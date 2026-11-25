package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Tests for the admin ratings list (US 03.09.01). They use fake campsites, so no Firebase is needed. */
class AdminRatingsListTest {

    @get:Rule
    val rule = createComposeRule()

    private fun campsite(id: String, name: String, ratings: List<Double>) = CampsiteModel(
        campsiteName = name,
        campgroundName = "",
        location = "",
        longitude = 0.0,
        latitude = 0.0,
        occupancy = 4,
        description = "",
        imageUrl = "",
        id = id,
        ratings = ratings
    )

    @Test
    fun eachRating_isShownWithARemoveButton() {
        rule.setContent {
            AdminRatingsList(
                campsites = listOf(campsite("c1", "Site 56A", listOf(5.0, 3.0))),
                message = "",
                onRemove = { _, _ -> }
            )
        }
        rule.onNodeWithText("Site 56A").assertIsDisplayed()
        rule.onNodeWithTag("admin_rating_remove_c1_0").assertIsDisplayed()
        rule.onNodeWithTag("admin_rating_remove_c1_1").assertIsDisplayed()
    }

    @Test
    fun campsiteWithoutRatings_isNotListed() {
        rule.setContent {
            AdminRatingsList(
                campsites = listOf(campsite("c2", "Quiet Site", emptyList())),
                message = "",
                onRemove = { _, _ -> }
            )
        }
        rule.onNodeWithTag("admin_rating_empty").assertIsDisplayed()
    }

    @Test
    fun tappingRemove_givesBackThatCampsiteAndPosition() {
        var removedId = ""
        var removedIndex = -1
        rule.setContent {
            AdminRatingsList(
                campsites = listOf(campsite("c1", "Site 56A", listOf(5.0, 3.0))),
                message = "",
                onRemove = { c, i -> removedId = c.getId(); removedIndex = i }
            )
        }
        rule.onNodeWithTag("admin_rating_remove_c1_1").performClick()
        assertEquals("c1", removedId)
        assertEquals(1, removedIndex)
    }
}