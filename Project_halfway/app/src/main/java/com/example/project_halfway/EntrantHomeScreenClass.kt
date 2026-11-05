package com.example.project_halfway

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Screen opened by "Create Account" on the owner login screen. It shows
 * SignUpScreen, and once the owner's account is created it shows the
 * owner's screens (OwnerScreens). Signing out goes back to MainActivity.
 *
 * **AI assistance (Varun Talwar):** connecting the sign-up screen to the next
 * home screen was implemented with OpenAI ChatGPT's help on 2026-10-25.
 * Prompt: "can help me to connect my code to next home screen"
 *
 * Outstanding issues: despite its name, this is the campsite owner sign-up
 * flow, not the entrant home screen (the entrant home screen is shown by
 * AuthGate).
 */
class EntrantHomeScreenClass : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showOwnerHome by rememberSaveable {
                mutableStateOf(false)
            }

            Project_halfwayTheme {
                // AI-assisted: see the AI assistance note in this class's KDoc
                if (showOwnerHome) {
                    OwnerScreens(
                        // the new owner's id, so their campsites are saved and loaded under it
                        ownerId = FirebaseSecurity.ownerAuth.currentUser?.uid ?: "",
                        onSignOut = {
                            FirebaseSecurity.ownerAuth.signOut()

                            val intent = Intent(
                                this@EntrantHomeScreenClass,
                                MainActivity::class.java
                            )

                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                        }
                    )
                }
                else {
                    SignUpScreen(
                        onSignUpSuccess = {
                            showOwnerHome = true
                        }
                    )
                }
            }
        }
    }
}
