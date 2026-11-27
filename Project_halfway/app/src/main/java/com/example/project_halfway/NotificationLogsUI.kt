package com.example.project_halfway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The admin's log screens, opened from the admin home screen.
 *
 * If ownerMessagesOnly is false, it shows every notification the app sent
 * (US 03.08.01). If it is true, it shows only the messages campsite owners
 * sent to entrants (US 03.12.01), which are saved in the
 * "notification_logs" collection (US 02.10.01). Each row shows the title, the text, who
 * got it, the campsite and the time. Names are shown instead of ids when we
 * can find them.
 *
 * Used for US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: the admin can only read the logs. They can't search or filter them by date.
 *
 * @param ownerMessagesOnly true to show only owner messages, false to show all notifications
 * @param toback called when "Go Back" is pressed
 */
@Composable
fun NotificationLogsUI(ownerMessagesOnly: Boolean, toback: () -> Unit) {
    var logs by remember { mutableStateOf<List<EntrantNotification>>(emptyList()) }
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(ownerMessagesOnly) {
        if (ownerMessagesOnly) {
            NotificationRepository.getOwnerMessagesForAdmin(
                onResult = { messages ->
                    logs = messages
                    loading = false
                },
                onError = { error ->
                    message = error
                    loading = false
                }
            )
        } else {
            NotificationRepository.getAllForAdmin(
                onResult = { all ->
                    logs = all
                    loading = false
                },
                onError = { error ->
                    message = error
                    loading = false
                }
            )
        }
        NotificationRepository.loadNamesForAdmin { names = it }
    }

    var title = "Notification Logs"
    if (ownerMessagesOnly) {
        title = "Owner Message Logs"
    }

    NotificationLogList(
        title = title,
        logs = logs,
        names = names,
        loading = loading,
        message = message,
        toback = toback
    )
}

/**
 * The list part of the log screen. It is separate so we can test it without Firestore.
 *
 * @param title the screen's title
 * @param logs the notifications to show, newest first
 * @param names a map of id to name for users and campsites
 * @param loading true while the logs are loading
 * @param message an error message, or ""
 * @param toback called when "Go Back" is pressed
 */
@Composable
fun NotificationLogList(
    title: String,
    logs: List<EntrantNotification>,
    names: Map<String, String>,
    loading: Boolean,
    message: String,
    toback: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "${logs.size} total", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Loading....")
        }
        if (message != "") {
            Text(message, color = Color.Red)
        }
        if (!loading && logs.isEmpty() && message == "") {
            Text("Nothing has been sent yet")
        }

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(logs) { n ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp).testTag("log_row")) {
                    Text(n.title(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(n.text(), fontSize = 14.sp)
                    if (n.isOwnerMessage()) {
                        Text("From: " + NotificationHelper.nameOrId(names, n.senderId), fontSize = 13.sp, color = Color.DarkGray)
                    }
                    if (n.entrantId != "") {
                        Text("To: " + NotificationHelper.nameOrId(names, n.entrantId), fontSize = 13.sp, color = Color.DarkGray)
                    } else {
                        Text("To: entrants in the lottery", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    if (n.campsiteId != "") {
                        Text("Campsite: " + NotificationHelper.nameOrId(names, n.campsiteId), fontSize = 13.sp, color = Color.DarkGray)
                    }
                    Text(n.sentAtText(), fontSize = 12.sp, color = Color.Gray)
                }
                HorizontalDivider()
            }
        }

        Button(
            onClick = toback,
            colors = ButtonDefaults.buttonColors(containerColor = AppGreen)
        ) {
            Text(text = "Go Back")
        }
    }
}

