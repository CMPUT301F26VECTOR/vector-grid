package com.example.project_halfway

/**
 * A campsite owner's account details, saved in the Firestore "Users"
 * collection when they sign up (see SignUpScreen).
 *
 * Outstanding issues: the campsites list is only kept in memory; the owner's
 * campsites are loaded from Firestore with CampsiteRepository.getByOwner.
 *
 * @property Name the owner's name
 * @property EmailAddress the email address they sign in with
 * @property ProfilePictureURL link to their profile picture, or "" if none
 * @property uid their Firebase Authentication user id
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
    /** The campsites this owner has added. Read only; use addCampsite to add one. */
    val campsites: List<CampsiteModel>
        get() = _allOwnerCampsites

    /**
     * Adds a campsite to this owner's list.
     *
     * @param newCampsite the campsite to add
     */
    fun addCampsite(newCampsite: CampsiteModel){
        _allOwnerCampsites.add(newCampsite)
    }
}