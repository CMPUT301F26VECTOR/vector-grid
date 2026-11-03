
package com.example.project_halfway

import java.time.LocalDate

/**
 * Provides search and filtering functionality for campsite listings.
 */
class CampSearch {

    /**
     * Searches campsites by name, campground name, or location.
     * US 01.01.01
     */
    fun searchCampsites(
        campsites: List<CampsiteModel>,
        query: String
    ): List<CampsiteModel> {

        val results = mutableListOf<CampsiteModel>()

        for (campsite in campsites) {
            val name = campsite.getCampsiteName()
            val campground = campsite.getCampgroundName()
            val location = campsite.getCampsiteLocation()

            if (
                name.contains(query, ignoreCase = true) ||
                campground.contains(query, ignoreCase = true) ||
                location.contains(query, ignoreCase = true)
            ) {
                results.add(campsite)
            }
        }

        return results
    }

    /**
     * Filters campsites by location and required guest capacity.
     * An empty location or null guest count means no filter.
     * US 01.01.02
     */
    fun filterCampsites(
        campsites: List<CampsiteModel>,
        location: String,
        guests: Int?
    ): List<CampsiteModel> {

        val results = mutableListOf<CampsiteModel>()

        for (campsite in campsites) {
            val matchesLocation =
                location.isBlank() ||
                        campsite.getCampsiteLocation().contains(location, ignoreCase = true)

            val matchesGuests =
                guests == null || campsite.getOccupancy() >= guests

            if (matchesLocation && matchesGuests) {
                results.add(campsite)
            }
        }

        return results
    }
    /**
     * Filtering by dates
     */
    fun filterByDates(
        campsites: List<CampsiteModel>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CampsiteModel> {

        val results = mutableListOf<CampsiteModel>()

        //Check in and check out dates
        if (!startDate.isBefore(endDate)) {
            return results
        }

        for (campsite in campsites) {

            for (range in campsite.availableDateRanges) {
                val start = range["start"]
                val end = range["end"]

                if (start != null && end != null) {
                    val availableStart = LocalDate.parse(start)
                    val availableEnd = LocalDate.parse(end)

                    if (
                        !startDate.isBefore(availableStart) &&
                        !endDate.isAfter(availableEnd)
                    ) {
                        results.add(campsite)
                        break
                    }
                }
            }
        }

        return results
    }
}
