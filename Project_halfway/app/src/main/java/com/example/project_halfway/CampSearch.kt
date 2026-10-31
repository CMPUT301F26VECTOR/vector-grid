package com.example.project_halfway

/**
 * Provides search functionality for campsite listings.
 */
class CampSearch {

    /**
     * Returns campsite's location or name  which matches the search query.
     * Add other filters like dates and number of guest later on
     */

    fun searchCampsites(
        campsites: List<CampsiteModel>,
        query: String
    ):List<CampsiteModel>{
        val results = mutableListOf<CampsiteModel>()

        for (campsite in campsites) {

            val name = campsite.getCampsiteName()
            val location = campsite.getCampsiteLocation()

            if (name.contains(query, ignoreCase = true)) {
                results.add(campsite)
            }
            else if (location.contains(query, ignoreCase = true)) {
                results.add(campsite)
            }
        }

        return results
    }
}