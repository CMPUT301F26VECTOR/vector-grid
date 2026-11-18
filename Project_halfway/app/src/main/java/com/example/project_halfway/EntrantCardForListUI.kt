package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp



@Composable
fun EntrantCardui (currentrant: EntrantProfile,
                   onDelete:()->Unit) {
    var openDialog by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()
        .padding(8.dp)){
        Column(modifier = Modifier.padding(17.dp)){
            Text(text ="Entrant Name: ${currentrant.Name.ifBlank {"Not Updated"}}")
            Text(text = "Entrant Email: ${currentrant.EmailAddress.ifBlank {"Not Updated"}}")
            Text(text = "Phone Number: ${currentrant.PhoneNumber.ifBlank { "Not Updated" }}")
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = {openDialog=true}
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Button",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
    if (openDialog){
        AlertDialog(
            onDismissRequest = {openDialog = false},

            icon ={
                Icon(imageVector = Icons.Default.Delete,
                    contentDescription = "delete icon")
            },
            title = {
                Text (text = "Delete Entrant")
            },

            text = {Text (
                text = "Are you sure, you want to delete this Entrant?"
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

//@Preview(showBackground = true)
//@Composable
//fun samplePreview () {
//    var tester = EntrantProfile(
//        Name  = "Varun",
//        EmailAddress = "vtalwar@ualberta.ca",
//        PhoneNumber = "(647) 907-1230",
//        ProfilePicture = "",
//        uid = "123"
//    )
//    EntrantCardui(tester)
//}