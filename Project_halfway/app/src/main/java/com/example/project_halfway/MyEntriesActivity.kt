package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Opens the "My Reservations & Entries" screen (MyEntriesScreen). Started by
 * the "My Reservations & Entries" card on the entrant home screen. The back
 * arrow closes it.
 *
 * Used for US 01.02.01 and US 01.02.02.
 *
 * Outstanding issues: none known.
 */
class MyEntriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_halfwayTheme {
                MyEntriesScreen(onBackClick = { finish() })
            }
        }
    }
}
