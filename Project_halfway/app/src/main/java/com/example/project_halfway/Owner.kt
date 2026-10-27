package com.example.project_halfway

class Owner (
    private val Name:String,
    private val EmailAddress:String,
    private val Password: String,
    private val ProfilePictureURL:String
){
    private val _allOwnerCampsites = mutableListOf<CampsiteModel>()

    // only the campsite owner can add or remove
    // if we do mutable list then all can change it
    val campsites: List<CampsiteModel>
        get() = _allOwnerCampsites

    fun addCampsite(newCampsite: CampsiteModel){
        _allOwnerCampsites.add(newCampsite)
    }
}