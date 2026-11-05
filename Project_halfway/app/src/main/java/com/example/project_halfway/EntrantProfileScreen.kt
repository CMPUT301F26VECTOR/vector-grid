package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProfileGreen = Color(0xFF566C11)

/**
 * The entrant's profile page, opened by the profile icon on the home screen.
 * Shows the entrant's name, email and phone number from the Firestore "Users"
 * collection and lets them update it.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileGreen)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // back arrow + title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp)
                .clickable { onBackClick() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Text(
                text = "My Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .padding(20.dp)
        ) {
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
                ProfileInfoLine("Name: $name")
                ProfileInfoLine("Email: $emailAddress")
                ProfileInfoLine("Phone Number: $phoneNumber")
                Button(
                    onClick = {
                        message = ""
                        isEditing = true
                    }
                ) {
                    Text(text = "EDIT DETAILS")
                }
            }

            if (message != "") {
                Text(text = message, color = Color.White, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

// one line of white profile text
@Composable
private fun ProfileInfoLine(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontSize = 20.sp,
        modifier = Modifier.padding(bottom = 7.dp)
    )
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

    val boxColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White
    )

    Column {
        OutlinedTextField(
            value = nameEdited,
            onValueChange = { nameEdited = it },
            label = { Text(text = "Name", color = Color.White) },
            colors = boxColors
        )
        OutlinedTextField(
            value = emailEdited,
            onValueChange = { emailEdited = it },
            label = { Text(text = "Email", color = Color.White) },
            colors = boxColors
        )
        OutlinedTextField(
            value = phoneEdited,
            onValueChange = { phoneEdited = it },
            label = { Text(text = "Phone Number", color = Color.White) },
            colors = boxColors
        )

        if (errorText != "") {
            Text(text = errorText, color = Color(0xFFFF8A80), modifier = Modifier.padding(top = 6.dp))
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
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
            Button(
                modifier = Modifier.weight(1f),
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
    Column(modifier = Modifier.background(Color.Black).padding(20.dp)) {
        ProfileEditForm("Parneet Kaur", "parneet@ualberta.ca", "", { _, _, _ -> }, {})
    }
}
