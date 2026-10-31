package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

class CampSearchTest {

    @Test
    fun searchCampsites_matchesName() {

        val campsite = CampsiteModel(
            "Lakeview",
            "Pine Campground",
            "Banff",
            -115.57,
            51.17,
            4,
            4.5,
            "A campsite near a lake",
            "image.jpg"
        )

        val campsites = listOf(campsite)

        val search = CampSearch()

        val results = search.searchCampsites(campsites, "lake")

        assertEquals(1, results.size)
        assertEquals("Lakeview", results[0].getCampsiteName())
    }
}