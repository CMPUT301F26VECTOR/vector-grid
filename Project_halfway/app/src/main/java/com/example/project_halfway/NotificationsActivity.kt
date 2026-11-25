package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Opens the Notifications screen (NotificationsScreen). It is started by the
 * "Notifications" card on the entrant home screen, or when a pop-up is
 * tapped. The back arrow closes it.
 *
 * Used for US 01.03.01, US 01.03.02 and US 01.10.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: none known.
 */
class NotificationsActivity : ComponentActivity() {

    /**
     * Shows the Notifications screen.
     *
     * @param savedInstanceState saved state from Android (not used)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_halfwayTheme {
                NotificationsScreen(onBackClick = { finish() })
            }
        }
    }
}
