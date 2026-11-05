package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.apps.card.v1.Columns


@Composable
fun AppPolicyAndGuidelines(onBack:() ->Unit) {

    Column(modifier = Modifier.fillMaxSize()
        .padding(40.dp) ) {

        Text(text ="App Policy and Guidelines",
            fontSize = 24.sp)

        Spacer(modifier = Modifier.height(18.dp))
        Text(text="1. Account Policy and Guidelines")
        Text(text = "The Profile Details on the App should be updated")

        Spacer(modifier = Modifier.height(18.dp))
        Text(text="2. Lottery Policy and Guidelines")
        Text(text = "If you have joined a lottery the reservation is not guranteed")

        Spacer(modifier = Modifier.height(18.dp))
        Text("3. Reservation Guidelines")
        Text("The reservation Rules must be followed")

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = {onBack()}
        ){
            Text(text = "Go back")
        }

    }
}