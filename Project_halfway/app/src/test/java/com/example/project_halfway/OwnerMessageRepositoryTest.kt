package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for turning a message into Firestore values and back (US 01.14.01). */
class OwnerMessageRepositoryTest {

    @Test
    fun toMap_savesEveryFieldAndTrimsTheText() {
        val message = OwnerMessage("", "res1", "camp1", "entrant1", "  Hello owner  ", 5L)
        val map = OwnerMessageRepository.toMap(message)
        assertEquals("res1", map["reservationId"])
        assertEquals("camp1", map["campsiteId"])
        assertEquals("entrant1", map["entrantId"])
        assertEquals("Hello owner", map["text"])
        assertEquals(5L, map["createdAt"])
    }

    @Test
    fun fromMap_buildsTheMessageBack() {
        val map = mapOf<String, Any>(
            "reservationId" to "res1",
            "campsiteId" to "camp1",
            "entrantId" to "entrant1",
            "text" to "Hello owner",
            "createdAt" to 5L
        )
        val message = OwnerMessageRepository.fromMap("doc1", map)
        assertEquals("doc1", message.id)
        assertEquals("res1", message.reservationId)
        assertEquals("camp1", message.campsiteId)
        assertEquals("entrant1", message.entrantId)
        assertEquals("Hello owner", message.text)
        assertEquals(5L, message.createdAt)
    }

    @Test
    fun fromMap_missingValuesBecomeBlanksAndZero() {
        val message = OwnerMessageRepository.fromMap("doc2", emptyMap())
        assertEquals("", message.text)
        assertEquals("", message.campsiteId)
        assertEquals(0L, message.createdAt)
    }
}