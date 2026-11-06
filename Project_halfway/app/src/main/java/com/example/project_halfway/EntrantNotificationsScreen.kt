package com.example.project_halfway

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Displays to entrants a list of their past notifications.
 *
 * @param onBack called when back button is pressed
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
public fun EntrantNotificationsScreen(onBack: () -> Unit) {
    RequestNotificationPermissionDialog()
}
