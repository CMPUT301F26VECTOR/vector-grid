package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the optional campsite image (US 02.01.02) and for owners only
 * editing their own campsites (US 02.01.03). No Android or Firestore needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class CampsiteImageAndOwnerTest {

    private val saved = CampsiteModel(
        campsiteName = "Site 7", campgroundName = "Elk Island", location = "Edmonton, AB",
        longitude = 0.0, latitude = 0.0, occupancy = 4, description = "Shady",
        imageUrl = "https://example.com/old.jpg", id = "camp1", ownerId = "owner1"
    )

    private val form = CampsiteForm(name = "Site 7", location = "Edmonton, AB", occupancy = "4")

    @Test
    fun aCampsiteCanBeSavedWithoutAnImage() {
        assertEquals("", form.copy(imageUrl = "").toCampsite("owner1").getImageUrl())
    }

    @Test
    fun anUploadedImageLinkIsSaved() {
        val campsite = form.copy(imageUrl = "https://example.com/new.jpg").toCampsite("owner1", saved)
        assertEquals("https://example.com/new.jpg", campsite.getImageUrl())
    }

    @Test
    fun removingTheImageClearsIt() {
        assertEquals("", form.copy(imageUrl = "").toCampsite("owner1", saved).getImageUrl())
    }

    @Test
    fun notTouchingTheImageKeepsTheOldOne() {
        assertEquals("https://example.com/old.jpg", form.toCampsite("owner1", saved).getImageUrl())
    }

    @Test
    fun eachUploadGetsItsOwnFile() {
        assertEquals("campsite_images/owner1/5.jpg", CampsiteImageUploader.pathFor("owner1", 5L))
    }

    @Test
    fun onlyTheCampsitesOwnerCanEditIt() {
        assertTrue(CampsiteForm.canEdit(saved, "owner1"))
        assertFalse(CampsiteForm.canEdit(saved, "owner2"))
        assertFalse(CampsiteForm.canEdit(saved, ""))
    }

    @Test
    fun theNewScreensGoBackToTheRightPlace() {
        assertEquals("details", ownerBackTarget("bookedDates"))
        assertEquals("details", ownerBackTarget("editLottery"))
        assertEquals("home", ownerBackTarget("reservations"))
    }
}
