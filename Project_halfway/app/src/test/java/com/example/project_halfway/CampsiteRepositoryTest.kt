package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

// Tests that a campsite survives being turned into Firestore data and back.
// These tests don't use the real database, so they run on the computer.

class CampsiteRepositoryTest {

    // A realistic campsite, like the one on our summary card mockup.
    private fun sampleCampsite(): CampsiteModel {
        return CampsiteModel(
            campsiteName = "Site 56A",
            campgroundName = "Campground A",
            location = "Edmonton, AB",
            longitude = -113.4938,
            latitude = 53.5461,
            occupancy = 3,
            userRating = 4.5,
            description = "Quiet tent site near the lake",
            imageUrl = "https://example.com/site56a.jpg",
            id = "camp_001",
            ownerId = "owner_7",
            type = "Tent site",
            services = listOf("Power", "Non-potable water"),
            pricePerNight = 43.0
        )
    }

    @Test
    fun toMapThenFromMap_keepsAllValues() {
        val original = sampleCampsite()

        val map = CampsiteRepository.toMap(original)
        val copy = CampsiteRepository.fromMap("camp_001", map)

        assertEquals("Site 56A", copy.getCampsiteName())
        assertEquals("Campground A", copy.getCampgroundName())
        assertEquals("Edmonton, AB", copy.getCampsiteLocation())
        assertEquals(-113.4938, copy.getLongitude(), 0.0001)
        assertEquals(53.5461, copy.getLatitude(), 0.0001)
        assertEquals(3, copy.getOccupancy())
        assertEquals(4.5, copy.getUserRating(), 0.0001)
        assertEquals("Quiet tent site near the lake", copy.getDescription())
        assertEquals("https://example.com/site56a.jpg", copy.getImageUrl())
        assertEquals("camp_001", copy.getId())
        assertEquals("owner_7", copy.getOwnerId())
        assertEquals("Tent site", copy.getType())
        assertEquals(listOf("Power", "Non-potable water"), copy.getServices())
        assertEquals(43.0, copy.getPricePerNight(), 0.0001)
    }

    @Test
    fun toMap_leavesOutTheId() {
        // The id is the document's name in Firestore, not a line inside it.
        val map = CampsiteRepository.toMap(sampleCampsite())

        assertFalse(map.containsKey("id"))
    }

    @Test
    fun fromMap_withMissingFields_usesBlankAndZero() {
        val copy = CampsiteRepository.fromMap("camp_002", emptyMap<String, Any>())

        assertEquals("", copy.getCampsiteName())
        assertEquals(0, copy.getOccupancy())
        assertEquals(0.0, copy.getPricePerNight(), 0.0001)
        assertEquals("camp_002", copy.getId())
    }

    @Test
    fun toMapThenFromMap_keepsTagsAndDates() {
        // US 02.01.02: tags and available dates must survive the trip to Firestore
        val original = CampsiteModel(
            "Site 12", "Elk Island", "Fort Saskatchewan, AB", -112.86, 53.61,
            4, 0.0, "Walk-in site", "",
            tags = listOf("Lakeside", "Pet friendly"),
            availableDateRanges = listOf(mapOf("start" to "2027-06-01", "end" to "2027-06-15"))
        )

        val copy = CampsiteRepository.fromMap("camp_003", CampsiteRepository.toMap(original))

        assertEquals(listOf("Lakeside", "Pet friendly"), copy.getTags())
        assertEquals("2027-06-01", copy.getAvailableDateRanges()[0]["start"])
        assertEquals("2027-06-15", copy.getAvailableDateRanges()[0]["end"])
    }
}