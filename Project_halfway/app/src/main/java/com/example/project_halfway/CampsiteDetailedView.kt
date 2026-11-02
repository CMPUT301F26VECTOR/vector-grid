package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


fun update(model: CampsiteModel) {
    TODO("Not yet implemented")
}

// Detailed campsite view
@Composable
fun CampsiteDetailedCard(campsite : CampsiteModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 40.dp)) {
        Text(campsite.getCampsiteName(), style = MaterialTheme.typography.headlineMedium)

        // TODO US 01.01.05: rest of the campsite details go here

        // US 01.01.06 - join the lottery from the details screen
        LotterySection(campsite.id)
    }
}

