package com.example.project_halfway

import kotlin.collections.mutableListOf

/**
 * A list of every entrant profile in the app, used by [Admin].
 *
 * Outstanding issues: only kept in memory and not used by any screen yet;
 * entrant profiles are actually stored in the Firestore "Users" collection.
 */
class AllEntrants {
    private val _allEntrantsProfile = mutableListOf<EntrantProfile>()

    /** Every entrant profile added so far. Read only. */
    val profiles: List<EntrantProfile>
        get() = _allEntrantsProfile


    /**
     * Adds an entrant profile to the list.
     *
     * @param newProfile the profile to add
     */
    fun addProfile(newProfile:EntrantProfile){
        _allEntrantsProfile.add(newProfile)
    }
}