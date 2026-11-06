package com.example.project_halfway

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 *
 * Created with the assistance of Google, Gemini
 * Prompt: "How do I send a topic notification with FCM API V1 in Kotlin?"
 * @param lotteryId
 *
 */
fun notifyWaitlist(context: Context, lotteryId: String, title: String, message: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Open Service Account file
            val asset = context.assets.open("serviceAccount.json")

            // Read encrypted keys from service account file
            val credentials = GoogleCredentials.fromStream(asset).createScoped(
                listOf("https://www.googleapis.com/auth/firebase.messaging")
            )

            // Get access token
            credentials.refreshIfExpired()
            val accessToken = credentials.accessToken.tokenValue


            val client = OkHttpClient()
            val url = "https://fcm.googleapis.com/v1/projects/cmput301-vector/messages:send"

            // JSON block containing topic, title, and message
            val jsonPayload = """
        {
            "message": {
                "topic": "WAITLIST_CAMPSITE_$lotteryId",
                "notification": {
                    "title": "$title",
                    "body": "$message"
                }
            }
        }
    """.trimIndent()

            val body = jsonPayload.toRequestBody(
                ("application/json; " +
                        "charset=utf-8").toMediaType()
            )

            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(
                        "FCM_HTTP_ERROR", "Failed to dispatch notification network request",
                        e
                    )
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d("FCM_HTTP_SUCCESS", "Response Code: ${response.code}")
                    Log.d("FCM_HTTP_SUCCESS", "Server Response: $responseBody")
                    saveNotification(lotteryId, title, message)
                }
            })
        } catch (e: Exception) {
            Log.e("FCM_PROJECT", "Failed to automatically generate access token", e)
        }
    }
}

fun saveNotification(lotteryId: String, title: String, message: String) {

    val senderId = FirebaseSecurity.auth.currentUser?.uid ?: "UNKNOWN_OWNER"

    val notificationData = hashMapOf(
        "lotteryId" to lotteryId,
        "senderId" to senderId,
        "title" to title,
        "message" to message,
        "timestamp" to FieldValue.serverTimestamp()
    )

    FirebaseSecurity.db.collection("notification_logs")
        .add(notificationData)
        .addOnSuccessListener { ref ->
            Log.d("FIRESTORE_LOG", "Notification logged with ID: ${ref.id}")
        }
        .addOnFailureListener { e ->
            Log.e("FIRESTORE_LOG", "Error saving notification history", e)
        }
}
