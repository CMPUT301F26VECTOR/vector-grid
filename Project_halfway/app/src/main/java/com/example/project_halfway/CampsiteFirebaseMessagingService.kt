package com.example.project_halfway

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Firebase notification service.
 * Class structure adapted from:
 * https://firebase.blog/posts/2023/08/adding-fcm-to-jetpack-compose-app/
 *
 * Outstanding issues: None known.
 */
class CampsiteFirebaseMessagingService : FirebaseMessagingService()  {
    val defaultNotificationChannelId = "waitlist_alerts_channel"
    val defaultChannelName = "Waitlist Alerts"
    val defaultChannelDescription = "Notifications that campsite owners send to entrants in their" +
            " waitlist."

    /**
     * Creates a notification channel that allows users to manage their notification preferences
     * on their device. Required on API 26+.
     * Adapted from
     * https://github.com/android/snippets/blob/b160bad98c32bcd4caad9a887add19fc28979284/compose/snippets/src/main/java/com/example/compose/snippets/notifications/NotificationsSnippets.kt#L549-L559
     *
     * Outstanding issues: I believe we will have to adapt this function in the future to create
     * multiple notification channels for messaging and other notification types. Right now
     * we have only one default notification channel.
     *
     * @param context - Object required for accessing Android notification manager
     */
    fun createNotificationChannel(context: Context) {
        val channelName = defaultChannelName
        val channelDescription = defaultChannelDescription
        val channelId = defaultNotificationChannelId

        // Check API level:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT).apply {
                description = channelDescription
            }

            // Register the channel with Android system.
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     *
     * Adapted from
     * https://firebase.blog/posts/2023/08/adding-fcm-to-jetpack-compose-app/
     *
     * Outstanding issues: N/A
     *
     * @param remoteMessage
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { message ->
            sendNotification(message)
        }
    }

    /**
     * Adapted from
     * https://firebase.blog/posts/2023/08/adding-fcm-to-jetpack-compose-app/
     *
     * Outstanding issues: N/A
     *
     * @param message -
     */
    private fun sendNotification(message: RemoteMessage.Notification) {
        val channelId = defaultNotificationChannelId

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Generates an ID based on the last 9 digits of the timestamp
        val uniqueId = (System.currentTimeMillis() % 100000000).toInt()

        createNotificationChannel(context = applicationContext)

        // Sends notification with unique identifier
        manager.notify(uniqueId, notificationBuilder.build())
    }

}