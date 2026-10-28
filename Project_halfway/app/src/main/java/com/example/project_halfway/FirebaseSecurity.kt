package com.example.project_halfway

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


//code takem from OpenAI ChatGPT

object FirebaseSecurity {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val db: FirebaseFirestore by lazy{
        FirebaseFirestore.getInstance()
    }
}