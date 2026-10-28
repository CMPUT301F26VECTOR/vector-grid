package com.example.project_halfway

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthGate(){
    var currentUser by remember{mutableStateOf(FirebaseSecurity.auth.currentUser)}

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener{auth->
            currentUser = auth.currentUser
        }
        FirebaseSecurity.auth.addAuthStateListener(authListener)

        onDispose {
            FirebaseSecurity.auth.removeAuthStateListener(authListener)
        }
    }

    if (currentUser == null){
        LoginScreenUI()
    }
    else{
        EntrantHomeScreenUI()
    }
}