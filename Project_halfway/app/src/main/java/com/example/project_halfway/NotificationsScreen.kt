package com.example.project_halfway

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The entrant's Notifications screen. It opens from the "Notifications" card
 * on the home screen, or when a pop-up is tapped.
 *
 * It shows all the entrant's notifications: lottery wins and losses,
 * reservations, cancellations and owner messages. At the top there is a
 * switch to turn notifications off.
 *
 * Used for US 01.03.01, US 01.03.02 and US 01.10.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: turning notifications off only stops the pop-ups. The list still shows here so the entrant doesn't miss a lottery win.
 *
 * @param onBackClick called when the back arrow is pressed
 */
@Composable
fun NotificationsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val uid = FirebaseSecurity.auth.currentUser?.uid ?: ""

    val notifications = remember { mutableStateListOf<EntrantNotification>() }
    val campsiteNames = remember { mutableStateMapOf<String, String>() }
    var optedOut by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    // load the opt-out setting when the screen opens
    LaunchedEffect(uid) {
        if (uid != "") {
            NotificationRepository.isOptedOut(uid) { optedOut = it }
        }
    }

    // keep the list up to date while the screen is open
    DisposableEffect(uid) {
        if (uid == "") {
            loading = false
            return@DisposableEffect onDispose { }
        }
        val listener = NotificationRepository.listenForEntrant(uid) { list ->
            notifications.clear()
            notifications.addAll(list)
            loading = false
            // load the campsite names we don't have yet
            for (n in list) {
                if (n.campsiteId != "" && campsiteNames[n.campsiteId] == null) {
                    CampsiteRepository.getById(n.campsiteId) { campsite ->
                        campsiteNames[n.campsiteId] = campsite.getCampsiteName()
                    }
                }
            }
        }
        onDispose {
            listener.remove()
        }
    }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        GreenScreenHeader(title = "Notifications", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(HeaderGap))

        WhitePanel {
            OptOutRow(
                optedOut = optedOut,
                onChange = { off ->
                    optedOut = off
                    if (uid != "") {
                        NotificationRepository.setOptedOut(uid, off) { saved ->
                            if (!saved) {
                                Toast.makeText(context, "Could not save, try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Loading...", color = Color.White)
        } else if (notifications.isEmpty()) {
            Text("No notifications yet.", color = Color.White)
        }

        for (n in notifications.toList()) {
            NotificationCard(
                title = n.title(),
                campsiteName = campsiteNames[n.campsiteId] ?: "",
                text = n.text(),
                time = n.sentAtText()
            )
        }
    }
}

/**
 * The switch to turn notifications on or off (US 01.10.01).
 *
 * @param optedOut true if notifications are off right now
 * @param onChange called with true when the entrant turns them off, and false when they turn them on
 */
@Composable
fun OptOutRow(optedOut: Boolean, onChange: (Boolean) -> Unit) {
    var statusText = "On: you get pop-ups for results and reservations"
    if (optedOut) {
        statusText = "Off: you won't get pop-ups"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Receive notifications", fontWeight = FontWeight.Bold, color = Color.Black)
            Text(statusText, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.testTag("opt_out_status"))
        }
        // the switch is "on" when notifications are on, so it is the opposite of optedOut
        Switch(
            checked = !optedOut,
            onCheckedChange = { on -> onChange(!on) },
            modifier = Modifier.testTag("opt_out_switch")
        )
    }
}

/**
 * One notification in the list.
 *
 * @param title the title, like "You won the lottery!"
 * @param campsiteName the campsite name, or "" if we don't know it
 * @param text the notification text
 * @param time when it was sent
 */
@Composable
fun NotificationCard(title: String, campsiteName: String, text: String, time: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .testTag("notification_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            if (campsiteName != "") {
                Text(campsiteName, fontSize = 14.sp, color = AppGreen)
            }
            Text(text, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
            if (time != "") {
                Text(time, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
