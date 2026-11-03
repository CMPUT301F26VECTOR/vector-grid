package com.example.project_halfway



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun OwnerHomeScreenUI(
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, Campsite Owner!")
        Button(onClick = {}) {
            Text("My Campsites")
        }
        Button(onClick = onSignOut) {
            Text("SIGN OUT")
        }

    }
}