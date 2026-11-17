package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the owner's Reservations screen (US 02.07.01, 02.07.02,
 * 02.07.03) and the Reservations card on the owner home. The reservations are
 * given directly, so no Firestore is needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
@RunWith(AndroidJUnit4::class)
class OwnerReservationsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = "2026-07-15"

    private fun row(id: String, name: String, start: String, end: String, status: String = Reservation.STATUS_ACTIVE) =
        OwnerReservationRow(
            Reservation(
                id = id, entrantId = "e_$id", lotteryId = "l1", campsiteId = "c1",
                startDate = start, endDate = end, createdAt = 1L, status = status,
                cancelledAt = if (status == Reservation.STATUS_ACTIVE) 0L else 1_780_000_000_000L,
                guests = 3
            ),
            entrantName = name,
            campsiteName = "Site 56A"
        )

    private val rows = listOf(
        row("active1", "Alex Smith", "2026-07-20", "2026-07-22"),
        row("past1", "Jordan Lee", "2026-05-01", "2026-05-03"),
        row("cancel1", "Sam Patel", "2026-08-01", "2026-08-03", Reservation.STATUS_CANCELLED),
        row("reject1", "Riley Chen", "2026-08-10", "2026-08-12", Reservation.STATUS_REJECTED)
    )

    @Test
    fun activeTabShowsUpcomingStaysWithDatesAndGuests() {
        composeRule.setContent { OwnerReservationsList(rows = rows, today = today) }

        composeRule.onNodeWithTag("reservation_card_active1").assertIsDisplayed()
        composeRule.onNodeWithText("Alex Smith").assertIsDisplayed()
        composeRule.onNodeWithText("2026-07-20 to 2026-07-22").assertIsDisplayed()
        composeRule.onNodeWithText("Guests: 3").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_card_past1").assertDoesNotExist()
    }

    @Test
    fun pastTabShowsOnlyFinishedStays() {
        composeRule.setContent { OwnerReservationsList(rows = rows, today = today) }

        composeRule.onNodeWithTag("reservations_tab_past").performClick()

        composeRule.onNodeWithTag("reservation_card_past1").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_card_active1").assertDoesNotExist()
    }

    @Test
    fun cancelledTabSaysWhoCancelled() {
        composeRule.setContent { OwnerReservationsList(rows = rows, today = today) }

        composeRule.onNodeWithTag("reservations_tab_cancelled").performClick()

        composeRule.onNodeWithText("Cancelled by entrant").assertIsDisplayed()
        composeRule.onNodeWithText("Rejected by owner").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_card_active1").assertDoesNotExist()
    }

    @Test
    fun anEmptyTabSaysSo() {
        composeRule.setContent { OwnerReservationsList(rows = emptyList(), today = today) }

        composeRule.onNodeWithTag("reservations_empty").assertIsDisplayed()
    }

    @Test
    fun theScreenShowsWhatTheLoaderGives() {
        // This screen uses the real date, so the stay is far in the future to always be active
        val future = listOf(row("future1", "Morgan Diaz", "2099-01-01", "2099-01-03"))
        composeRule.setContent {
            OwnerReservationsScreen(ownerId = "owner1", loadReservations = { _, onResult -> onResult(future) })
        }

        composeRule.onNodeWithText("Morgan Diaz").assertIsDisplayed()
    }

    @Test
    fun ownerHomeReservationsCardOpensReservations() {
        var pressed = false
        composeRule.setContent { OwnerHomeScreenUI(onSignOut = {}, onReservationsClick = { pressed = true }) }

        composeRule.onNodeWithTag("reservations_card").performClick()

        assertTrue(pressed)
    }
}
