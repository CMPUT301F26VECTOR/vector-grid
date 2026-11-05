
package com.example.project_halfway

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseOptions


/**
 * The app's Firebase connections, shared by every screen.
 *
 * There are two separate logins so an entrant and a campsite owner can be
 * signed in at the same time on one phone:
 * - auth / db: the entrant's login (device sign-in) and Firestore connection
 * - ownerAuth / ownerDb: the campsite owner's login (email and password) and
 *   Firestore connection, using a second Firebase app named "OwnerApp"
 *
 * Outstanding issues: none known.
 */
object FirebaseSecurity {

    // Entrant session
    /** The entrant's Firebase login. */
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    /** Firestore, signed in as the entrant. */
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Separate owner session
    private val ownerApp: FirebaseApp by lazy {
        val defaultApp = FirebaseApp.getInstance()

        FirebaseApp.getApps(defaultApp.applicationContext)
            .find { it.name == "OwnerApp" }
            ?: FirebaseApp.initializeApp(
                defaultApp.applicationContext,
                defaultApp.options,
                "OwnerApp"
            )!!
    }

    /** The campsite owner's Firebase login (email and password). */
    val ownerAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance(ownerApp)
    }

    /** Firestore, signed in as the campsite owner. */
    val ownerDb: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(ownerApp)
    }
}