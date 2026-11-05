package com.example.project_halfway
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.Source
import com.google.firebase.auth.FirebaseUser

/**
 * This object is used when a sign up happens for entrant
 * it has three functions sigIN, create anonymous account and
 * ensure profile. the first is what sign up and the second is
 * for entrant which helps him signin anonymously wihout passsword
 * and email and the last fucntion is to ensure of the user
 * already have an account. it doesn't need to make another one
 * it can just use old uid from firebase and connect it.
 */


//this code was made with OpenAI ChatGPT and the resources provided by the TA via email.
// here create anonymous account and ensure profile were my ideas and has been implemented
// with the help above. Also, Ai was used to add listeners which were a great help to debug the code.
// the Ai was presented with my code that I wrote and it helped me modify it. 2026-10-22
//"help me with my sigIn, create anonymous account and ensure profile functions with the code I have
// already written for them"
object EntrantAuthManager {

<<<<<<< Updated upstream
=======
    /**
     * Signs the entrant in with this device. Reuses the device's existing
     * anonymous account if there is one, otherwise creates a new one, then
     * checks or creates the entrant's profile.
     *
     * @param onSuccess called with the signed-in user once their profile is ready
     * @param onFailure called with an error message if sign-in fails, or if a
     * different (non-anonymous) account is already signed in
     */
    //this code was made with OpenAI ChatGPT and the resources provided by the TA via email.
// here create anonymous account and ensure profile were my ideas and has been implemented
// with the help above. Also, Ai was used to add listeners which were a great help to debug the code.
// the Ai was presented with my code that I wrote and it helped me modify it. 2026-10-22
//"help me with my sigIn, create anonymous account and ensure profile functions with the code I have
// already written for them"
>>>>>>> Stashed changes
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



