package com.example.project_halfway

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat.enableEdgeToEdge
import com.example.project_halfway.ui.theme.Project_halfwayTheme
import com.google.firebase.messaging.FirebaseMessaging

/**
 * The screen the app opens on. It applies the app theme and shows AuthGate,
 * which picks the welcome, entrant or owner screens.
 *
 * Outstanding issues: none known.
 */
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_halfwayTheme {
                AuthGate()
                    //LoginScreenUI()
//                SignUpScreen ()
            }
        }
    }
}
/** Android Studio preview of CampsiteDescriptionCard with a sample campsite. */
// For UI preview while building CampsiteSummaryCard
@Preview(showBackground = true)
@Composable
fun CampsiteSummaryPreview() {
    val campsite = CampsiteModel("Site 56A", "B Provincial Park",
        "Rockyview County, AB",1.0, 1.0, 5, 0.0,
        "A campsite.", "")

    Project_halfwayTheme {
        CampsiteDescriptionCard(campsite, {})
    }
}

/** Android Studio preview of CampsiteDetailedCard with a sample campsite. */
// UI preview for CampsiteDetailedCard
@Preview(showBackground = true)
@Composable
fun CampsiteDetailedPreview() {
    val campsite = CampsiteModel("Site 56A", "B Provincial Park",
        "Rockyview County, AB",1.0, 1.0, 5, 5.5, "A campsite.", "",
        "","","Tent site",mutableListOf("Power","Non-potable water"),43.00)
    Project_halfwayTheme {
        CampsiteDetailedCard(campsite, true,{},{})
    }
}
/** Android Studio preview of CampsiteCalender with a sample Calender. */
@Preview(showBackground = true)
@Composable
private fun CampsiteCalendarPreview() {
    Project_halfwayTheme {
        CampsiteCalendar(
            availableDateRanges = listOf(
                mapOf("start" to "2025-09-09", "end" to "2025-09-13")
            )
        )
    }
}