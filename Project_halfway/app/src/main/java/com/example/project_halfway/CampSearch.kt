
package com.example.project_halfway

import java.time.LocalDate

/**
 * Provides search and filtering functionality for campsite listings.
 * Works on a list already loaded from Firestore, so it has no Firebase code
 * and can be unit tested (see CampSearchTest).
 *
 * Used for US 01.01.01 and US 01.01.02.
 *
 * Outstanding issues: filterByDates expects dates in YYYY-MM-DD form; a
 * badly formatted date saved in Firestore would make it crash.
 */
class CampSearch {

    /**
     * Searches campsites by name, campground name, or location.
     * US 01.01.01
     *
     * @param campsites the campsites to search
     * @param query the text to look for (not case sensitive)
     * @return the campsites whose name, campground or location contains the query
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
     *
     * @param campsites the campsites to filter
     * @param location text the campsite's location must contain, or "" for any
     * @param guests how many people must fit, or null for any
     * @return the campsites that match both filters
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
     * Filtering by dates: keeps the campsites that are available for the whole
     * stay, i.e. one of their available date ranges covers check-in to check-out.
     * US 01.01.02
     *
     * @param campsites the campsites to filter
     * @param startDate check-in date
     * @param endDate check-out date; must be after startDate
     * @return the campsites available for the whole stay, or an empty list if
     * endDate is not after startDate
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
