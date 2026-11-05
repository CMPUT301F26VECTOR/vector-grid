package com.example.project_halfway



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The campsite owner's home screen: a welcome line and buttons for the
 * owner's screens. OwnerScreens decides what each button opens.
 *
 * Used for US 02.01.01 (Register a Campsite) and US 02.03.01 (My Campsites).
 *
 * Outstanding issues: this is a placeholder layout that Varun is replacing
 * with the mockup's owner screen. A replacement should keep these callbacks so
 * the buttons still open the right screens.
 *
 * @param onSignOut called when Sign Out is pressed
 * @param onRegisterClick called when Register a Campsite is pressed
 * @param onMyCampsitesClick called when My Campsites is pressed
 */
@Composable
fun OwnerHomeScreenUI(
    onSignOut: () -> Unit,
    onRegisterClick: () -> Unit = {},
    onMyCampsitesClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, Campsite Owner!")
        Button(onClick = onRegisterClick) {
            Text("Register a Campsite")
        }
        Button(onClick = onMyCampsitesClick) {
            Text("My Campsites")
        }
        Button(onClick = onSignOut) {
            Text("SIGN OUT")
        }

    }
}