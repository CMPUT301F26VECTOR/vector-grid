package com.example.project_halfway
import com.google.firebase.firestore.FirebaseFirestore


    /**
     * Checks that the app can reach Firestore by writing a small test document
     * to "connectionTest/testDocument", then prints whether it worked.
     * For development only.
     *
     * Outstanding issues: should be removed (or kept out of release builds)
     * once the team no longer needs it.
     */
    fun testFirebaseConnection() {
        val db = FirebaseFirestore.getInstance()

        val testData = mapOf(
            "message" to "Firebase connection works!",
            "test" to true
        )

        db.collection("connectionTest")
            .document("testDocument")
            .set(testData)
            .addOnSuccessListener {
                println("Successfully connected to Firestore!")
            }
            .addOnFailureListener { exception ->
                println("Firebase test failed: ${exception.message}")
            }
    }
