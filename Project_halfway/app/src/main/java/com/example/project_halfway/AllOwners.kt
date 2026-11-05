package com.example.project_halfway

/**
 * A list of every campsite owner in the app, used by [Admin].
 *
 * Outstanding issues: only kept in memory and not used by any screen yet;
 * owners are actually stored in the Firestore "Users" collection.
 * allOwners returns the list itself, so callers can change it directly.
 */
class AllOwners {

    private val _allOwnersList = mutableListOf<Owner>()

    /** Every owner added so far. */
    val allOwners: MutableList<Owner>
        get() = _allOwnersList

    /**
     * Adds a campsite owner to the list.
     *
     * @param campsiteOwner the owner to add
     */
    fun addOwner(campsiteOwner: Owner){
        _allOwnersList.add(campsiteOwner)
    }
}