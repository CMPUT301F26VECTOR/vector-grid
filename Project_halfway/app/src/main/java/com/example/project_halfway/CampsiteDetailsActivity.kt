package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme

// Opens the campsite details screen for one campsite.
// Start it with an Intent and put "campsiteId" and "campsiteName" as extras.
//
// Outstanding issues: only the id and name are passed in for now. Once
// CampsiteRepository is on main we can load the whole campsite with getById.

class CampsiteDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val campsiteId = intent.getStringExtra("campsiteId") ?: ""
        val campsiteName = intent.getStringExtra("campsiteName") ?: "Campsite"

        // only name and id are real here, the rest are blanks for now
        val campsite = CampsiteModel(
            campsiteName, "", "", 0.0, 0.0, 0, 0.0, "", "",
            id = campsiteId
        )

        setContent {
            Project_halfwayTheme {
                CampsiteDetailedCard(campsite)
            }
        }
    }
}
