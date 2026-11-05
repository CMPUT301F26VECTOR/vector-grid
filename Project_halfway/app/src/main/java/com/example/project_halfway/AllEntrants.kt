package com.example.project_halfway

import kotlin.collections.mutableListOf

/**
 * This class is collection of all the entrants
 * it contains all their information
 */
class AllEntrants {
    private val _allEntrantsProfile = mutableListOf<EntrantProfile>()

    val profiles: List<EntrantProfile>
        get() = _allEntrantsProfile


    fun addProfile(newProfile:EntrantProfile){
        _allEntrantsProfile.add(newProfile)
    }
}