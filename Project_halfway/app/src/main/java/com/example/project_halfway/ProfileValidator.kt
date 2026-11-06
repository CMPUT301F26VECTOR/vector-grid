package com.example.project_halfway

/**
 * Checks the entrant's profile details before they are saved.
 *
 * What it checks:
 * - The name is not empty.
 * - The email looks like something@something.something, if it is filled in.
 * - The phone number has 10 digits, if it is filled in.
 *
 * Email and phone are optional because a new device sign-in starts with them blank.
 * ProfileEditForm calls checkInfo when Save is pressed.
 *
 * Used for US 01.07.03 (update name, email and phone number).
 *
 * Outstanding issues: none known.
 */
object ProfileValidator {

    /**
     * Checks the name, email and phone number, in that order.
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
     * Checks that an email looks right.
     *
     * It needs something before the @, and a dot with something on both sides
     * after it, with no spaces. For example a@b.com works but a@b doesn't.
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
     * Checks that a phone number has exactly 10 digits.
     *
     * Spaces, dashes and brackets are allowed, so 780-555-1234 and
     * (780) 555 1234 both work. Any other character makes it invalid.
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
