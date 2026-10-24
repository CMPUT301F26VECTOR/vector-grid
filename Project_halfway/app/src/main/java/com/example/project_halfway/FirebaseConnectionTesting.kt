package com.example.project_halfway
import com.google.firebase.firestore.FirebaseFirestore


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
