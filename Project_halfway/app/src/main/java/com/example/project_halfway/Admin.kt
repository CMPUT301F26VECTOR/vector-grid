package com.example.project_halfway

/**
 * An administrator account, who can look after every entrant, owner and
 * campsite in the app (planned from the CRC cards).
 *
 * Outstanding issues: not used by any screen yet and not saved to Firestore.
 * The password is stored as plain text; real sign-in should go through
 * Firebase Authentication instead of keeping a password here.
 *
 * @property Name the admin's name
 * @property Password the admin's password (see outstanding issues)
 * @property EmailAddress the admin's email address
 * @property profilePicURL link to the admin's profile picture
 * @property allEntrants every entrant profile the admin can manage
 * @property allOwnersClass every campsite owner the admin can manage
 * @property allCampsitesClass every campsite the admin can manage
 */
class Admin (
    private val Name:String,
    private val Password:String,
    private val EmailAddress:String,
    private val profilePicURL: String,
    private val allEntrants: AllEntrants,
    private val allOwnersClass: AllOwners,
    private val allCampsitesClass: AllCampsites
)



