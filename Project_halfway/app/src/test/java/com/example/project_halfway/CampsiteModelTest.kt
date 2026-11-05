package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CampsiteModel.getAvailableDateRangesString, the dates shown
 * on the campsite summary card, and CampsiteModel.hasMapLocation, which decides
 * whether the details card shows a map (US 01.01.05).
 */
class CampsiteModelTest {

    private fun campsite(ranges: List<Map<String, String>>) = CampsiteModel(
        campsiteName = "Site 56A",
        campgroundName = "Lakeside",
        location = "Edmonton",
        longitude = 0.0,
        latitude = 0.0,
        occupancy = 4,
        description = "",
        imageUrl = "",
        availableDateRanges = ranges
    )

    @Test
    fun noRanges_saysNoDatesFound() {
        assertEquals("No available dates found.", campsite(emptyList()).getAvailableDateRangesString())
    }

    @Test
    fun oneRange_showsStartAndEnd() {
        val ranges = listOf(mapOf("start" to "2026-11-10", "end" to "2026-11-15"))
        assertEquals("2026-11-10 - 2026-11-15", campsite(ranges).getAvailableDateRangesString())
    }

    @Test
    fun twoRanges_areSeparatedByACommaWithNoTrailingComma() {
        val ranges = listOf(
            mapOf("start" to "2026-11-10", "end" to "2026-11-15"),
            mapOf("start" to "2026-12-01", "end" to "2026-12-05")
        )
        assertEquals(
            "2026-11-10 - 2026-11-15, 2026-12-01 - 2026-12-05",
            campsite(ranges).getAvailableDateRangesString()
        )
    }

    // A campsite with just a map pin, for the hasMapLocation tests
    private fun campsiteAt(latitude: Double, longitude: Double) = CampsiteModel(
        campsiteName = "Whistlers",
        campgroundName = "Jasper",
        location = "Jasper, AB",
        longitude = longitude,
        latitude = latitude,
        occupancy = 6,
        description = "",
        imageUrl = ""
    )

    @Test
    fun noPin_hasNoMapLocation() {
        assertFalse(campsiteAt(0.0, 0.0).hasMapLocation())
    }

    @Test
    fun realPin_hasMapLocation() {
        assertTrue(campsiteAt(52.8507, -118.077).hasMapLocation())
    }

    @Test
    fun pinOnTheEquatorOrPrimeMeridian_stillCounts() {
        // Only exactly (0, 0) means "no pin"
        assertTrue(campsiteAt(0.0, -118.077).hasMapLocation())
        assertTrue(campsiteAt(52.8507, 0.0).hasMapLocation())
    }
}
