package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for Review.firstError (US 01.13.02). */
class ReviewTest {

    private fun review(text: String, campsiteId: String = "camp1", entrantId: String = "e1"): Review {
        return Review(campsiteId = campsiteId, entrantId = entrantId, text = text, createdAt = 1L)
    }

    @Test
    fun goodReview_hasNoError() {
        assertNull(review("Lovely spot, very quiet.").firstError())
    }

    @Test
    fun blankText_isRefused() {
        assertEquals("Please write something before posting.", review("   ").firstError())
    }

    @Test
    fun missingCampsiteOrEntrant_isRefused() {
        val message = "Could not tell which campsite or entrant this review is for."
        assertEquals(message, review("Nice", campsiteId = "").firstError())
        assertEquals(message, review("Nice", entrantId = "").firstError())
    }

    @Test
    fun exactlyMaxLength_isAllowed() {
        assertNull(review("a".repeat(Review.MAX_LENGTH)).firstError())
    }

    @Test
    fun overMaxLength_isRefused() {
        assertEquals(
            "A review can be at most 500 characters.",
            review("a".repeat(Review.MAX_LENGTH + 1)).firstError()
        )
    }
}