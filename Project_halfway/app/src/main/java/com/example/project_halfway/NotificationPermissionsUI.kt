package com.example.project_halfway

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Prompts a dialogue box asking the user to enable push notifications if not already enabled
 * on the device. Displays the Android "Allow <App> to send you notifications?" box on confirm.
 *
 * Adapted from
 * https://firebase.blog/posts/2023/08/adding-fcm-to-jetpack-compose-app/
 *
 * Outstanding issues: N/A
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionDialog() {
    val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (!permissionState.status.isGranted) {
        if (!permissionState.status.shouldShowRationale) PermissionDialogue({ },
            { permissionState.launchPermissionRequest() })
    }
}

@Composable
fun PermissionDialogue(onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
    PermissionDialogueCard(onDismissRequest, onConfirmation, "Notification" +
            " Permission", "Please enable push notifications.",
        "Request")

}

/**
 * Displays a dialogue card that can be confirmed or dismissed.
 * Adapted from
 * https://developer.android.com/develop/ui/compose/components/dialog
 *
 * Outstanding issues: N/A
 */
@Composable
fun PermissionDialogueCard(onDismissRequest: () -> Unit, onConfirmation: () -> Unit,
                           dialogTitle: String, dialogText: String, confirmText: String) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}