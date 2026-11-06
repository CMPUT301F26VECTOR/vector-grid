package com.example.project_halfway

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The campsite owner login screen: the CamperMax logo, then a white panel with
 * email and password, SIGN IN and Create Account, on the app's green (the same
 * look as Browse Campsites). Signs in through FirebaseSecurity.ownerAuth.
 *
 * **AI assistance (Varun Talwar):**
 * - The title text style was made with OpenAI ChatGPT on 2026-10-21.
 *   Prompt: "how can i improve this font style". (On 2026-11-05 that title
 *   was replaced by the CamperMax logo.)
 * - The sign-in call and its listeners were made with OpenAI ChatGPT and the
 *   resources the TA provided, on 2026-10-25. Prompt: "help me with syntax and
 *   listeners for signin firebase"
 *
 * Outstanding issues: Create Account opens EntrantHomeScreenClass (the owner
 * sign-up screen) as a separate activity instead of going through AuthGate.
 *
 * @param onLoginSuccess called after the owner signs in successfully
 * @param onBackClick called when the back arrow (or the phone's back button)
 * is pressed, to return to the welcome screen
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenUI (
    onLoginSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var Email by remember{ mutableStateOf("") }
    var Password by remember {mutableStateOf("")}
    var loginMessage by remember{mutableStateOf("")}
    val context  = LocalContext.current

    // The phone's back button/gesture does the same as the back arrow
    BackHandler(onBack = onBackClick)

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
        // Back arrow: returns to the welcome screen
        Box(modifier = Modifier.fillMaxWidth()) {
            GreenScreenHeader(title = "Owner Login", onBackClick = onBackClick)
        }

        Spacer(modifier = Modifier.height(24.dp))
        CampermaxLogo(
            tileSize = 96.dp,
            nameSize = 26,
            nameColor = Color.White,
            taglineColor = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(28.dp))

        WhitePanel {
            Text(
                text = "LOGIN",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // EMAIL
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = Email,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email image"
                    )
                },
                onValueChange = { Email = it },
                label = { Text(text = "Email") },
                placeholder = { Text(text = "ABC@gmail.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = greenFieldColors()
            )
            PanelGap()

            // Password, shown as dots
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = Password,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "lock image"
                    )
                },
                onValueChange = { Password = it },
                label = { Text(text = "Password") },
                placeholder = { Text(text = "**********") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = greenFieldColors()
            )
            Spacer(modifier = Modifier.height(20.dp))

            // SIGN IN
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {
                    // AI-assisted: see the AI assistance note in this function's KDoc
                    FirebaseSecurity.ownerAuth.signInWithEmailAndPassword(Email.trim(),Password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                loginMessage = "Login successful!"
                                onLoginSuccess()
                            } else {
                                loginMessage = task.exception?.localizedMessage
                                    ?: "Login failed"
                            }
                        }
                },
                colors = greenButtonColors()
            ) {
                Text(text = "SIGN IN ", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Create Account: opens the owner sign-up screen
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {
                    val intent = Intent(
                        context,
                        EntrantHomeScreenClass::class.java
                    )
                    context.startActivity(intent)
                },
                border = BorderStroke(1.dp, AppGreen),
                colors = greenOutlinedButtonColors()
            ) {
                Text(text = "Create Account", fontWeight = FontWeight.Bold)
            }

            if (loginMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = loginMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


