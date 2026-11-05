package com.example.project_halfway

/**
 * This class represents the entrant class and contains
 * its properties like name, email, phone number,
 * pic and uid. These properties will be used to store
 * in a list and later will be used by firebase to
 * store them in the database
 *
 * @property Name This is the name of entrant
 * @property EmailAddress this is the email of entrant
 * @property PhoneNumber this is cell phone number
 * @property uid this it the unique identifier on firebase database
 */

data class EntrantProfile (
    val Name: String,
    val EmailAddress:String,
    val PhoneNumber: String,
    val ProfilePicture:String,
    val uid:String
)