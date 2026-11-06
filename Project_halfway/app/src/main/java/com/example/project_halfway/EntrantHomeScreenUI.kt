
package com.example.project_halfway

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the entrant home screen.
 *
 * Shows a welcome message, profile icon, the entrant's details and cards for
 * Browse Campsites, My Reservations & Entries and App Policy and Guidelines,
 * in the app's green look (the same as Browse Campsites).
 * Calls onBrowseClick when the user selects the card.
 * The entrant can also edit their name, email and phone number, checked with
 * ProfileValidator (US 01.07.03), or delete their profile, which also takes
 * them off every lottery waiting list (US 01.08.01).
 *
 * The profile icon opens the profile page (EntrantProfileActivity). The
 * details reload whenever the home comes back into view, so edits made on
 * that page show here straight away.
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
    // Shown under the boxes when what was typed isn't valid (US 01.07.03)
    var editError by remember { mutableStateOf("") }
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
                        // US 01.08.01: first take the entrant off every lottery waiting list
                        LotteryRepository.removeFromAllWaitingLists(uid) { removed ->
                        if (!removed) {
                            // keep the profile, so the entrant isn't left on a list with no profile
                            Toast.makeText(
                                context,
                                "Could not delete your profile. Check your connection and try again.",
                                Toast.LENGTH_SHORT).show()
                            openDialog = false
                        } else
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


                    }
                },
                    colors = dangerButtonColors()){
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = {openDialog = false},
                    colors = greenOutlinedButtonColors()
                ){
                    Text(text = "Cancel")
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.DarkGray,
            textContentColor = Color.DarkGray



        )
    }
    // Load the entrant's details from Firestore. This runs every time the home
    // screen comes back into view (ON_RESUME), so changes saved on the My Profile
    // page show here straight away when the entrant comes back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, uid) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && uid != null) {
                FirebaseSecurity.db.collection("Users").document(uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // if a value is missing, show it as blank ("Not set")
                            Name = document.getString("Name") ?: ""
                            emailAddress = document.getString("EmailAddress") ?: ""
                            PhoneNumber = document.getString("PhoneNumber") ?: ""
                        }
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (viewPolicyGuidelines){
        AppPolicyAndGuidelines (onBack={viewPolicyGuidelines = false})
    }
    else{
    LightStatusBarIcons()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    // Everything but the bottom buttons scrolls, so small screens still fit
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier  = Modifier.height(12.dp))
        // Welcome message and profile icon
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
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
        Spacer(modifier = Modifier.height(16.dp))

        // The entrant's details, in a white panel like the rest of the app
        WhitePanel {
            if (isediting) {
                OutlinedTextField(
                    value = nameEdited,
                    onValueChange = { nameEdited = it },
                    label = { Text(text = "Name") },
                    singleLine = true,
                    colors = greenFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emailEdited,
                    onValueChange = { emailEdited = it },
                    label = { Text(text = "Email") },
                    singleLine = true,
                    colors = greenFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = PNedited,
                    onValueChange = { PNedited = it },
                    label = { Text(text = "PhoneNumber") },
                    singleLine = true,
                    colors = greenFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (editError.isNotEmpty()) {
                    Text(
                        text = editError,
                        color = Color(0xFFB3261E),
                        modifier = Modifier.padding(top = 6.dp).testTag("home_edit_error")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = greenButtonColors(),
                        onClick = {
                            // US 01.07.03: the same checks as the My Profile page
                            val error = ProfileValidator.checkInfo(nameEdited, emailEdited, PNedited)
                            if (error != null) {
                                editError = error
                            } else if (uid != null) {
                                editError = ""
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
                        Text(text = "Save")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, AppGreen),
                        colors = greenOutlinedButtonColors(),
                        onClick = {
                            editError = ""
                            isediting = false
                        }
                    ){
                        Text(text="Back")
                    }
                }

            } else {
                DetailLine("Name", Name)
                DetailLine("Email", emailAddress)
                DetailLine("Phone", PhoneNumber)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    colors = greenButtonColors(),
                    onClick = {
                        // start the boxes filled in with the current details
                        nameEdited = Name
                        emailEdited = emailAddress
                        PNedited = PhoneNumber
                        editError = ""
                        isediting = true
                    }
                ) {
                    Text(text = "EDIT DETAILS")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Card for opening the Browse Campsites screen
        HomeCard(
            icon = Icons.Outlined.Search,
            title = "Browse Campsites",
            subtitle = "Browse listings for sites with active or upcoming lotteries",
            onClick = onBrowseClick
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Card for opening My Reservations & Entries (Waitlisted tab, US 01.02.01, 01.02.02)
        HomeCard(
            icon = Icons.Outlined.EventNote,
            title = "My Reservations & Entries",
            subtitle = "See the waitlists you joined",
            onClick = { context.startActivity(Intent(context, MyEntriesActivity::class.java)) }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Card for the app's policy and lottery guidelines
        HomeCard(
            icon = Icons.Outlined.Info,
            title = "App Policy and Guidelines",
            subtitle = "How lotteries, waitlists and reservations work",
            onClick = { viewPolicyGuidelines = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)){

            Button(
                onClick={
                    openDialog = true
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = dangerButtonColors()
            ){
                Text(text="Delete Profile", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick={
                    onExit()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = whiteButtonColors()
            ){
                Text(text="Signout", fontWeight = FontWeight.Bold)
            }
        }
    }
    }
}

// One line of the entrant's details: a grey label above the value
@Composable
private fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF5F6368))
        Text(
            text = value.ifBlank { "Not set" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (value.isBlank()) Color(0xFF9AA0A6) else Color(0xFF1C1B1F)
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun EntrantHomeScreenPreview() {
//    EntrantHomeScreenUI()
//}
