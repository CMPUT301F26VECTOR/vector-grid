package com.example.project_halfway



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * The first screen of the app: choose to continue as an Entrant or as a
 * Campsite Owner. AuthGate decides what each button does.
 *
 * Outstanding issues: none known.
 *
 * @param onEntrantClick called when Entrant is pressed (signs in with the device)
 * @param onOwnerClick called when Campsite Owner is pressed (opens the owner login)
 */
@Composable
fun WelcomeScreen(
    onEntrantClick: () -> Unit,
    onOwnerClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onEntrantClick) {
            Text("Entrant")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onOwnerClick) {
            Text("Campsite Owner")
        }
    }
}