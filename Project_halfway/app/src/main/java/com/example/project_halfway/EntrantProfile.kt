package com.example.project_halfway

/**
 * An entrant's profile, saved in the Firestore "Users" collection under
 * their user id (see EntrantAuthManager).
 *
 * Used for US 01.09.01 (device sign-in) and the entrant profile stories.
 *
 * Outstanding issues: a new device sign-in starts with every field blank
 * until the entrant fills in their profile.
 *
 * @property Name the entrant's name ("" until they add it)
 * @property EmailAddress the entrant's email address (optional)
 * @property PhoneNumber the entrant's phone number (optional)
 * @property ProfilePicture link to their profile picture, or "" if none
 * @property uid their Firebase Authentication user id
 * @property fcmToken The user's FCM token. Corresponds uniquely to a user's device.
 * For notification services.
 */
data class EntrantProfile (
    val Name: String,
    val EmailAddress: String,
    val PhoneNumber: String,
    val ProfilePicture: String,
    val uid: String,
    val fcmToken: String
)