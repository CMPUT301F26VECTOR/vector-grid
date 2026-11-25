package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for ReviewRepository.toMap and fromMap (US 01.13.02). No Firestore needed. */
class ReviewRepositoryTest {

    @Test
    fun toMap_holdsEveryField_andTrimsTheText() {
        val review = Review(campsiteId = "camp1", entrantId = "e1", text = "  Quiet place  ", createdAt = 5L)

        val map = ReviewRepository.toMap(review)

        assertEquals("camp1", map["campsiteId"])
        assertEquals("e1", map["entrantId"])
        assertEquals("Quiet place", map["text"])
        assertEquals(5L, map["createdAt"])
    }

    @Test
    fun fromMap_givesBackTheSameReview() {
        val map = mapOf<String, Any>("campsiteId" to "camp1", "entrantId" to "e1", "text" to "Nice", "createdAt" to 7L)

        val review = ReviewRepository.fromMap("doc9", map)

        assertEquals("doc9", review.id)
        assertEquals("camp1", review.campsiteId)
        assertEquals("e1", review.entrantId)
        assertEquals("Nice", review.text)
        assertEquals(7L, review.createdAt)
    }

    @Test
    fun fromMap_withMissingFields_usesBlanks() {
        val review = ReviewRepository.fromMap("doc9", emptyMap())

        assertEquals("", review.campsiteId)
        assertEquals("", review.text)
        assertEquals(0L, review.createdAt)
    }
}