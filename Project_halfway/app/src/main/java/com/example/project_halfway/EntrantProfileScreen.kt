package com.example.project_halfway

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The entrant's profile page, opened by the profile icon on the home screen.
 * Shows the entrant's name, email and phone number from the Firestore "Users"
 * collection and lets them update it. Uses the app's green look (the same as
 * Browse Campsites): a white title with a back arrow and a white panel.
 *
 * Used for US 01.07.03. Uses the same "Users" fields as EntrantAuthManager
 * and the home screen ("Name", "EmailAddress", "PhoneNumber").
 *
 * Outstanding issues: the profile picture can't be changed yet.
 *
 * @param onBackClick called when the back arrow is pressed
 */
@Composable
fun EntrantProfileScreen(onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val uid = FirebaseSecurity.auth.currentUser?.uid

    // load the profile when the page opens
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseSecurity.db.collection("Users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("Name") ?: ""
                        emailAddress = document.getString("EmailAddress") ?: ""
                        phoneNumber = document.getString("PhoneNumber") ?: ""
                    }
                }
                .addOnFailureListener {
                    message = "Could not load your profile"
                }
        }
    }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // back arrow + title, the same header as Browse Campsites
        GreenScreenHeader(title = "My Profile", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(HeaderGap))

        WhitePanel {
            if (isEditing) {
                ProfileEditForm(
                    name = name,
                    email = emailAddress,
                    phone = phoneNumber,
                    onSave = { newName, newEmail, newPhone ->
                        if (uid == null) {
                            message = "Please sign in first"
                        } else {
                            FirebaseSecurity.db.collection("Users").document(uid).update(
                                mapOf(
                                    "Name" to newName.trim(),
                                    "EmailAddress" to newEmail.trim(),
                                    "PhoneNumber" to newPhone.trim()
                                )
                            ).addOnSuccessListener {
                                name = newName.trim()
                                emailAddress = newEmail.trim()
                                phoneNumber = newPhone.trim()
                                isEditing = false
                                message = "Profile updated!"
                            }.addOnFailureListener {
                                message = "Could not save, try again"
                            }
                        }
                    },
                    onCancel = { isEditing = false }
                )
            } else {
                ProfileInfoLine("Name", name)
                ProfileInfoLine("Email", emailAddress)
                ProfileInfoLine("Phone Number", phoneNumber)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    colors = greenButtonColors(),
                    onClick = {
                        message = ""
                        isEditing = true
                    }
                ) {
                    Text(text = "EDIT DETAILS")
                }
            }

            if (message != "") {
                Text(text = message, color = Color(0xFF3C4043), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

// One line of the profile: a grey label above the value
@Composable
private fun ProfileInfoLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF5F6368))
        Text(
            text = value.ifBlank { "Not set" },
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = if (value.isBlank()) Color(0xFF9AA0A6) else Color(0xFF1C1B1F)
        )
    }
}

/**
 * The text boxes for editing the profile. They start filled in with the
 * current values. Save checks the input with ProfileValidator first and only
 * calls onSave if it's ok. Does not talk to Firestore, so it can be tested
 * on its own.
 *
 * @param name current name
 * @param email current email
 * @param phone current phone number
 * @param onSave called with the new name, email and phone when they are valid
 * @param onCancel called when Back is pressed
 */
@Composable
fun ProfileEditForm(
    name: String,
    email: String,
    phone: String,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var nameEdited by remember { mutableStateOf(name) }
    var emailEdited by remember { mutableStateOf(email) }
    var phoneEdited by remember { mutableStateOf(phone) }
    var errorText by remember { mutableStateOf("") }

    val boxColors = greenFieldColors()

    Column {
        OutlinedTextField(
            value = nameEdited,
            onValueChange = { nameEdited = it },
            label = { Text(text = "Name") },
            singleLine = true,
            colors = boxColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = emailEdited,
            onValueChange = { emailEdited = it },
            label = { Text(text = "Email") },
            singleLine = true,
            colors = boxColors,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phoneEdited,
            onValueChange = { phoneEdited = it },
            label = { Text(text = "Phone Number") },
            singleLine = true,
            colors = boxColors,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != "") {
            Text(text = errorText, color = Color(0xFFB3261E), modifier = Modifier.padding(top = 6.dp))
        }

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                colors = greenButtonColors(),
                onClick = {
                    val error = ProfileValidator.checkInfo(nameEdited, emailEdited, phoneEdited)
                    if (error != null) {
                        errorText = error
                    } else {
                        errorText = ""
                        onSave(nameEdited, emailEdited, phoneEdited)
                    }
                }
            ) {
                Text(text = "Save")
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, AppGreen),
                colors = greenOutlinedButtonColors(),
                onClick = onCancel
            ) {
                Text(text = "Back")
            }
        }
    }
}

/** Android Studio preview of the edit profile boxes. */
@Preview(showBackground = true)
@Composable
fun ProfileEditFormPreview() {
    Column(modifier = Modifier.background(Color.White).padding(20.dp)) {
        ProfileEditForm("Parneet Kaur", "parneet@ualberta.ca", "", { _, _, _ -> }, {})
    }
}
