package com.example.project_halfway

/**
 * This class contains all campsites information
 * it is like a collection of all the campsites.
 */
class AllCampsites {
    private val _ArrayofCampsites= mutableListOf<CampsiteModel>()

    val allCampsites: MutableList<CampsiteModel>
        get() = _ArrayofCampsites


    fun addCampsite(newCampsite:CampsiteModel){
        _ArrayofCampsites.add(newCampsite)
    }
}
