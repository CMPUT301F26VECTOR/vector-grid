package com.example.project_halfway

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OwnerNotifyEntrantsScreen(onBackClick: () -> (Unit), lottery: Lottery) {
    var notificationTitle by remember { mutableStateOf("") }
    var notificationBody by remember { mutableStateOf("") }
    var notificationSent by remember{ mutableStateOf(false)}
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back arrow: returns to the campsite
        Box(modifier = Modifier.fillMaxWidth()) {
            GreenScreenHeader(title = "Compose Message", onBackClick = onBackClick)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(28.dp))

        WhitePanel {
            Text(
                text = "Send message out to waitlist:",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = notificationTitle,
                onValueChange = { notificationTitle = it },
                label = { Text(text = "Title") },
                singleLine = false,
                colors = greenFieldColors()
            )
            PanelGap()

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = notificationBody,
                onValueChange = { notificationBody = it },
                label = { Text(text = "Message body") },
                singleLine = false,
                colors = greenFieldColors()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                modifier = Modifier
                    .height(48.dp),
                onClick = {
                    notifyWaitlist(context = context, lotteryId = lottery.getLotteryId(),
                        title = notificationTitle, message = notificationBody)
                    notificationSent = true
                },
                colors = greenButtonColors()
            ) {
                Text(text = "Send", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (notificationSent) {
                Text(
                    text = "Message sent to waitlist!",
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp
                )
            }
        }
    }
}

/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EntrantHomeScreenPreview() {
    OwnerNotifyEntrantsScreen( {})
}
 */

