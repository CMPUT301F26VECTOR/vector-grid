package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the campsite QR code (US 02.02.01): the link it holds, reading
 * the campsite id back, and that each campsite gets its own code.
 * No Android needed (ZXing is plain Java).
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class CampsiteQrTest {

    @Test
    fun theLinkHoldsTheCampsiteId() {
        assertEquals("campermax://campsite/abc123", CampsiteQr.linkFor("abc123"))
    }

    @Test
    fun theCampsiteIdCanBeReadBack() {
        assertEquals("abc123", CampsiteQr.campsiteIdFrom(CampsiteQr.linkFor("abc123")))
    }

    @Test
    fun otherCodesAreNotCampsites() {
        assertNull(CampsiteQr.campsiteIdFrom("https://example.com"))
        assertNull(CampsiteQr.campsiteIdFrom(CampsiteQr.PREFIX))
    }

    @Test
    fun differentCampsitesGetDifferentCodes() {
        val first = CampsiteQr.matrixFor(CampsiteQr.linkFor("abc123"), 200)
        val second = CampsiteQr.matrixFor(CampsiteQr.linkFor("xyz789"), 200)
        assertNotEquals(first, second)
    }

    @Test
    fun theSameCampsiteAlwaysGetsTheSameCode() {
        val first = CampsiteQr.matrixFor(CampsiteQr.linkFor("abc123"), 200)
        val again = CampsiteQr.matrixFor(CampsiteQr.linkFor("abc123"), 200)
        assertEquals(first, again)
        assertEquals(200, first.width)
    }
}
