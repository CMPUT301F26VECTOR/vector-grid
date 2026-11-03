package com.example.project_halfway

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AuthGate() {

    var currentUser by remember {
        mutableStateOf(FirebaseSecurity.auth.currentUser)
    }

    var screen by remember {
        mutableStateOf("welcome")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }

        FirebaseSecurity.auth.addAuthStateListener(listener)

        onDispose {
            FirebaseSecurity.auth.removeAuthStateListener(listener)
        }
    }

    when (screen) {

        "welcome" -> WelcomeScreen(
            onEntrantClick = {
                errorMessage = ""
                isLoading = true

                EntrantAuthManager.signIn(
                    onSuccess = { user ->
                        Log.d("AUTH_TEST", "SUCCESS UID: ${user.uid}")
                        isLoading = false
                        screen = "entrant"
                    },
                    onFailure = { error ->
                        Log.e("AUTH_TEST", "FAILED: $error")
                        isLoading = false
                        errorMessage = error
                    }
                )
            }
            ,
            onOwnerClick = {
                errorMessage = ""
                screen = "ownerLogin"
            }
        )


        "entrant" -> {
            var showBrowse by rememberSaveable {
                mutableStateOf(false)
            }

            if (showBrowse) {
                BrowseCampsitesScreenUI(
                    onBackClick = {
                        showBrowse = false
                    }
                )
            } else {
                EntrantHomeScreenUI(
                    onExit = {
                        screen = "welcome"
                    },
                    onBrowseClick = {
                        showBrowse = true
                    }
                )
            }
        }


        "ownerLogin" -> LoginScreenUI(
            onLoginSuccess = {
                screen = "owner"
            }
        )

        "owner" -> OwnerHomeScreenUI(
            onSignOut = {
                FirebaseSecurity.ownerAuth.signOut()
                screen = "welcome"
            }
        )
    }

    if (isLoading) {
        Text("Loading...")
    }

    if (errorMessage.isNotEmpty()) {
        Text(errorMessage)
    }
}
