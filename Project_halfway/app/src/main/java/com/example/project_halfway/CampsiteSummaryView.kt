package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class CampsiteSummaryView : ViewObserver<CampsiteModel> {

    override fun update(model: CampsiteModel) {
        TODO("Not yet implemented")
    }

    @Composable
    fun CampsiteDescriptionCard() {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier,
            onClick = { }  // Needs to link to the full view
        ) {
            Column() {


            }

        }

    }

}