package com.example.project_halfway

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun CampsiteCardUI (currentcampsite: CampsiteModel,
                    onDelete: ()-> Unit) {
    // this is a card ui that we have created and we will use this
    //with lazy column to view them
    var openDialog by remember{mutableStateOf(false)}
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
    ) {

         Column(
             modifier = Modifier.padding(18.dp)
         ) {

            Text(
                text = currentcampsite.getCampsiteName(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(text = "Campground Name: ${currentcampsite.getCampgroundName()}",
                fontWeight = FontWeight.Bold)
            Text(text = "Location: ${currentcampsite.getCampsiteLocation()}",fontWeight = FontWeight.Bold
                )
            Text(text = "Occupancy: ${currentcampsite.getOccupancy()}",
                fontWeight = FontWeight.Bold)
            Text(text = "Description:\n${currentcampsite.getDescription()}",
                fontWeight = FontWeight.Bold)
            Text(text = "Campsite Price: ${currentcampsite.getPricePerNight()}",
                fontWeight = FontWeight.Bold)
        }
        Spacer(modifier= Modifier.size(24.dp))
        IconButton(
            onClick ={openDialog =true}
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_24),
                contentDescription = "delete Button",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        if (openDialog){
            AlertDialog(
                onDismissRequest = {openDialog = false},

                icon ={
                    Icon(imageVector = Icons.Default.Delete,
                        contentDescription = "delete icon")
                },
                title = {
                    Text (text = "Delete Item")
                },

                text = {Text (
                    text = "Are you sure, you want to delete this Campsite?"
                )},
                confirmButton ={
                    Button(onClick = {
                        openDialog = false
                        onDelete()
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray ,
                            contentColor = Color.White
                        )){
                        Text(text = "Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {openDialog = false},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.DarkGray
                        )
                    ){
                        Text(text = "Cancel")
                    }
                },
                containerColor = Color.White,
                titleContentColor = Color.DarkGray,
                textContentColor = Color.DarkGray



            )
        }
        }
    }







