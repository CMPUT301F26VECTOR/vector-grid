package com.example.project_halfway

/**
 * Checks the entrant's profile info before it is saved.
 *
 * Used for US 01.07.03 (update name, email and phone number on the profile).
 * Name is needed. Email and phone are optional (a new device sign-in starts
 * with them blank), but if they are filled in they have to look right.
 *
 * Outstanding issues: none known.
 */
object ProfileValidator {

    /**
     * Checks the name, email and phone number.
     *
     * @param name the entrant's name
     * @param email the entrant's email, can be blank
     * @param phone the entrant's phone number, can be blank
     * @return null if everything is ok, or a message saying what's wrong
     */
    fun checkInfo(name: String, email: String, phone: String): String? {
        if (name.isBlank()) {
            return "Name can't be empty"
        }
        if (email.isNotBlank() && !isValidEmail(email.trim())) {
            return "Please enter a valid email"
        }
        if (phone.isNotBlank() && !isValidPhone(phone)) {
            return "Phone number should be 10 digits"
        }
        return null
    }

    /**
     * Simple email check, needs something@something.something with no spaces.
     *
     * @param email the email to check
     * @return true if it looks like an email
     */
    fun isValidEmail(email: String): Boolean {
        if (email.contains(" ")) {
            return false
        }
        val atIndex = email.indexOf("@")
        if (atIndex < 1) {
            return false
        }
        val domain = email.substring(atIndex + 1)
        val dotIndex = domain.lastIndexOf(".")
        return dotIndex > 0 && dotIndex < domain.length - 1
    }

    /**
     * Phone has to be 10 digits. Spaces, dashes and brackets are allowed,
     * so 780-555-1234 and (780) 555 1234 both work.
     *
     * @param phone the phone number to check
     * @return true if it is a 10 digit phone number
     */
    fun isValidPhone(phone: String): Boolean {
        var digits = ""
        for (c in phone) {
            if (c.isDigit()) {
                digits += c
            } else if (c != ' ' && c != '-' && c != '(' && c != ')') {
                return false
            }
        }
        return digits.length == 10
    }
}
