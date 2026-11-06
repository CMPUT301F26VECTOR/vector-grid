package com.example.project_halfway

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.auth.FirebaseAuth

/**
 * Decides which screen to show, starting with WelcomeScreen:
 * - Entrant: signs in with the device (EntrantAuthManager), then shows the
 *   entrant home screen, which can open Browse Campsites
 * - Campsite Owner: shows LoginScreenUI, then the owner's screens
 *   (OwnerScreens, starting at the owner home) after login
 *
 * Used for US 01.09.01 and the owner login.
 *
 * **AI assistance (Varun Talwar):** made in collaboration with OpenAI ChatGPT on
 * 2026-10-18. Having one file that navigates to the entrant or the campsite owner
 * screens was Varun's idea, and ChatGPT helped with the implementation: it was shown
 * his code and helped him modify it. Prompt: "How can I design an Authentication
 * page where we can navigate using two buttons with entrant and campsite owners give
 * me a roadmap"
 *
 * Outstanding issues: which screen is open is kept in memory only, so the app
 * goes back to the welcome screen when it is restarted. currentUser is tracked
 * but not used to skip the welcome screen yet.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AuthGate() {


    var currentUser by remember {  mutableStateOf(FirebaseSecurity.auth.currentUser)}
    var screen by remember {mutableStateOf("welcome")}
    var errorMessage by remember { mutableStateOf("")}
    var isLoading by remember {mutableStateOf(false)}

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseSecurity.auth.addAuthStateListener(listener)
        onDispose {
            FirebaseSecurity.auth.removeAuthStateListener(listener)
        }
    }

    // The screen is very simple design where first the screen goes to welcome
    //where it has both entrant and owner buttons and based on those buttons clicked
    // the screen goes to entrant adn signs in an entrant anonymously or goes to owner and
    //asks for a login password or create an account
    when (screen) {
        // this is the welcome screen where two button are available entrant and owner
        "welcome" -> WelcomeScreen(
            onEntrantClick = {
                errorMessage = ""
                isLoading = true
//now once the entrant is clicked entrant auth manager has a fucntion which sign it in
                //without password and email
                EntrantAuthManager.signIn(
                    onSuccess = { user ->
                        Log.d("AUTH_TEST", "SUCCESS UID: ${user.uid}")
                        isLoading = false
                        //here the our scrren variable is entrant
                        screen = "entrant"
                    },
                    //if it doesn't work it give us the error message
                    onFailure = { error ->
                        Log.e("AUTH_TEST", "FAILED: $error")
                        isLoading = false
                        errorMessage = error
                    }
                )
            }
            ,
            // the second button on the screen where it goes to owner login which are defined below
            onOwnerClick = {
                errorMessage = ""
                screen = "ownerLogin"
            }
        )


// here are the two scrren that are defined here where if it entrant is cliked it goes
        //entrant home screen and it also have a back button so it can come back to the welcome screen
        "entrant" -> {
            var showBrowse by rememberSaveable { mutableStateOf(false)}
            var showNotifications by rememberSaveable { mutableStateOf(false)}

            if (showBrowse) {
                BrowseCampsitesScreenUI(
                    onBackClick = {
                        showBrowse = false
                    }
                )
            }
            else if (showNotifications) {
                EntrantNotificationsScreen(
                    onBack = { showNotifications = false}
                ) // @TODO
            }

            else {
                EntrantHomeScreenUI(
                    onExit = {
                        screen = "welcome"
                    },
                    onBrowseClick = {
                        showBrowse = true
                    },
                )
            }
        }

// this part is for owner login screen where if campsite owner is clicked it will go to campsite owner
        //home screen.
        "ownerLogin" -> LoginScreenUI(
            onLoginSuccess = {
                screen = "owner"
            },
            onBackClick = {
                screen = "welcome"
            }
        )
        // the owner also has a signout button so it can exit the campsite account
        "owner" -> OwnerScreens(
            // the signed in owner's id, so their campsites are saved and loaded under it
            ownerId = FirebaseSecurity.ownerAuth.currentUser?.uid ?: "",
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
