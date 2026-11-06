package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the text on the campsite summary card (US 01.01.04): rating,
 * place, guests, price and dates.
 */
class CampsiteCardTextTest {

    private fun campsite(campground: String, location: String) = CampsiteModel(
        campsiteName = "Site 42",
        campgroundName = campground,
        location = location,
        longitude = 0.0,
        latitude = 0.0,
        occupancy = 6,
        description = "",
        imageUrl = ""
    )

    @Test
    fun ratingText_noRatingSaysNoRatingsYet() {
        assertEquals("No ratings yet", ratingText(0.0))
    }

    @Test
    fun ratingCountText_singularAndPlural() {
        assertEquals("1 rating", ratingCountText(1))
        assertEquals("3 ratings", ratingCountText(3))
    }

    @Test
    fun ratingText_showsOneDecimal() {
        assertEquals("4.0", ratingText(4.0))
        assertEquals("3.5", ratingText(3.5))
    }

    @Test
    fun placeText_joinsCampgroundAndLocation() {
        assertEquals("Whistlers · Jasper, AB", placeText(campsite("Whistlers", "Jasper, AB")))
    }

    @Test
    fun placeText_withoutCampground_isJustTheLocation() {
        assertEquals("Jasper, AB", placeText(campsite("", "Jasper, AB")))
    }

    @Test
    fun guestsText_singularAndPlural() {
        assertEquals("1 guest", guestsText(1))
        assertEquals("6 guests", guestsText(6))
    }

    @Test
    fun priceText_hasTwoDecimals() {
        assertEquals("$38.50 / night", priceText(38.5))
    }

    @Test
    fun datesText_noRanges() {
        assertEquals("No dates yet", datesText(emptyList()))
    }

    @Test
    fun datesText_showsFirstRangeAndHowManyMore() {
        val ranges = listOf(
            mapOf("start" to "2027-07-02", "end" to "2027-07-09"),
            mapOf("start" to "2027-08-13", "end" to "2027-08-20")
        )
        assertEquals("2027-07-02 to 2027-07-09 (+1 more)", datesText(ranges))
    }
}
