package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The campsite owner's home screen: a welcome line and cards for the owner's
 * screens, in the app's green look (the same as Browse Campsites and the
 * entrant home). OwnerScreens decides what each card opens.
 *
 * Used for US 02.01.01 (Register a Campsite) and US 02.03.01 (My Campsites).
 *
 * Outstanding issues: the mockup's owner home also lists the owner's lotteries;
 * that part is not built yet. A replacement should keep these callbacks so the
 * cards still open the right screens.
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
    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Welcome, Campsite Owner!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.height(20.dp))

        HomeCard(
            icon = Icons.Filled.AddLocationAlt,
            title = "Register a Campsite",
            subtitle = "List a new campsite and drop its pin on the map",
            onClick = onRegisterClick
        )
        Spacer(Modifier.height(12.dp))
        HomeCard(
            icon = Icons.Filled.Cabin,
            title = "My Campsites",
            subtitle = "See your campsites, their lotteries and waitlists",
            onClick = onMyCampsitesClick
        )

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onSignOut,
            colors = whiteButtonColors(),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("SIGN OUT", fontWeight = FontWeight.Bold)
        }
    }
}

// Preview of the owner home
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OwnerHomeScreenPreview() {
    OwnerHomeScreenUI(onSignOut = {})
}
