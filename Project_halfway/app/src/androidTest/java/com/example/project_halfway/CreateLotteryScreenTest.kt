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
 * UI tests for CreateLotteryScreen (US 02.04.01, US 02.04.03, US 02.04.04,
 * US 02.04.05, US 02.04.07). Saving is replaced with a fake, so no Firestore
 * is needed.
 */
@RunWith(AndroidJUnit4::class)
class CreateLotteryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // what a Material date picker gives for a day: midnight UTC in milliseconds
    private fun picked(day: LocalDate): Long =
        day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // registration next week, booking the week after, so it is always valid
    private val today = LocalDate.now()
    private val goodSettings = LotterySettings(
        registrationStart = picked(today.plusDays(1)),
        registrationEnd = picked(today.plusDays(7)),
        bookingStart = picked(today.plusDays(10)),
        bookingEnd = picked(today.plusDays(12)),
        numberOfWinners = "3",
        waitlistCapacity = "40"
    )

    // Makes a simple campsite with the given id and name
    private fun makeCampsite(id: String, name: String): CampsiteModel {
        return CampsiteModel(
            campsiteName = name,
            campgroundName = "",
            location = "",
            longitude = 0.0,
            latitude = 0.0,
            occupancy = 4,
            userRating = 0.0,
            description = "",
            imageUrl = "",
            id = id
        )
    }

    @Test
    fun saveWithoutDates_showsErrorAndSavesNothing() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryScreen(campsiteId = "camp1", save = { lottery, onDone ->
                saved = lottery
                onDone(true)
            })
        }

        composeRule.onNodeWithTag("create_lottery_save").performClick()

        composeRule.onNodeWithTag("create_lottery_message")
            .assertTextEquals("Please set a registration period.")
        assertNull(saved)
    }

    @Test
    fun saveGoodSettings_savesLotteryForTheCampsite() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryScreen(
                campsiteId = "camp1",
                initialSettings = goodSettings,
                save = { lottery, onDone ->
                    saved = lottery
                    onDone(true)
                }
            )
        }

        composeRule.onNodeWithTag("create_lottery_save").performClick()

        composeRule.onNodeWithTag("create_lottery_message").assertTextEquals("Lottery created")
        composeRule.onNodeWithTag("create_lottery_save").assertIsNotEnabled()
        val lottery = saved!!
        assertEquals("camp1", lottery.campsiteId)
        assertEquals(3, lottery.numberOfWinners)
        assertEquals(40, lottery.waitlistCapacity)
        assertEquals(goodSettings.bookingStart, lottery.bookingStartDate)
    }

    @Test
    fun failedSave_showsErrorAndLetsOwnerTryAgain() {
        composeRule.setContent {
            CreateLotteryScreen(
                campsiteId = "camp1",
                initialSettings = goodSettings,
                save = { _, onDone -> onDone(false) }
            )
        }

        composeRule.onNodeWithTag("create_lottery_save").performClick()

        composeRule.onNodeWithTag("create_lottery_message")
            .assertTextEquals("Could not save, please try again.")
        composeRule.onNodeWithTag("create_lottery_save").assertIsEnabled()
    }

    @Test
    fun blankCampsite_isRefused() {
        var called = false
        composeRule.setContent {
            CreateLotteryScreen(
                campsiteId = "",
                initialSettings = goodSettings,
                save = { _, onDone -> called = true; onDone(true) }
            )
        }

        composeRule.onNodeWithTag("create_lottery_save").performClick()

        composeRule.onNodeWithTag("create_lottery_message")
            .assertTextEquals("Please pick a campsite first.")
        assertEquals(false, called)
    }

    @Test
    fun title_showsTheCampsiteName() {
        composeRule.setContent {
            CreateLotteryScreen(campsiteId = "camp1", campsiteName = "Site 56A")
        }

        composeRule.onNodeWithTag("create_lottery_title")
            .assertTextEquals("Create Lottery for Site 56A")
    }
    @Test
    fun tickingASecondCampsite_savesLotteryWithBoth() {
        var saved: Lottery? = null
        composeRule.setContent {
            CreateLotteryScreen(
                campsiteId = "camp1",
                campsiteName = "Lakeside",
                initialCampsites = listOf(
                    makeCampsite("camp1", "Lakeside"),
                    makeCampsite("camp2", "Hilltop")
                ),
                initialSettings = goodSettings,
                save = { lottery, onDone ->
                    saved = lottery
                    onDone(true)
                }
            )
        }

        composeRule.onNodeWithTag("campsite_check_camp2").performClick()
        composeRule.onNodeWithTag("create_lottery_save").performClick()

        assertEquals(listOf("camp1", "camp2"), saved!!.campsiteIds)
    }

    @Test
    fun untickingEveryCampsite_isRefused() {
        var called = false
        composeRule.setContent {
            CreateLotteryScreen(
                campsiteId = "camp1",
                initialCampsites = listOf(makeCampsite("camp1", "Lakeside")),
                initialSettings = goodSettings,
                save = { _, onDone -> called = true; onDone(true) }
            )
        }

        composeRule.onNodeWithTag("campsite_check_camp1").performClick()
        composeRule.onNodeWithTag("create_lottery_save").performClick()

        composeRule.onNodeWithTag("create_lottery_message")
            .assertTextEquals("Please pick at least one campsite.")
        assertEquals(false, called)
    }
}
