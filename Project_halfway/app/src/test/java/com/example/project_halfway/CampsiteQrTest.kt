package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for CampsiteQr: making the QR text and reading the campsite id back
 * from scanned codes, including codes that are not campsite codes.
 *
 * Used for US 01.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class CampsiteQrTest {

    @Test
    fun textFor_addsThePrefix() {
        assertEquals("campermax://campsite/Xy7Kp2LmQ9", CampsiteQr.textFor("Xy7Kp2LmQ9"))
    }

    @Test
    fun campsiteIdFrom_readsBackTheId() {
        assertEquals("Xy7Kp2LmQ9", CampsiteQr.campsiteIdFrom(CampsiteQr.textFor("Xy7Kp2LmQ9")))
    }

    @Test
    fun campsiteIdFrom_acceptsAPlainId() {
        assertEquals("Xy7Kp2LmQ9", CampsiteQr.campsiteIdFrom("  Xy7Kp2LmQ9 "))
    }

    @Test
    fun campsiteIdFrom_rejectsOtherCodes() {
        assertNull(CampsiteQr.campsiteIdFrom("https://www.ualberta.ca/events"))
        assertNull(CampsiteQr.campsiteIdFrom("hello world"))
        assertNull(CampsiteQr.campsiteIdFrom(""))
        assertNull(CampsiteQr.campsiteIdFrom(CampsiteQr.PREFIX))
        assertNull(CampsiteQr.campsiteIdFrom(null))
    }
}
