package com.example.project_halfway

class AllOwners {

    private val _allOwnersList = mutableListOf<Owner>()

    val allOwners: MutableList<Owner>
        get() = _allOwnersList

    fun addOwner(campsiteOwner: Owner){
        _allOwnersList.add(campsiteOwner)
    }
}