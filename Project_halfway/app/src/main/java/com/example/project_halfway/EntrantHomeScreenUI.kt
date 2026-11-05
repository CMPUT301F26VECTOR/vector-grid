
package com.example.project_halfway

import android.R
import android.content.Intent
import androidx.compose.foundation.layout.statusBarsPadding
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeGreen = Color(0xFF526B0D)

/**
 * Displays the entrant home screen.
 *
 * Shows a welcome message, profile icon and Browse Campsites card.
 * Calls onBrowseClick when the user selects the card.
 * The entrant can also edit their name, email and phone number, or delete
 * their profile (US 01.07.03).
 *
 * The profile icon opens the profile page (EntrantProfileActivity).
 *
 * Outstanding issues: none known.
 *
 * @param onExit called when SIGNOUT is pressed
 * @param onBrowseClick called when the Browse Campsites card is pressed
 */
@Composable
fun EntrantHomeScreenUI(
    onExit: () -> Unit,
    onBrowseClick: () -> Unit = {}
) {
    var emailEdited by remember { mutableStateOf("") }
    var PNedited by remember { mutableStateOf("") }
    var nameEdited by remember { mutableStateOf("") }
    var Name by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var PhoneNumber by remember { mutableStateOf("") }
    var isediting by remember {mutableStateOf(false)}
    var openDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var viewPolicyGuidelines by remember {mutableStateOf(false)}

    val uid = FirebaseSecurity.auth.currentUser?.uid
    if (openDialog){
        AlertDialog(
            onDismissRequest = {openDialog = false},

            icon ={
                Icon(imageVector = Icons.Default.Delete,
                    contentDescription = "delete icon")
            },
            title = {
                Text (text = "Delete Profile")
            },

            text = {Text (
                text = "Are you sure, you want to delete your Profile?"
            )},
            confirmButton ={
                Button(onClick = {

                    val entrant  = FirebaseSecurity.auth.currentUser
                    if (entrant!=null){
                        val uid = entrant.uid
                        //here it deletes the firebase details
                        FirebaseSecurity.db.collection("Users").document(uid).delete()
                            .addOnSuccessListener {
                                //here it deletes the anonymous authentication part
                                entrant.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Profile has been Deleted",
                                            Toast.LENGTH_SHORT).show()
                                        openDialog = false
                                        onExit()
                                    }
                            }


                    }
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
    LaunchedEffect(uid) {
        if (uid!=null){
            FirebaseSecurity.db.collection("Users").document(uid).get()
                .addOnSuccessListener { document->
                    if(document.exists()){
                        //if null then jsut dispaly ""
                        Name = document.getString("Name")?:""
                        emailAddress = document.getString("EmailAddress")?:""
                        PhoneNumber = document.getString("PhoneNumber")?:""
                    }
                }


        }
    }

    if (viewPolicyGuidelines){
        AppPolicyAndGuidelines (onBack={viewPolicyGuidelines = false})
    }
    else{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeGreen)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier  = Modifier.height(20.dp))
        // Welcome message and profile icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // US 01.07.03 - profile icon opens the profile page (EntrantProfileActivity)
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clickable {
                        context.startActivity(Intent(context, EntrantProfileActivity::class.java))
                    }
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Column(
            modifier  = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .padding(20.dp)
        ) {


            if (isediting) {
                OutlinedTextField(
                    value = nameEdited,
                    onValueChange = { nameEdited = it },
                    label = { Text(text = "Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = emailEdited,
                    onValueChange = { emailEdited = it },
                    label = { Text(text = "Email", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = PNedited,
                    onValueChange = { PNedited = it },
                    label = {
                        Text(
                            text = "PhoneNumber",
                            color = Color.White
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White
                    )


                )
                Row(
                    modifier = Modifier.padding(5.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (uid != null) {
                                FirebaseSecurity.db.collection("Users").document(uid).update(
                                    mapOf(
                                        "Name" to nameEdited,
                                        "EmailAddress" to emailEdited,
                                        "PhoneNumber" to PNedited
                                    )
                                ).addOnSuccessListener {
                                    Name = nameEdited
                                    emailAddress = emailEdited
                                    PhoneNumber = PNedited
                                    isediting = false
                                }
                            }
                        }
                    ) {
                        Text(text = "save")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {isediting=false}
                    ){
                        Text(text="Back")
                    }
                }

            } else {


                Text(
                    text = "Name: $Name",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "EmailAddress: $emailAddress",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "PhoneNumber: $PhoneNumber",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(7.dp))
                Button(
                    onClick = {
                        isediting = true
                    }
                ) {
                    Text(text = "EDIT DETAILS")
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        // Card for opening the Browse Campsites screen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clickable { onBrowseClick() },
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Browse Campsites",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Browse listings for sites with active or upcoming lotteries",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 17.sp
                )

//        }
            }

        }
        Spacer(modifier = Modifier.height(12.dp) )
        Button(onClick = {viewPolicyGuidelines = true},
            modifier = Modifier.fillMaxWidth()){
            Text(text = "App Policy and Guidelines",
                fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Card for opening My Reservations & Entries (Waitlisted tab, US 01.02.01, 01.02.02)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable {
                    context.startActivity(Intent(context, MyEntriesActivity::class.java))
                },
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Reservations & Entries",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "See the waitlists you joined",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)){

            Button(
                onClick={
                    openDialog = true

                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ){
                Text(text="Delete Profile",
                    fontSize = 20.sp)
            }
            Button(
                onClick={
                    onExit()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ){
                Text(text="Signout",
                    fontSize = 20.sp)
            }




        }



    }
    }
}


//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun EntrantHomeScreenPreview() {
//    EntrantHomeScreenUI()
//}
