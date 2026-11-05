package com.example.project_halfway


/**
 * This class is for collection of all the owners
 * with all their specific details.
 */
class AllOwners {

    private val _allOwnersList = mutableListOf<Owner>()

    val allOwners: MutableList<Owner>
        get() = _allOwnersList

    fun addOwner(campsiteOwner: Owner){
        _allOwnersList.add(campsiteOwner)
    }
}