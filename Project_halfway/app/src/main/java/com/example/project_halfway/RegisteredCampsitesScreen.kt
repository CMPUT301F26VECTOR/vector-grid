package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Registered Campsites screen for the campsite owner (US 02.03.01).
 * Loads the owner's campsites from Firestore with CampsiteRepository and
 * shows them with RegisteredCampsitesList.
 *
 * Outstanding issues: there is no loading message, so the screen looks empty
 * until Firestore answers. The + button from the mockup is not built yet.
 * The ownerId has to be passed in by the screen that opens this one.
 *
 * @param ownerId user id of the signed in campsite owner
 */
@Composable
fun RegisteredCampsitesScreen(ownerId: String = "") {

    // The campsites we have loaded so far (empty until Firestore answers)
    var campsites by remember { mutableStateOf(listOf<CampsiteModel>()) }

    // Runs once when the screen opens: ask the repository for this owner's campsites
    LaunchedEffect(ownerId) {
        CampsiteRepository.getByOwner(ownerId) { result ->
            campsites = result
        }
    }

    RegisteredCampsitesList(campsites)
}

/**
 * Draws the title and one card for each campsite in the list.
 * If the list is empty it shows a short message instead.
 * It does not talk to Firestore, so it can be tested on its own.
 *
 * @param campsites the owner's campsites to show
 */
@Composable
fun RegisteredCampsitesList(campsites: List<CampsiteModel>) {
    Column(modifier = Modifier.padding(16.dp)) {

        Text("Registered Campsites")

        if (campsites.isEmpty()) {
            Text(
                "You have no registered campsites yet.",
                modifier = Modifier.testTag("empty_message")
            )
        } else {
            LazyColumn(modifier = Modifier.testTag("campsite_list")) {
                items(campsites) { campsite ->
                    CampsiteDescriptionCard(campsite)
                }
            }
        }
    }
}