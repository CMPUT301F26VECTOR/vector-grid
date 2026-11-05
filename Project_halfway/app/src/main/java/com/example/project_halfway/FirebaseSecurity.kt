
package com.example.project_halfway

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseOptions

/**
 * This is the most important object in the entire
 * App as it contains the firebase database object.
 * it is the object which stores all the details related
 * to campsite owner, entrant and even Admin.
 * For entrant and owner we have created two different sessions
 * so that entrant can have an anonymous account while the
 * campsite owner has to login or create an account.
 * for admin a similar approach will be used
 */



// this code has been made with the help of OpenAI ChatGPT to how to make the syntax and other context part. although,
//it was my idea to create the two instances of same db so that entrant and campsite owner can
// different sessions and admin will also be added for next part.2026-10-21
// the Ai was presented with my code that I wrote and it helped me modify it.
//"can you provide me syntax and of how to have two differnt sessions with what i have implemented
//so far for this project and how implement this context"
object FirebaseSecurity {
    // this code has been made with the help of OpenAI ChatGPT to how to make the syntax and other context part. although,
//it was my idea to create the two instances of same db so that entrant and campsite owner can
// different sessions and admin will also be added for next part.2026-10-21
// the Ai was presented with my code that I wrote and it helped me modify it.
//"can you provide me syntax and of how to have two differnt sessions with what i have implemented
//so far for this project and how implement this context"

    //entrant session
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

   //owner session
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