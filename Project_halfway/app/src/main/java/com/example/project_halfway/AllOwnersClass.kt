package com.example.project_halfway

class AllOwnersClass {

    private val _allOwnersList = mutableListOf<OwnerClass>()

    val allOwners: MutableList<OwnerClass>
        get() = _allOwnersList

    fun addOwner(campsiteOwner: OwnerClass){
        _allOwnersList.add(campsiteOwner)
    }
}