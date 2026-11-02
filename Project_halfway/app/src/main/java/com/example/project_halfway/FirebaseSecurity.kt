
package com.example.project_halfway

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseOptions


object FirebaseSecurity {

    // Entrant session
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

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

    val ownerAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance(ownerApp)
    }

    val ownerDb: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(ownerApp)
    }
}