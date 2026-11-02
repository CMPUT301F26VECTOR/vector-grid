package com.example.project_halfway


import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidUserException

import com.google.firebase.firestore.Source
import com.google.firebase.auth.FirebaseUser


object EntrantAuthManager {

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



