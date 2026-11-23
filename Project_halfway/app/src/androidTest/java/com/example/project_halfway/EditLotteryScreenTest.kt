package com.example.project_halfway

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * UI tests for EditLotteryScreen (US 02.04.06). Lotteries and campsites are
 * given directly and saving is a fake, so no Firestore is needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
@RunWith(AndroidJUnit4::class)
class EditLotteryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun picked(day: LocalDate): Long = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // Registration next week, so the lottery is always still editable
    private val today = LocalDate.now()
    private val settings = LotterySettings(
        registrationStart = picked(today.plusDays(1)),
        registrationEnd = picked(today.plusDays(7)),
        numberOfWinners = "2"
    )

    private fun lottery(id: String, drawn: Boolean): Lottery {
        val base = settings.toLottery("camp1", id = id)
        return Lottery(id, "camp1", base.startDate, base.endDate, numberOfWinners = 2, isDrawn = drawn)
    }

    private val campsite = CampsiteModel(
        campsiteName = "Site 56A", campgroundName = "", location = "", longitude = 0.0, latitude = 0.0,
        occupancy = 4, description = "", imageUrl = "", id = "camp1"
    )

    @Test
    fun savingAnEditWritesTheNewValues() {
        var savedId: String? = null
        var savedFields: Map<String, Any?> = emptyMap()

        composeRule.setContent {
            EditLotteryScreen(
                campsiteId = "camp1",
                initialLotteries = listOf(lottery("lot1", drawn = false)),
                initialCampsites = listOf(campsite),
                save = { id, fields, onDone -> savedId = id; savedFields = fields; onDone(true) }
            )
        }

        composeRule.onNodeWithTag("edit_lottery_pick_lot1").performClick()
        composeRule.onNodeWithTag("edit_lottery_save").assertIsEnabled().performClick()

        composeRule.onNodeWithTag("edit_lottery_message").assertTextEquals("Lottery updated")
        assertEquals("lot1", savedId)
        assertEquals(listOf("camp1"), savedFields["campsiteIds"])
        assertEquals(2, savedFields["numberOfWinners"])
    }

    @Test
    fun aDrawnLotteryIsLocked() {
        var saved = false
        composeRule.setContent {
            EditLotteryScreen(
                campsiteId = "camp1",
                initialLotteries = listOf(lottery("lot2", drawn = true)),
                initialCampsites = listOf(campsite),
                save = { _, _, onDone -> saved = true; onDone(true) }
            )
        }

        composeRule.onNodeWithTag("edit_lottery_pick_lot2").performClick()

        composeRule.onNodeWithTag("edit_lottery_save").assertIsNotEnabled()
        composeRule.onNodeWithTag("edit_lottery_message").assertTextEquals(DRAWN_MESSAGE)
        assertEquals(false, saved)
    }

    @Test
    fun unTickingEveryCampsiteIsRefused() {
        var savedId: String? = null
        composeRule.setContent {
            EditLotteryScreen(
                campsiteId = "camp1",
                initialLotteries = listOf(lottery("lot1", drawn = false)),
                initialCampsites = listOf(campsite),
                save = { id, _, onDone -> savedId = id; onDone(true) }
            )
        }

        composeRule.onNodeWithTag("edit_lottery_pick_lot1").performClick()
        composeRule.onNodeWithTag("edit_campsite_check_camp1").performClick()
        composeRule.onNodeWithTag("edit_lottery_save").performClick()

        composeRule.onNodeWithTag("edit_lottery_message").assertTextEquals("Please pick at least one campsite.")
        assertNull(savedId)
    }
}
