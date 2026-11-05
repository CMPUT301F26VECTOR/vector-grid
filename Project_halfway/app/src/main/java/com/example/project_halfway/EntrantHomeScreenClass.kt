////
////package com.example.project_halfway
////
////import android.os.Bundle
////import androidx.activity.ComponentActivity
////import androidx.activity.compose.setContent
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.mutableStateOf
////import androidx.compose.runtime.saveable.rememberSaveable
////import androidx.compose.runtime.setValue
////import com.example.project_halfway.ui.theme.Project_halfwayTheme
////
/////**
//// * Displays the entrant home screen and handles navigation between the home screen and Browse Campsites.
//// */
////class EntrantHomeScreenClass : ComponentActivity() {
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////
////        setContent {
////            Project_halfwayTheme {
////                var showBrowse by rememberSaveable {
////                    mutableStateOf(false)
////                }
////
////                if (showBrowse) {
////                    BrowseCampsitesScreenUI(
////                        onBackClick = {
////                            showBrowse = false
////                        }
////                    )
////                } else {
////                    onExit = {},
////                    EntrantHomeScreenUI(
////                        onBrowseClick = {
////                            showBrowse = true
////                        }
////                    )
////                }
////            }
////        }
////    }
////}
//package com.example.project_halfway
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.material3.Text
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import com.example.project_halfway.ui.theme.Project_halfwayTheme
//
////class EntrantHomeScreenClass: ComponentActivity(){
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContent {
////            var showOwnerHome by rememberSaveable {
////                mutableStateOf(false)
////            }
////        }
////        Project_halfwayTheme {
////            if (showOwnerHome) {
////                Text("Owner Home Screen")
////            } else {
////                SignUpScreen(
////                    onSignUpSuccess = {
////                        showOwnerHome = true
////                    }
////                )
////            }
////        }
////    }
////}
////}
//package com.example.project_halfway
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import com.example.project_halfway.ui.theme.Project_halfwayTheme
//
//class EntrantHomeScreenClass : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        setContent {
//            var showOwnerHome by rememberSaveable {
//                mutableStateOf(false)
//            }
//
//            Project_halfwayTheme {
//                if (showOwnerHome) {
//                    Text("Owner Home Screen")
//                } else {
//                    SignUpScreen(
//                        onSignUpSuccess = {
//                            showOwnerHome = true
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
package com.example.project_halfway

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.project_halfway.ui.theme.Project_halfwayTheme

class EntrantHomeScreenClass : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showOwnerHome by rememberSaveable {
                mutableStateOf(false)
            }

            Project_halfwayTheme {
//This part has been implemented with OpenAI ChatGPT help, 2026-10-25
                // "can help me to connect my code to next home screen"
                if (showOwnerHome) {
                    OwnerHomeScreenUI(
                        onSignOut = {
                            FirebaseSecurity.ownerAuth.signOut()

                            val intent = Intent(
                                this@EntrantHomeScreenClass,
                                MainActivity::class.java
                            )

                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                        }
                    )
                }
                else {
                    SignUpScreen(
                        onSignUpSuccess = {
                            showOwnerHome = true
                        }
                    )
                }
            }
        }
    }
}
