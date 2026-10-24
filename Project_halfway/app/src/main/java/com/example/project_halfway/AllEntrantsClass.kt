package com.example.project_halfway

import kotlin.collections.mutableListOf

class AllEntrantsClass {
    private val _allEntrantsProfile = mutableListOf<EntrantProfile>()

    val profiles: List<EntrantProfile>
        get() = _allEntrantsProfile


    fun addProfile(newProfile:EntrantProfile){
        _allEntrantsProfile.add(newProfile)
    }
}