package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for removing one rating from a list (US 03.09.01). */
class AdminRatingRepositoryTest {

    @Test
    fun removingOneRating_keepsTheOthersInOrder() {
        val result = AdminRatingRepository.withoutRating(listOf(5.0, 3.0, 4.0), 1)
        assertEquals(listOf(5.0, 4.0), result)
    }

    @Test
    fun removingTheOnlyRating_givesAnEmptyList() {
        val result = AdminRatingRepository.withoutRating(listOf(2.0), 0)
        assertEquals(emptyList<Double>(), result)
    }

    @Test
    fun badIndex_changesNothing() {
        val ratings = listOf(5.0, 3.0)
        assertEquals(ratings, AdminRatingRepository.withoutRating(ratings, 5))
        assertEquals(ratings, AdminRatingRepository.withoutRating(ratings, -1))
    }
}