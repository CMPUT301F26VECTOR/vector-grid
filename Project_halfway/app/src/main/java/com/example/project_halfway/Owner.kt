package com.example.project_halfway

data class Owner (
    val Name:String,
    val EmailAddress:String,
    val ProfilePictureURL: String,
    val uid:String
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