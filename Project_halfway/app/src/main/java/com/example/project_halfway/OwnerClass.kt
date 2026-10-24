package com.example.project_halfway

class OwnerClass (
    private val Name:String,
    private val EmailAddress:String,
    private val Password: String,
    private val ProfilePictureURL:String
){
    private val _allOwnerCampsites = mutableListOf<CampsiteClass>()

    // only the campsite owner can add or remove
    // if we do mutable list then all can change it
    val campsites: List<CampsiteClass>
        get() = _allOwnerCampsites

    fun addCampsite(newCampsite: CampsiteClass){
        _allOwnerCampsites.add(newCampsite)
    }
}