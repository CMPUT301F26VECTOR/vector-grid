package com.example.project_halfway

class AllCampsitesClass {
    private val _ArrayofCampsites= mutableListOf<CampsiteClass>()

    val allCampsites: MutableList<CampsiteClass>
        get() = _ArrayofCampsites


    fun addCampsite(newCampsite:CampsiteClass){
        _ArrayofCampsites.add(newCampsite)
    }

}