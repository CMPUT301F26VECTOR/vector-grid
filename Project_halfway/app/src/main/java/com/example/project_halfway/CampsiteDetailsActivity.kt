package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Opens the campsite details screen for one campsite in its own activity.
 *
 * How to start it: use an Intent and put "campsiteId" and "campsiteName" as extras.
 *
 * Not used yet: the Browse screen shows the details screen itself. This is kept
 * so a campsite can be opened from somewhere else later, like scanning its QR code.
 *
 * Used for US 01.01.05 and US 01.01.06.
 *
 * Outstanding issues:
 * - It is not listed in AndroidManifest.xml yet, so it has to be added there before it is used.
 * - Only the id and name are filled in. It should load the whole campsite with CampsiteRepository.getById.
 * - The Join button does nothing here; it should show LotterySection like Browse does.
 *
 * **AI assistance (Parneet Kaur):** part of this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */

class CampsiteDetailsActivity : ComponentActivity() {

      /**
     * Reads the campsite's id and name from the Intent and shows its details screen.
     * The back arrow closes the activity.
     *
     * @param savedInstanceState Android's saved state, not used here
     */
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val campsiteId = intent.getStringExtra("campsiteId") ?: ""
        val campsiteName = intent.getStringExtra("campsiteName") ?: "Campsite"

        val campsite = CampsiteModel(
            campsiteName, "", "", 0.0, 0.0, 0, 0.0, "", "",
            id = campsiteId
        )

        setContent {
            Project_halfwayTheme {
                CampsiteDetailedCard(campsite, isLotteryActive = true, onBackClick = { finish() }, onJoinClick = {})
            }
        }
    }
}
