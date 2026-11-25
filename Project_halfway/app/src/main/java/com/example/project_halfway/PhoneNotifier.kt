package com.example.project_halfway

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Shows notifications as pop-ups on the phone, like "You won the lottery!".
 *
 * NotificationWatcher (below) watches the entrant's notifications in
 * Firestore while the app is open and shows a pop-up for each new one.
 * If the entrant turned notifications off, nothing pops up (US 01.10.01).
 * Tapping a pop-up opens the Notifications screen.
 *
 * Used for US 01.03.01, US 01.03.02 and US 01.10.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: pop-ups only show while the app is open because we don't use Firebase Cloud Messaging. Missed ones are still on the Notifications screen.
 */
object PhoneNotifier {

    /** The id of our notification channel. */
    const val CHANNEL_ID = "campsite_updates"

    /**
     * Makes the notification channel. Android 8 and up needs this before
     * showing pop-ups. It is ok to call this more than once.
     *
     * @param context the app context
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Campsite updates", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Lottery results and reservations"
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if we are allowed to show pop-ups. Android 13 and up asks the
     * user first. Older phones always allow it.
     *
     * @param context the app context
     * @return true if pop-ups are allowed
     */
    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        return result == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Shows one notification as a pop-up. Does nothing if we don't have permission.
     *
     * @param context the app context
     * @param n the notification to show
     */
    fun show(context: Context, n: EntrantNotification) {
        if (!hasPermission(context)) {
            return
        }
        createChannel(context)

        // when the pop-up is tapped, open the Notifications screen
        val intent = Intent(context, NotificationsActivity::class.java)
        val openScreen = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val popUp = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(n.title())
            .setContentText(n.text())
            .setContentIntent(openScreen)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(n.id.hashCode(), popUp)
        } catch (e: SecurityException) {
            println("Could not show the pop-up: " + e.message)
        }
    }

    /**
     * Gets the time of the last notification we showed to this entrant,
     * so the same one doesn't pop up twice.
     *
     * @param context the app context
     * @param entrantId the entrant's id
     * @return the time in milliseconds, or 0 if we never showed one
     */
    fun lastShown(context: Context, entrantId: String): Long {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_" + entrantId, 0L)
    }

    /**
     * Saves the time of the last notification we showed to this entrant.
     *
     * @param context the app context
     * @param entrantId the entrant's id
     * @param time the time in milliseconds
     */
    fun saveLastShown(context: Context, entrantId: String, time: Long) {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_" + entrantId, time).apply()
    }
}

/**
 * Put this on the entrant home screen. It asks for permission to show
 * notifications (Android 13 and up), and then shows a pop-up for every new
 * notification the entrant gets, unless they turned notifications off.
 *
 * The first time it runs, it just marks the old notifications as seen so
 * they don't all pop up at once.
 */
@Composable
fun NotificationWatcher() {
    val context = LocalContext.current
    val entrantId = FirebaseSecurity.auth.currentUser?.uid ?: ""

    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        PhoneNotifier.createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PhoneNotifier.hasPermission(context)) {
            askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(entrantId) {
        if (entrantId == "") {
            return@DisposableEffect onDispose { }
        }

        val listener = NotificationRepository.listenForEntrant(entrantId) { list ->
            val last = PhoneNotifier.lastShown(context, entrantId)

            // find the newest time in the list
            var newest = 0L
            for (n in list) {
                if (n.sentAt > newest) {
                    newest = n.sentAt
                }
            }

            if (last == 0L) {
                // first time: just remember what is already there (1 means "nothing yet")
                PhoneNotifier.saveLastShown(context, entrantId, maxOf(newest, 1L))
            } else {
                NotificationRepository.isOptedOut(entrantId) { optedOut ->
                    val toShow = NotificationHelper.toPopUp(list, last, optedOut)
                    for (n in toShow) {
                        PhoneNotifier.show(context, n)
                    }
                    if (newest > last) {
                        PhoneNotifier.saveLastShown(context, entrantId, newest)
                    }
                }
            }
        }

        onDispose {
            listener.remove()
        }
    }
}
