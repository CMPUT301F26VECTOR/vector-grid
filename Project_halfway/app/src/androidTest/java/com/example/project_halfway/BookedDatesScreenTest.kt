package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

/**
 * UI tests for the booked dates calendar (US 02.06.01). The reservations are
 * given directly, so no Firestore is needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
@RunWith(AndroidJUnit4::class)
class BookedDatesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun row(start: String, end: String, status: String = Reservation.STATUS_ACTIVE) = OwnerReservationRow(
        Reservation(
            entrantId = "e1", lotteryId = "l1", campsiteId = "c1",
            startDate = start, endDate = end, createdAt = 1L, status = status
        ),
        entrantName = "Alex Smith",
        campsiteName = "Site 56A"
    )

    @Test
    fun bookedNightsAreMarkedOnTheCalendar() {
        composeRule.setContent {
            BookedDatesCalendar("Site 56A", listOf(row("2026-07-03", "2026-07-04")), startMonth = YearMonth.of(2026, 7))
        }

        composeRule.onNodeWithTag("booked_month_title").assertTextEquals("July 2026")
        composeRule.onNodeWithTag("booked_day_2026-07-03").assertIsDisplayed()
        composeRule.onNodeWithTag("booked_day_2026-07-04").assertIsDisplayed()
        composeRule.onNodeWithTag("day_2026-07-05").assertIsDisplayed()
        composeRule.onNodeWithText("2026-07-03 to 2026-07-04 · Alex Smith").assertExists()
    }

    @Test
    fun cancelledStaysAreNotMarked() {
        composeRule.setContent {
            BookedDatesCalendar(
                "Site 56A",
                listOf(row("2026-07-03", "2026-07-04", Reservation.STATUS_CANCELLED)),
                startMonth = YearMonth.of(2026, 7)
            )
        }

        composeRule.onNodeWithTag("day_2026-07-03").assertIsDisplayed()
        composeRule.onNodeWithTag("booked_empty").assertExists()
    }

    @Test
    fun theArrowsMoveBetweenMonths() {
        composeRule.setContent {
            BookedDatesCalendar("Site 56A", listOf(row("2026-08-10", "2026-08-11")), startMonth = YearMonth.of(2026, 7))
        }

        composeRule.onNodeWithTag("booked_next").performClick()
        composeRule.onNodeWithTag("booked_month_title").assertTextEquals("August 2026")
        composeRule.onNodeWithTag("booked_day_2026-08-10").assertIsDisplayed()

        composeRule.onNodeWithTag("booked_prev").performClick()
        composeRule.onNodeWithTag("booked_month_title").assertTextEquals("July 2026")
    }
}
