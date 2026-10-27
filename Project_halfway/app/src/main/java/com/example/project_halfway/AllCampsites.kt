package com.example.project_halfway

class AllCampsites {
    private val _ArrayofCampsites= mutableListOf<CampsiteModel>()

    val allCampsites: MutableList<CampsiteModel>
        get() = _ArrayofCampsites


    fun addCampsite(newCampsite:CampsiteModel){
        _ArrayofCampsites.add(newCampsite)
    }

}