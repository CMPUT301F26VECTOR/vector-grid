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

/**
 * Screen opened by "Create Account" on the owner login screen. It shows
 * SignUpScreen, and once the owner's account is created it shows
 * OwnerHomeScreenUI. Signing out goes back to MainActivity.
 *
 * Outstanding issues: despite its name, this is the campsite owner sign-up
 * flow, not the entrant home screen (the entrant home screen is shown by
 * AuthGate). The old commented-out versions above could be removed.
 */
class EntrantHomeScreenClass : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showOwnerHome by rememberSaveable {
                mutableStateOf(false)
            }

            Project_halfwayTheme {
//                if (showOwnerHome) {
////                    Text("Owner Home Screen")
//                    OwnerHomeScreenUI()
//
//                }
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
