package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Opens one campsite's details in its own screen. It is started after the
 * entrant scans the campsite's QR code (see QrScanner).
 *
 * How to start it: use an Intent and put the campsite id in "campsiteId".
 *
 * It loads the campsite from Firestore and shows the same details card as
 * Browse Campsites, with the LotterySection so the entrant can join the
 * waiting list right away.
 *
 * Used for US 01.12.01 (and US 01.01.05, US 01.02.01 through the details card).
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: if the campsite was deleted, the screen keeps saying "Loading..." until the entrant goes back.
 */
class CampsiteDetailsActivity : ComponentActivity() {

    /**
     * Gets the campsite id from the Intent, loads the campsite and shows it.
     * The back arrow closes the screen.
     *
     * @param savedInstanceState saved state from Android (not used)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val campsiteId = intent.getStringExtra("campsiteId") ?: ""

        setContent {
            Project_halfwayTheme {
                var campsite by remember { mutableStateOf<CampsiteModel?>(null) }

                LaunchedEffect(campsiteId) {
                    if (campsiteId != "") {
                        CampsiteRepository.getById(campsiteId) { campsite = it }
                    }
                }

                val loaded = campsite
                if (loaded == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppGreen)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var text = "Loading..."
                        if (campsiteId == "") {
                            text = "No campsite in this QR code"
                        }
                        Text(text, color = Color.White)
                        Button(onClick = { finish() }, colors = whiteButtonColors(), modifier = Modifier.padding(top = 12.dp)) {
                            Text("Go back")
                        }
                    }
                } else {
                    CampsiteDetailedCard(
                        campsite = loaded,
                        isLotteryActive = true,
                        onBackClick = { finish() },
                        onJoinClick = {},
                        actionArea = { LotterySection(loaded.getId()) }
                    )
                }
            }
        }
    }
}
