package com.example.project_halfway

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * The entrant's App Policy and Guidelines screen (US 01.11.01): the account,
 * lottery and reservation rules, opened from the entrant home. Uses the app's
 * green look (the same as Browse Campsites).
 *
 * Outstanding issues: the rules are short placeholders, not the final policy.
 *
 * @param onBack called when the back arrow, Go back or the phone's back
 * button is pressed, to return to the entrant home
 */
@Composable
fun AppPolicyAndGuidelines(onBack: () -> Unit) {
    // The phone's back button does the same as Go back
    BackHandler(onBack = onBack)
    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        GreenScreenHeader(title = "App Policy and Guidelines", onBackClick = onBack)
        Spacer(modifier = Modifier.height(HeaderGap))

        WhitePanel(modifier = Modifier.weight(1f), scrollable = true) {
            PanelLabel("1. Account Policy and Guidelines", first = true)
            Text(text = "The Profile Details on the App should be updated", color = Color(0xFF3C4043))

            PanelLabel("2. Lottery Policy and Guidelines")
            Text(text = "If you have joined a lottery the reservation is not guranteed", color = Color(0xFF3C4043))

            PanelLabel("3. Reservation Guidelines")
            Text("The reservation Rules must be followed", color = Color(0xFF3C4043))
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { onBack() },
            colors = whiteButtonColors(),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(text = "Go back", fontWeight = FontWeight.Bold)
        }
    }
}

// Preview of the policy screen
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppPolicyAndGuidelinesPreview() {
    AppPolicyAndGuidelines(onBack = {})
}
