package com.example.project_halfway

/**
 * this class will contain all the
 * information for all the entrants, owners,
 * campsites and images.(yet to be implemented.)
 */

class Admin (
    private val Name:String,
    private val Password:String,
    private val EmailAddress:String,
    private val profilePicURL: String,
    private val allEntrants: AllEntrants,
    private val allOwnersClass: AllOwners,
    private val allCampsitesClass: AllCampsites
)



