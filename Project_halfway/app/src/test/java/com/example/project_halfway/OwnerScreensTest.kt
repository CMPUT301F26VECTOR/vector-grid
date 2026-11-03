package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests where the back arrow and the phone's back button lead on the owner's
 * screens (ownerBackTarget in OwnerScreens.kt). No Android or Firestore needed.
 */
class OwnerScreensTest {

    @Test
    fun backFromRegister_goesHome() {
        assertEquals("home", ownerBackTarget("register"))
    }

    @Test
    fun backFromMyCampsites_goesHome() {
        assertEquals("home", ownerBackTarget("mine"))
    }

    @Test
    fun backFromDetails_goesToMyCampsites() {
        assertEquals("mine", ownerBackTarget("details"))
    }

    @Test
    fun backFromWaitlist_goesToDetails() {
        assertEquals("details", ownerBackTarget("waitlist"))
    }

    @Test
    fun unknownScreen_goesHome() {
        assertEquals("home", ownerBackTarget("something else"))
    }
}
