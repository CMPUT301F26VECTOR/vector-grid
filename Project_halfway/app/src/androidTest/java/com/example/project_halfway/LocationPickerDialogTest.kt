package com.example.project_halfway

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LocationPickerDialog, where an owner drops a pin for their campsite
 * (US 02.01.02, 02.01.03). The map is FakePickerMap, so these don't need Google Maps.
 */
@RunWith(AndroidJUnit4::class)
class LocationPickerDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var saved: LatLng? = null
    private var dismissed = false

    // Opens the picker, starting with the given pin (or none)
    private fun showPicker(initial: LatLng?) {
        composeRule.setContent {
            CompositionLocalProvider(LocalLocationPickerMap provides FakePickerMap.map) {
                LocationPickerDialog(
                    initial = initial,
                    onSave = { point, _ -> saved = point },
                    onDismiss = { dismissed = true }
                )
            }
        }
    }

    @Test
    fun noPinYet_saveIsOff() {
        showPicker(initial = null)

        composeRule.onNodeWithTag("picked_location_text").assertTextEquals("No pin yet")
        composeRule.onNodeWithTag("location_save_button").assertIsNotEnabled()
    }

    @Test
    fun droppingAPin_showsItsCoordinates_andSaveGivesThePoint() {
        showPicker(initial = null)

        composeRule.onNodeWithTag("fake_map_pick").performClick()
        composeRule.onNodeWithTag("picked_coordinates_text").assertTextEquals("52.85070, -118.07700")
        composeRule.onNodeWithTag("location_save_button").assertIsEnabled().performClick()

        assertEquals(FakePickerMap.JASPER, saved)
    }

    @Test
    fun startingPin_isShown() {
        showPicker(initial = LatLng(51.1784, -115.5708))

        composeRule.onNodeWithTag("picked_coordinates_text").assertTextEquals("51.17840, -115.57080")
        composeRule.onNodeWithTag("location_save_button").assertIsEnabled()
    }

    @Test
    fun backArrow_closesWithoutSaving() {
        showPicker(initial = null)

        composeRule.onNodeWithTag("location_back_button").performClick()

        assertTrue(dismissed)
        assertNull(saved)
    }
}
