package com.example.project_halfway

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * UI tests for LotterySettingsForm (US 02.04.03, US 02.04.04, US 02.04.05, US 02.04.07).
 * The test holds the settings itself, the way the create-lottery screen will.
 */
@RunWith(AndroidJUnit4::class)
class LotterySettingsFormUITest {

    @get:Rule
    val composeRule = createComposeRule()

    private var settings by mutableStateOf(LotterySettings())

    private fun picked(day: String): Long =
        LocalDate.parse(day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun showForm() {
        composeRule.setContent {
            LotterySettingsForm(settings, onSettingsChange = { settings = it })
        }
    }

    @Test
    fun newForm_showsNoDatesAndOneWinner() {
        showForm()
        composeRule.onNodeWithTag("registration_dates").assertTextEquals("Not set")
        composeRule.onNodeWithTag("booking_dates").assertTextEquals("Not set")
        composeRule.onNodeWithTag("winners_field").assertIsDisplayed()
        assertEquals("1", settings.numberOfWinners)
    }

    @Test
    fun typingNumbers_updatesTheSettings() {
        showForm()
        composeRule.onNodeWithTag("winners_field").performTextClearance()
        composeRule.onNodeWithTag("winners_field").performTextInput("4")
        composeRule.onNodeWithTag("capacity_field").performTextInput("20")
        assertEquals("4", settings.numberOfWinners)
        assertEquals("20", settings.waitlistCapacity)
    }

    @Test
    fun pickedDates_areShownAndBookingCanBeCleared() {
        settings = LotterySettings(
            registrationStart = picked("2026-11-10"),
            registrationEnd = picked("2026-11-17"),
            bookingStart = picked("2026-12-01"),
            bookingEnd = picked("2026-12-05")
        )
        showForm()
        composeRule.onNodeWithTag("registration_dates").assertTextEquals("Nov 10, 2026 to Nov 17, 2026")
        composeRule.onNodeWithTag("booking_dates").assertTextEquals("Dec 1, 2026 to Dec 5, 2026")

        composeRule.onNodeWithTag("booking_clear").performClick()
        composeRule.onNodeWithTag("booking_dates").assertTextEquals("Not set")
        assertNull(settings.bookingStart)
    }

    @Test
    fun chooseDates_opensTheCalendar() {
        showForm()
        composeRule.onNodeWithTag("registration_button").performClick()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithTag("registration_dates").assertTextEquals("Not set")
    }
}
