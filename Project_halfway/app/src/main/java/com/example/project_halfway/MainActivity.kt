package com.example.project_halfway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.project_halfway.ui.theme.Project_halfwayTheme

class MainActivity : ComponentActivity() {
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

// For UI preview while building CampsiteSummaryCard
@Preview(showBackground = true)
@Composable
fun CampsiteSummaryPreview() {
    val campsite = CampsiteModel("Site 56A", "B Provincial Park",
        1.0, 1.0, 5, 5.5, "A campsite.", "")
    Project_halfwayTheme {
        CampsiteDescriptionCard(campsite)
    }
}