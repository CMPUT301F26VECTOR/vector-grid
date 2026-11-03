package com.example.project_halfway


import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidUserException

import com.google.firebase.firestore.Source
import com.google.firebase.auth.FirebaseUser


/**
 * Signs entrants in with their device, with no username or password
 * (Firebase anonymous sign-in), and makes sure each entrant has a profile
 * in the Firestore "Users" collection with role "entrant".
 *
 * Used for US 01.09.01 (entrants are identified by their device).
 *
 * Outstanding issues: if the signed-in account's profile exists but its role
 * is not "entrant", neither onSuccess nor onFailure is called, so the screen
 * keeps showing "Loading...".
 */
object EntrantAuthManager {

    /**
     * Signs the entrant in with this device. Reuses the device's existing
     * anonymous account if there is one, otherwise creates a new one, then
     * checks or creates the entrant's profile.
     *
     * @param onSuccess called with the signed-in user once their profile is ready
     * @param onFailure called with an error message if sign-in fails, or if a
     * different (non-anonymous) account is already signed in
     */
    fun signIn(
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val auth = FirebaseSecurity.auth
        val existingUser = auth.currentUser

        if (existingUser != null && existingUser.isAnonymous) {

            existingUser.reload()
                .addOnSuccessListener {
                    ensureProfile(existingUser, onSuccess, onFailure)
                }
                .addOnFailureListener { exception ->

                    if (exception is FirebaseAuthInvalidUserException) {
                        auth.signOut()
                        createAnonymousAccount(onSuccess, onFailure)
                    } else {
                        onFailure(
                            exception.localizedMessage ?: "Failed to verify user"
                        )
                    }
                }

        } else if (existingUser == null) {

            createAnonymousAccount(onSuccess, onFailure)

        } else {
            onFailure("Another account is already signed in")
        }
    }

    // Makes a new anonymous Firebase account for this device, then sets up its profile.
    private fun createAnonymousAccount(
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseSecurity.auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val user = result.user

                if (user != null) {
                    ensureProfile(user, onSuccess, onFailure)
                } else {
                    onFailure("Unable to get Firebase user")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(
                    exception.localizedMessage ?: "Anonymous sign-in failed"
                )
            }
    }
    // Loads the user's "Users" document; creates a blank entrant profile if it doesn't exist yet.
    private fun ensureProfile(
        user: FirebaseUser,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val document = FirebaseSecurity.db
            .collection("Users")
            .document(user.uid)

        document.get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    if (snapshot.getString("role") == "entrant") {
                        onSuccess(user)
                    }
                } else {
                    val entrant = EntrantProfile(
                        Name = "",
                        EmailAddress = "",
                        PhoneNumber = "",
                        ProfilePicture = "",
                        uid = user.uid
                    )
                    val entrantdata = hashMapOf(
                        "Name" to entrant.Name,
                        "EmailAddress" to entrant.EmailAddress,
                        "PhoneNumber" to entrant.PhoneNumber,
                        "ProfilePicture" to entrant.ProfilePicture,
                        "uid" to entrant.uid,
                        "role" to "entrant"
                    )
                    document.set(entrantdata)
                        .addOnSuccessListener {
                            onSuccess(user)
                        }
                        .addOnFailureListener { exception ->
                            onFailure(
                                exception.localizedMessage
                                    ?: "Failed to create entrant profile"
                            )
                        }
                }

            }
            .addOnFailureListener {exception->
                onFailure(
                    exception.localizedMessage
                        ?: "Failed to check entrant profile"
                )
            }
    }
}



