
package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.project_halfway.ui.theme.Project_halfwayTheme

/**
 * Displays the entrant home screen and handles navigation between the home screen and Browse Campsites.
 */
class EntrantHomeScreenClass : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Project_halfwayTheme {
                var showBrowse by rememberSaveable {
                    mutableStateOf(false)
                }

                if (showBrowse) {
                    BrowseCampsitesScreenUI(
                        onBackClick = {
                            showBrowse = false
                        }
                    )
                } else {
                    EntrantHomeScreenUI(
                        onBrowseClick = {
                            showBrowse = true
                        }
                    )
                }
            }
        }
    }
}
