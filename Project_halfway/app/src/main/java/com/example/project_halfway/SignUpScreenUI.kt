package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreen () {

    var firstName by remember{ mutableStateOf("") }
    var lastName by remember{mutableStateOf("")}
    var Email by remember{mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var createMessage by remember{mutableStateOf("")}

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(150.dp))
        Text(text ="Campsite\nReservation\nSystem",
            fontWeight = FontWeight.Thin,
            fontFamily = FontFamily.SansSerif,
            fontSize =  64.sp,
            textAlign = TextAlign.Center,
            lineHeight = 48.sp,
            color = Color(0xff566C11)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "SIGN UP",
            modifier = Modifier.align(Alignment.Start)
                .padding(20.dp),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp)

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = firstName,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.outline_drive_file_rename_24),
                    contentDescription = "Email image",
                    modifier = Modifier.size(30.dp)

                )
            },
            onValueChange = {firstName = it},
            label ={Text(text="First Name")},
            placeholder = {Text(text="ABC XYZ")}
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = lastName,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.rounded_contextual_token_24),
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Email image"

                )
            },
            onValueChange = {lastName = it},
            label ={Text(text="Last Name")},
            placeholder = {Text(text="XYZ ABC")}
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = Email,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Email image"

                )
            },
            onValueChange = {Email = it},
            label ={Text(text="Email")},
            placeholder = {Text(text="ABC@gmail.com")}
        )


        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = password,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Email image"

                )
            },
            onValueChange = {password = it},
            label ={Text(text="Passowrd")},
            placeholder = {Text(text="************")}
        )

        Spacer(modifier = Modifier.height(40.dp))
    Button(
        modifier = Modifier.width(200.dp)
                    .height(60.dp),
        onClick = {
            FirebaseSecurity.auth.createUserWithEmailAndPassword(Email.trim(),password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        var user = FirebaseSecurity.auth.currentUser
                        if (user!=null) {
                            val userData = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to Email.trim(),
                                "role" to "entrant"
                            )


                            FirebaseSecurity.db
                                .collection("Users")
                                .document(user.uid)
                                .set(userData)
                                .addOnSuccessListener { createMessage = "Successful creation"}
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
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xff566C11)
        )
    ){
        Text(text = "Create Account",
            fontSize = 20.sp)
    }

        Text(text = createMessage)
}
    }
