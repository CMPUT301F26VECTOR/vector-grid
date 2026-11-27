package com.example.project_halfway

import androidx.compose.foundation.Image
import com.google.firebase.firestore.ListenerRegistration
import com.google.type.PhoneNumber
import com.google.firebase.storage.StorageException
object AdminRepository {
    fun campsitedelete(
        campsiteId: String,
        onDone: (Boolean) -> Unit
    ) {
        FirebaseSecurity.adminDB.collection("campsites").document(campsiteId)
            .delete().addOnSuccessListener { onDone(true) }.addOnFailureListener { onDone(false) }
    }

    // this function just retrieves the entrants from firebase
    fun retrieveAllEntrants(
        Output: (List<EntrantProfile>) -> Unit
    ): ListenerRegistration {
        return FirebaseSecurity.adminDB.collection("Users").whereEqualTo("role", "entrant")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val entrants = mutableListOf<EntrantProfile>()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val entrant = EntrantProfile(
                            Name = document.getString("Name") ?: "",
                            EmailAddress = document.getString("EmailAddress") ?: "",
                            PhoneNumber = document.getString("PhoneNumber") ?: "",
                            ProfilePicture = document.getString("ProfilePicture") ?: "",
                            uid = document.id,
                            fcmToken = document.getString("fcmToken") ?: ""
                        )

                        entrants.add(entrant)
                    }
                }
                Output(entrants)
            }
    }

    fun retrieveAllOwners(output: (List<Owner>) -> Unit): ListenerRegistration {
        return FirebaseSecurity.adminDB.collection("Users").whereEqualTo("role", "owner")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val owners = mutableListOf<Owner>()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        val owner = Owner(
                            Name = document.getString("Name") ?: "",
                            EmailAddress = document.getString("EmailAddress") ?: "",
                            ProfilePictureURL = document.getString("ProfilePictureURL") ?: "",
                            uid = document.id
                        )
                        owners.add(owner)
                    }
                }
                output(owners)
            }
    }

    //we are using this function a common function for both the
    //entrants and the owners becasuse they both are under users collections
    fun deleteUser(
        uid: String,
        onDone: (Boolean) -> Unit
    ) {
        FirebaseSecurity.adminDB.collection("Users").document(uid)
            .delete().addOnSuccessListener { onDone(true) }.addOnFailureListener { (onDone(true)) }
    }

    fun gettingAllImages(
        onResult: (List<ImageAdmin>) -> Unit,
        onError: (String) -> Unit
    ) {
        val db = FirebaseSecurity.adminDB

        db.collection(("Users")).get().addOnSuccessListener { userResults ->
            val profilePic = userResults.documents.mapNotNull { doc ->
                val url = doc.getString("ProfilePicture")
                if (url.isNullOrBlank()) {
                    null
                } else {
                    ImageAdmin(
                        documentId = doc.id,
                        imageURL = url,
                        collection = "Users",
                        field = "ProfilePicture"
                    )
                }
            }

            // here we ask for campsite images
            db.collection("campsites").get().addOnSuccessListener { campsiteResult ->
                val campsiteImages = campsiteResult.documents.mapNotNull { doc ->
                    val url = doc.getString("imageUrl")
                    if (url.isNullOrBlank()) {
                        null
                    } else {
                        ImageAdmin(
                            documentId = doc.id,
                            imageURL = url,
                            collection = "campsites",
                            field = "imageUrl"
                        )
                    }
                }
                onResult(profilePic + campsiteImages)
            }.addOnFailureListener { onError("Failed to get campsite Image:${it.message}") }

        }.addOnFailureListener { onError("Failed to get the profile image ") }

    }

    fun ImageDelete(image: ImageAdmin, onSuccess: ()->Unit, onError:(String)->Unit){

        val storage = FirebaseSecurity.adminStorage
        val db = FirebaseSecurity.adminDB

        storage.getReferenceFromUrl(image.imageURL).delete().addOnSuccessListener {
            db.collection(image.collection).document(image.documentId)
                .update(image.field,"").addOnSuccessListener {
                    onSuccess()
                 }.addOnFailureListener { error->
                     onError("the update has failed:$error.message")
                }
        }.addOnFailureListener { error->
            onError("Image not deleted: ${error.message}")
        }

    }

}