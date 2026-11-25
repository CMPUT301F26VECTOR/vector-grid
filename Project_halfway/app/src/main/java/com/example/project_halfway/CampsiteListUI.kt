package com.example.project_halfway

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CampsiteListUI (toback: ()->Unit) {

    var ourCampsites by remember { mutableStateOf<List<CampsiteModel>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        CampsiteRepository.getAll {output->
            ourCampsites = output
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Spacer(modifier = Modifier.height(34.dp))
        Text(text="Registered Campsites",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.weight(1f)){items(ourCampsites)
            {
                eachcampsite ->CampsiteCardUI(currentcampsite = eachcampsite,
                    onDelete = {
                        AdminRepository.campsitedelete(
                            campsiteId = eachcampsite.getId(),
                            onDone = {success ->
                                if (success){
                                    ourCampsites = ourCampsites.filter{it.getId() !=eachcampsite.getId()}
                                    Toast.makeText(
                                        context,
                                        "Campsite deleted Successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )
                    })

            }
        }
        Button(
            onClick = toback
        ){
            Text(text = "Go Back")
        }
    }


}