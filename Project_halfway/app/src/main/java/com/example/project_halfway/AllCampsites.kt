package com.example.project_halfway

/**
 * A list of every campsite in the app, used by [Admin].
 *
 * Outstanding issues: only kept in memory and not used by any screen yet;
 * campsites are actually stored in Firestore through CampsiteRepository.
 * allCampsites returns the list itself, so callers can change it directly.
 */
class AllCampsites {
    private val _ArrayofCampsites= mutableListOf<CampsiteModel>()

    /** Every campsite added so far. */
    val allCampsites: MutableList<CampsiteModel>
        get() = _ArrayofCampsites


    /**
     * Adds a campsite to the list.
     *
     * @param newCampsite the campsite to add
     */
    fun addCampsite(newCampsite:CampsiteModel){
        _ArrayofCampsites.add(newCampsite)
    }
}
