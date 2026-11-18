package com.example.project_halfway

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerProfileListUI(toback:()->Unit) {

    var ourOwners by remember { mutableStateOf<List<Owner>>(emptyList()) }
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val mylistener = AdminRepository.retrieveAllOwners { owners ->
            ourOwners = owners
        }
        onDispose {
            mylistener.remove()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(34.dp))
        Text(text = "Campsite Owners",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)
        LazyColumn (modifier = Modifier.weight(1f)){
            items(ourOwners) { owner ->
                OwnerCardUIlIST(currowner = owner,
                    onDelete= {
                        AdminRepository.deleteUser(
                            uid = owner.uid,
                            onDone = {Success ->
                                if (Success){
                                    Toast.makeText(
                                        context,
                                        "Owner Deleted Successfully",
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
        ) {
            Text(text = "Go Back")
        }

    }
}

