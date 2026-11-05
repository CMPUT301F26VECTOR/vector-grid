package com.example.project_halfway

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The campsite owner login screen: email and password, SIGN IN and
 * Create Account. Signs in through FirebaseSecurity.ownerAuth.
 *
 * **AI assistance (Varun Talwar):**
 * - The title text style was made with OpenAI ChatGPT on 2026-10-21.
 *   Prompt: "how can i improve this font style"
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =Alignment.CenterHorizontally
    ){
        // Back arrow: returns to the welcome screen
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start).padding(top = 24.dp, start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xff566C11)
            )
        }
        Spacer(modifier = Modifier.height(78.dp))
        // AI-assisted: see the AI assistance note in this function's KDoc
        Text(text ="Campsite\nReservation\nSystem",
            fontWeight = FontWeight.Thin,
            fontFamily = FontFamily.SansSerif,
            fontSize =  64.sp,
            textAlign = TextAlign.Center,
            lineHeight = 48.sp,
            color = Color(0xff566C11)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "LOGIN",
            modifier = Modifier.align(Alignment.Start)
                .padding(20.dp),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp)



        // EMAIL
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email image"

                )
            },
            onValueChange = {Email = it},
            label ={Text(text="Email")},
            placeholder = {Text(text="ABC@gmail.com")}
        )




        //Password
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = Password,
            leadingIcon ={
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "lock image"
                )
            },
            onValueChange = {Password = it},
            label = {Text(text = "Password")},
            placeholder = {Text(text="**********")}
        )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement  = Arrangement.SpaceEvenly
        ){


            //SIGN IN
            Button(
                modifier = Modifier.weight(1f)
                    .width(40.dp)
                    .height(40.dp),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff566C11)
                )

            ){
                Text(text = "SIGN IN ")

            }
            Spacer(modifier = Modifier.width(50.dp))

            //Create Account
            Button(
                modifier = Modifier.weight(1f)
                    .width(40.dp)
                    .height(40.dp),
                onClick = {
                    val intent  = Intent(
                        context,
                        EntrantHomeScreenClass::class.java
                    )
                    context.startActivity(intent)

                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff566C11)
                )

            ){
                Text(text = "Create Account")

            }
        }

        Text(text=loginMessage)


    }
}


