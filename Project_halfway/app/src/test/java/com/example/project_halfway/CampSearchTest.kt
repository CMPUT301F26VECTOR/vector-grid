
package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CampSearchTest {

    private fun createCampsite(
        name: String,
        campground: String,
        location: String,
        occupancy: Int,
        dates: List<Map<String, String>> = emptyList()
    ): CampsiteModel {
        return CampsiteModel(
            name,
            campground,
            location,
            -115.57,
            51.17,
            occupancy,
            4.5,
            "A campsite",
            "image.jpg",
            availableDateRanges = dates
        )
    }

    @Test
    fun searchCampsites_matchesName() {
        val campsite = createCampsite("Lakeview", "Pine Campground", "Banff", 4)

        val results = CampSearch().searchCampsites(listOf(campsite), "lake")

        assertEquals(1, results.size)
        assertEquals("Lakeview", results[0].getCampsiteName())
    }

    @Test
    fun searchCampsites_matchesCampground() {
        val campsite = createCampsite("Lakeview", "Pine Campground", "Banff", 4)

        val results = CampSearch().searchCampsites(listOf(campsite), "pine")

        assertEquals(1, results.size)
    }

    @Test
    fun searchCampsites_matchesLocation() {
        val campsite = createCampsite("Lakeview", "Pine Campground", "Banff", 4)

        val results = CampSearch().searchCampsites(listOf(campsite), "banff")

        assertEquals(1, results.size)
    }

    @Test
    fun searchCampsites_noMatch() {
        val campsite = createCampsite("Lakeview", "Pine Campground", "Banff", 4)

        val results = CampSearch().searchCampsites(listOf(campsite), "Jasper")

        assertEquals(0, results.size)
    }

    @Test
    fun filterCampsites_matchesLocation() {
        val lake = createCampsite("Lakeview", "Pine Campground", "Banff", 4)
        val mountain = createCampsite("Mountain", "Rocky Campground", "Jasper", 6)

        val results = CampSearch().filterCampsites(
            listOf(lake, mountain),
            "Banff",
            null
        )

        assertEquals(1, results.size)
        assertEquals("Lakeview", results[0].getCampsiteName())
    }

    @Test
    fun filterCampsites_matchesGuests() {
        val lake = createCampsite("Lakeview", "Pine Campground", "Banff", 4)
        val forest = createCampsite("Forest", "Green Campground", "Banff", 2)

        val results = CampSearch().filterCampsites(
            listOf(lake, forest),
            "",
            3
        )

        assertEquals(1, results.size)
        assertEquals("Lakeview", results[0].getCampsiteName())
    }

    @Test
    fun filterCampsites_matchesBoth() {
        val lake = createCampsite("Lakeview", "Pine Campground", "Banff", 4)
        val forest = createCampsite("Forest", "Green Campground", "Banff", 2)
        val mountain = createCampsite("Mountain", "Rocky Campground", "Jasper", 6)

        val results = CampSearch().filterCampsites(
            listOf(lake, forest, mountain),
            "Banff",
            3
        )

        assertEquals(1, results.size)
        assertEquals("Lakeview", results[0].getCampsiteName())
    }

    @Test
    fun filterByDates_available() {
        val campsite = createCampsite(
            "Lakeview", "Pine Campground", "Banff", 4,
            listOf(mapOf("start" to "2026-11-01", "end" to "2026-11-10"))
        )

        val results = CampSearch().filterByDates(
            listOf(campsite),
            LocalDate.parse("2026-11-03"),
            LocalDate.parse("2026-11-05")
        )

        assertEquals(1, results.size)
    }

    @Test
    fun filterByDates_notAvailable() {
        val campsite = createCampsite(
            "Lakeview", "Pine Campground", "Banff", 4,
            listOf(mapOf("start" to "2026-11-02", "end" to "2026-11-10"))
        )

        val results = CampSearch().filterByDates(
            listOf(campsite),
            LocalDate.parse("2026-11-01"),
            LocalDate.parse("2026-11-03")
        )

        assertEquals(0, results.size)
    }

    @Test
    fun filterByDates_exactRange() {
        val campsite = createCampsite(
            "Lakeview", "Pine Campground", "Banff", 4,
            listOf(mapOf("start" to "2026-11-01", "end" to "2026-11-03"))
        )

        val results = CampSearch().filterByDates(
            listOf(campsite),
            LocalDate.parse("2026-11-01"),
            LocalDate.parse("2026-11-03")
        )

        assertEquals(1, results.size)
    }

    @Test
    fun filterByDates_invalidRange() {
        val campsite = createCampsite(
            "Lakeview", "Pine Campground", "Banff", 4,
            listOf(mapOf("start" to "2026-11-01", "end" to "2026-11-10"))
        )

        val results = CampSearch().filterByDates(
            listOf(campsite),
            LocalDate.parse("2026-11-05"),
            LocalDate.parse("2026-11-03")
        )

        assertEquals(0, results.size)
    }


    @Test
    fun combinedFilters_returnsOnlyMatchingCampsite() {
        val campsites = listOf(
            CampsiteModel(
                "Lakeview", "Pine Campground", "Banff",
                -115.57, 51.17, 4, 4.5,
                "Lake campsite", "image.jpg",
                availableDateRanges = listOf(
                    mapOf("start" to "2026-11-01", "end" to "2026-11-10")
                )
            ),
            CampsiteModel(
                "Mountain Camp", "Rocky Campground", "Jasper",
                -118.08, 52.87, 6, 4.2,
                "Mountain campsite", "image.jpg",
                availableDateRanges = listOf(
                    mapOf("start" to "2026-11-01", "end" to "2026-11-15")
                )
            ),
            CampsiteModel(
                "Pine Forest", "Forest Campground", "Banff",
                -115.60, 51.20, 8, 4.8,
                "Forest campsite", "image.jpg",
                availableDateRanges = listOf(
                    mapOf("start" to "2026-11-05", "end" to "2026-11-20")
                )
            )
        )

        val search = CampSearch()

        val locationAndGuests = search.filterCampsites(
            campsites,
            "Banff",
            5
        )

        val finalResults = search.filterByDates(
            locationAndGuests,
            LocalDate.parse("2026-11-06"),
            LocalDate.parse("2026-11-09")
        )

        assertEquals(1, finalResults.size)
        assertEquals("Pine Forest", finalResults[0].getCampsiteName())
    }

}
