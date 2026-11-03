package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests for checking the profile info before saving (US 01.07.03)

class ProfileValidatorTest {

    @Test
    fun checkInfo_allGood() {
        assertNull(ProfileValidator.checkInfo("Parneet Kaur", "parneet@ualberta.ca", "780-555-1234"))
    }

    @Test
    fun checkInfo_emailAndPhoneAreOptional() {
        assertNull(ProfileValidator.checkInfo("Parneet Kaur", "", ""))
    }

    @Test
    fun checkInfo_emptyName() {
        assertEquals("Name can't be empty", ProfileValidator.checkInfo("   ", "parneet@ualberta.ca", ""))
    }

    @Test
    fun checkInfo_badEmail() {
        assertEquals("Please enter a valid email", ProfileValidator.checkInfo("Parneet", "parneet", ""))
    }

    @Test
    fun checkInfo_badPhone() {
        assertEquals("Phone number should be 10 digits", ProfileValidator.checkInfo("Parneet", "", "12345"))
    }

    @Test
    fun isValidEmail_examples() {
        assertTrue(ProfileValidator.isValidEmail("deepali@gmail.com"))
        assertFalse(ProfileValidator.isValidEmail("deepali@gmail"))
        assertFalse(ProfileValidator.isValidEmail("@gmail.com"))
        assertFalse(ProfileValidator.isValidEmail("deep ali@gmail.com"))
        assertFalse(ProfileValidator.isValidEmail("deepali@gmail."))
    }

    @Test
    fun isValidPhone_examples() {
        assertTrue(ProfileValidator.isValidPhone("7805551234"))
        assertTrue(ProfileValidator.isValidPhone("780-555-1234"))
        assertTrue(ProfileValidator.isValidPhone("(780) 555 1234"))
        assertFalse(ProfileValidator.isValidPhone("780-555-12345"))
        assertFalse(ProfileValidator.isValidPhone("780-abc-1234"))
    }

    @Test
    fun checkInfo_givesAMessageForEachProblem() {
        assertNotNull(ProfileValidator.checkInfo("", "", ""))
        assertNotNull(ProfileValidator.checkInfo("A", "a@b", ""))
        assertNotNull(ProfileValidator.checkInfo("A", "a@b.com", "1"))
    }
}
