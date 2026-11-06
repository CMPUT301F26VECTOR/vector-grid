package com.example.project_halfway

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * The first screen of the app: the CamperMax logo, then a choice to continue
 * as an Entrant or as a Campsite Owner. AuthGate decides what each button does.
 *
 * **AI assistance (Varun Talwar):** OpenAI ChatGPT was used for the button click
 * handling on 2026-10-24. Prompt: "how can improve my code to have click"
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
    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CampermaxLogo(nameColor = Color.White, taglineColor = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onEntrantClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = AppGreen
            ),
            modifier = Modifier.width(240.dp).height(48.dp)
        ) {
            Text("Entrant", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onOwnerClick,
            border = BorderStroke(1.5.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            modifier = Modifier.width(240.dp).height(48.dp)
        ) {
            Text("Campsite Owner", fontWeight = FontWeight.Bold)
        }
    }
}

// Preview of the Welcome screen
@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    WelcomeScreen(onEntrantClick = {}, onOwnerClick = {})
}
