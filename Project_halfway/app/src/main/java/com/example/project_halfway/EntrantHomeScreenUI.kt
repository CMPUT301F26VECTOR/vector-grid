package com.example.project_halfway
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.google.apps.card.v1.Button
import androidx.compose.material3.Button
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EntrantHomeScreenUI () {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrant HomeScreen",
            fontWeight = FontWeight.Bold
        )


        Button(
            onClick = {
                FirebaseSecurity.auth.signOut()
            }
        ) {
            Text(text = "Sign Out!")
        }
    }
}