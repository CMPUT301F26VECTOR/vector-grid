package com.example.project_halfway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

/**
 * Home screen for the campsite owner (owner Profile).
 * The owner can open My Campsites (US 02.03.01), open the Register Campsite
 * form (US 02.01.01) or sign out. The signed in owner's id is passed on to
 * both screens so campsites are saved and loaded for the right owner.
 *  not included yet: the owner's name is not shown yet. The Lottery Summary
 * and Create Lottery buttons from the mockup are not added here yet.
 *
 * @param onSignOut called when the owner presses SIGN OUT
 */
@Composable
fun OwnerHomeScreenUI(
    onSignOut: () -> Unit
) {
    // Which screen is showing: "home", "register" or "list"
    var currentScreen by remember { mutableStateOf("home") }

    // The signed in owner's id (blank if nobody is signed in)
    val ownerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    if (currentScreen == "register") {
        Column(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { currentScreen = "home" },
                modifier = Modifier.padding(16.dp).testTag("back_button")
            ) {
                Text("Back")
            }
            Box(modifier = Modifier.weight(1f)) {
                RegisterCampsiteScreen(ownerId)
            }
        }
    } else if (currentScreen == "list") {
        Column(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { currentScreen = "home" },
                modifier = Modifier.padding(16.dp).testTag("back_button")
            ) {
                Text("Back")
            }
            Box(modifier = Modifier.weight(1f)) {
                RegisteredCampsitesScreen(ownerId)
            }
        }
    } else {
        // The home screen itself
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome, Campsite Owner!")

            Button(
                onClick = { currentScreen = "list" },
                modifier = Modifier.testTag("my_campsites_button")
            ) {
                Text("My Campsites")
            }

            Button(
                onClick = { currentScreen = "register" },
                modifier = Modifier.testTag("register_campsite_button")
            ) {
                Text("Register Campsite")
            }

            Button(onClick = onSignOut) {
                Text("SIGN OUT")
            }
        }
    }
}