package com.example.project_halfway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EntrantHomeScreenUI () {

    var searchText by remember { mutableStateOf("") }

    /**
     * temp data used for testing, to be deleted later on when connected to firebase
     */
    val campsites = remember {
        listOf(
            CampsiteModel(
                "Lakeview",
                "Pine Campground",
                "Banff",
                -115.57,
                51.17,
                4,
                4.5,
                "A campsite near a lake",
                "image.jpg"
            ),
            CampsiteModel(
                "Mountain Camp",
                "Rocky Campground",
                "Jasper",
                -118.08,
                52.87,
                6,
                4.2,
                "A campsite near the mountains",
                "image.jpg"
            )
        )
    }

    val search = CampSearch()
    val results = search.searchCampsites(campsites, searchText)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrant HomeScreen",
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it},
            label = {Text("Search Campsites")},
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        )
        if (results.isEmpty()){
            Text("No campsite found")
        } else{
            for (campsite in results){
                Text(campsite.getCampsiteName())
            }
        }
        Button(
            onClick = {
                FirebaseSecurity.auth.signOut()
            }
        ) {
            Text(text = "Sign Out!")
        }
    }
}