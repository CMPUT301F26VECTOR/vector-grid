package com.example.project_halfway

/**
 * This class represents the Owner class and contains
 * its properties like name, email,
 * pic and uid. These properties will be used to store
 * in a list and later will be used by firebase to
 * store them in the database.
 *
 * @property Name This is the name of entrant
 * @property EmailAddress this is the email of entrant
 * @property uid this it the unique identifier on firebase database
 */
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