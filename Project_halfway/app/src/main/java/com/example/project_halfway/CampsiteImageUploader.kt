package com.example.project_halfway

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

/**
 * Uploads a campsite's optional image to Firebase Storage (US 02.01.02).
 *
 * The owner picks a picture on the Register / Edit Campsite screen. This puts
 * it in Storage at campsite_images/<ownerId>/<time>.jpg, signed in as the
 * owner, and gives back a download link. The link is saved in the campsite's
 * imageUrl, and CampsiteImage shows it to entrants on the details screen.
 *
 * Outstanding issues: the Firebase project must be on the Blaze plan for
 * Storage to work. Replacing or removing an image leaves the old file in
 * Storage. The picture is uploaded at full size, not shrunk first.
 *
 * **AI assistance (Samarth Sathya Karthik):** this code was implemented using
 * Claude AI; Samarth tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
object CampsiteImageUploader {

    /**
     * Where in Storage an owner's picture goes. The time in the name keeps
     * every upload separate, so a new picture never overwrites an old one.
     *
     * @param ownerId user id of the signed in owner
     * @param time the current time in milliseconds
     * @return e.g. "campsite_images/owner1/1767225600000.jpg"
     */
    fun pathFor(ownerId: String, time: Long): String {
        return "campsite_images/$ownerId/$time.jpg"
    }

    /**
     * Uploads one picture and gets its download link.
     *
     * @param picture the picture the owner picked on the phone
     * @param ownerId user id of the signed in owner
     * @param onDone gets the download link, or null if the upload failed
     */
    fun upload(picture: Uri, ownerId: String, onDone: (String?) -> Unit) {
        // Use the owner's Firebase app, so Storage knows the owner is signed in
        val storage = FirebaseStorage.getInstance(FirebaseSecurity.ownerAuth.app)
        val file = storage.reference.child(pathFor(ownerId, System.currentTimeMillis()))

        // Step 1: send the picture. Step 2: ask Storage for its download link.
        file.putFile(picture)
            .continueWithTask { upload ->
                if (!upload.isSuccessful) throw upload.exception ?: Exception("Upload failed")
                file.downloadUrl
            }
            .addOnSuccessListener { link -> onDone(link.toString()) }
            .addOnFailureListener {
                println("Image upload failed: ${it.message}")
                onDone(null)
            }
    }
}
