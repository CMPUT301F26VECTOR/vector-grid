package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Opens the entrant's profile page (EntrantProfileScreen). Started by the
 * profile icon on the entrant home screen. The back arrow closes it and goes
 * back to the home screen.
 *
 * Used for US 01.07.03.
 *
 * Outstanding issues: none known.
 */
class EntrantProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_halfwayTheme {
                EntrantProfileScreen(onBackClick = { finish() })
            }
        }
    }
}
