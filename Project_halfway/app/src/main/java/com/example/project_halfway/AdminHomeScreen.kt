package com.example.project_halfway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminHomeScreen () {
    //this design is taken exaclty from the auth gate we implemented
    // it design is same
    var myScreen by remember{ mutableStateOf("home") }

    when(myScreen) {
        "home" -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Admin",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )


                // for campsites
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    ),
                    onClick = {
                        myScreen = "campsites"
                    }
                ) {
                    Text(
                        text = "Campsites",
                        fontSize = 16.sp
                    )
                }





                // for entrant profiles
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    ),
                    onClick = {
                        myScreen = "profiles"
                    }
                ) {
                    Text(
                        text = "Entrant Profiles",
                        fontSize = 16.sp
                    )
                }




                // owner profiles
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    ),
                    onClick = {
                        myScreen = "Ownerprofiles"
                    }
                ) {
                    Text(
                        text = "Owner Profiles",
                        fontSize = 16.sp
                    )
                }





                //for Images
                Button(
                    onClick = {
                        myScreen = "Images"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    Text(
                        text = "Uploaded Images",
                        fontSize = 16.sp
                    )
                }

                // for reviews (US 03.10.01)
                Button(
                    onClick = {
                        myScreen = "Reviews"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    Text(
                        text = "Reviews",
                        fontSize = 16.sp
                    )
                }

                // for ratings (US 03.09.01)
                Button(
                    onClick = {
                        myScreen = "Ratings"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    Text(
                        text = "Ratings",
                        fontSize = 16.sp
                    )
                }

                // Parneet: notification logs (US 03.08.01)
                Button(
                    onClick = {
                        myScreen = "NotificationLogs"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    Text(
                        text = "Notification Logs",
                        fontSize = 16.sp
                    )
                }

                // Parneet: owner message logs (US 03.12.01)
                Button(
                    onClick = {
                        myScreen = "MessageLogs"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppGreen
                    )
                ) {
                    Text(
                        text = "Owner Message Logs",
                        fontSize = 16.sp
                    )
                }
            }
        }

        "campsites" -> {
            CampsiteListUI(
                toback = { myScreen = "home" }
            )
        }

        "profiles" -> {
            ProfilesListUI(
                toback = { myScreen = "home" }
            )
        }


        "Ownerprofiles" ->{
            OwnerProfileListUI(
                toback = {myScreen = "home"}
            )
        }

        "Images" -> {
            ImagesListUI(
                toback = { myScreen = "home" }
            )
        }

        "Reviews" -> {
            AdminReviewsScreen(
                toback = { myScreen = "home" }
            )
        }

        "Ratings" -> {
            AdminRatingsScreen(
                toback = { myScreen = "home" }
            )
        }

        // Parneet: the two log screens (US 03.08.01, US 03.12.01)
        "NotificationLogs" -> {
            NotificationLogsUI(
                ownerMessagesOnly = false,
                toback = { myScreen = "home" }
            )
        }

        "MessageLogs" -> {
            NotificationLogsUI(
                ownerMessagesOnly = true,
                toback = { myScreen = "home" }
            )
        }
    }
}