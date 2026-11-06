package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The campsite owner sign-up screen: the CamperMax logo, then a white panel
 * with first name, last name, email and password, on the app's green (the
 * same look as Browse Campsites). Creates the owner's account through FirebaseSecurity.ownerAuth
 * and saves an Owner profile (role "owner") in the Firestore "Users"
 * collection.
 *
 * **AI assistance (Varun Talwar):**
 * - OpenAI ChatGPT was used for the title text style. (On 2026-11-05 that
 *   title was replaced by the CamperMax logo.)
 * - Creating the owner's Firebase account and its listeners was implemented with
 *   OpenAI ChatGPT on 2026-10-24. Prompt: "Provide the firebase syntax for
 *   creating a user and aslo how to add listeners"
 *
 * Outstanding issues: the boxes are not checked before sending (e.g. blank
 * names), and if saving the profile to Firestore fails no message is shown.
 *
 * @param onSignUpSuccess called once the account and profile are saved
 * @param onBackClick called when the back arrow is pressed; null shows no arrow
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreen (
    onSignUpSuccess: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {

    var firstName by remember{ mutableStateOf("") }
    var lastName by remember{mutableStateOf("")}
    var Email by remember{mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var createMessage by remember{mutableStateOf("")}

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            GreenScreenHeader(title = "Owner Sign Up", onBackClick = onBackClick)
        }

        Spacer(modifier = Modifier.height(16.dp))
        CampermaxLogo(
            tileSize = 80.dp,
            nameSize = 22,
            nameColor = Color.White,
            taglineColor = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        WhitePanel {
            Text(
                text = "SIGN UP",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = firstName,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.outline_drive_file_rename_24), contentDescription = "Name image")
                },
                onValueChange = { firstName = it },
                label = { Text(text = "First Name") },
                placeholder = { Text(text = "ABC XYZ") },
                singleLine = true,
                colors = greenFieldColors()
            )
            PanelGap()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = lastName,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.rounded_contextual_token_24), contentDescription = "Name image")
                },
                onValueChange = { lastName = it },
                label = { Text(text = "Last Name") },
                placeholder = { Text(text = "XYZ ABC") },
                singleLine = true,
                colors = greenFieldColors()
            )
            PanelGap()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = Email,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email image")
                },
                onValueChange = { Email = it },
                label = { Text(text = "Email") },
                placeholder = { Text(text = "ABC@gmail.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = greenFieldColors()
            )
            PanelGap()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "lock image")
                },
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                placeholder = { Text(text = "************") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = greenFieldColors()
            )
            PanelGap()

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {

                    val ownerFirstName =firstName.trim()
                    val ownerLastName = lastName.trim()
                    val ownerEmail = Email.trim()

                    // AI-assisted: see the AI assistance note in this function's KDoc
                    FirebaseSecurity.ownerAuth.createUserWithEmailAndPassword(Email.trim(),password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                var user = FirebaseSecurity.ownerAuth.currentUser
                                if (user!=null) {

                                    val owner  = Owner(
                                        Name = "$ownerFirstName $ownerLastName",
                                        EmailAddress = ownerEmail,
                                        ProfilePictureURL = "",
                                        uid =user.uid
                                    )

                                    val userData = hashMapOf(
                                        "Name" to owner.Name,
                                        "EmailAddress" to owner.EmailAddress,
                                        "ProfilePictureURL" to owner.ProfilePictureURL,
                                        "uid" to owner.uid,
                                        "role" to "owner"
                                    )


                                    FirebaseSecurity.ownerDb
                                        .collection("Users")
                                        .document(owner.uid)
                                        .set(userData)
                                        .addOnSuccessListener { createMessage = "Successful creation"
                                            createMessage = "Successful creation"
                                            onSignUpSuccess()}
                                    firstName =""
                                    lastName =""
                                    Email =""
                                    password=""
                                }

                            }
                            else{
                                createMessage = "failed Creation!"
                            }

                        }
                },
                colors = greenButtonColors()
            ) {
                Text(text = "Create Account", fontWeight = FontWeight.Bold)
            }

            if (createMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = createMessage)
            }
        }
    }
}

