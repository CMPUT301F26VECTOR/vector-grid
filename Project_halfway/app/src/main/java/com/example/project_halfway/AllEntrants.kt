package com.example.project_halfway

import kotlin.collections.mutableListOf

class AllEntrants {
    private val _allEntrantsProfile = mutableListOf<EntrantProfile>()

    val profiles: List<EntrantProfile>
        get() = _allEntrantsProfile


    fun addProfile(newProfile:EntrantProfile){
        _allEntrantsProfile.add(newProfile)
    }
}