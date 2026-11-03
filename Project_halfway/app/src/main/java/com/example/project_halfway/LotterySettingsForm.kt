package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * What the owner has entered in LotterySettingsForm, before it becomes a Lottery.
 * Has no Android or Firebase code, so it can be unit tested (see LotterySettingsFormTest).
 *
 * Dates are the values a Material date picker gives: midnight UTC of the
 * picked day, in milliseconds, or null if not picked yet. The two numbers are
 * kept as the text the owner typed, so the boxes can be empty or half typed.
 *
 * Used for US 02.04.03, US 02.04.04, US 02.04.05 and US 02.04.07.
 *
 * Outstanding issues: registration opens at the start of its first day and
 * closes at the end of its last day; the owner can't pick a time of day.
 *
 * @property registrationStart first day entrants can join the waiting list
 * @property registrationEnd last day entrants can join the waiting list
 * @property bookingStart first day winners can stay, or null for no booking period
 * @property bookingEnd last day winners can stay, or null for no booking period
 * @property numberOfWinners text in the "Number of winners" box
 * @property waitlistCapacity text in the "Waiting list limit" box; empty means no limit
 */
data class LotterySettings(
    val registrationStart: Long? = null,
    val registrationEnd: Long? = null,
    val bookingStart: Long? = null,
    val bookingEnd: Long? = null,
    val numberOfWinners: String = "1",
    val waitlistCapacity: String = ""
) {

    /**
     * Builds the Lottery these settings describe.
     * Registration runs from the start of its first day to the end of its last
     * day in the owner's time zone. Text that isn't a whole number becomes a
     * value that firstSettingsError() rejects.
     *
     * @param campsiteId id of the campsite the lottery is for
     * @param id Firestore id; leave empty so LotteryRepository.save makes a new one
     * @param zone the owner's time zone
     * @return a new Lottery with an empty waiting list
     */
    fun toLottery(
        campsiteId: String,
        id: String = "",
        zone: ZoneId = ZoneId.systemDefault()
    ): Lottery {
        val capacityText = waitlistCapacity.trim()
        return Lottery(
            id = id,
            campsiteId = campsiteId,
            startDate = registrationStart?.let { startOfDay(it, zone) } ?: 0L,
            endDate = registrationEnd?.let { endOfDay(it, zone) } ?: 0L,
            numberOfWinners = numberOfWinners.trim().toIntOrNull() ?: 0,
            bookingStartDate = bookingStart ?: 0L,
            bookingEndDate = bookingEnd ?: 0L,
            waitlistCapacity = if (capacityText.isEmpty()) null else capacityText.toIntOrNull() ?: 0
        )
    }

    /**
     * Checks the settings, using the same rules as Lottery.firstSettingsError().
     *
     * @param now the current time in milliseconds
     * @param zone the owner's time zone
     * @return a message to show the owner, or null if the settings can be saved
     */
    fun firstError(
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): String? {
        val winners = numberOfWinners.trim()
        if (winners.isNotEmpty() && winners.toIntOrNull() == null) {
            return "The number of winners must be a whole number."
        }
        val capacity = waitlistCapacity.trim()
        if (capacity.isNotEmpty() && capacity.toIntOrNull() == null) {
            return "The waiting list limit must be a whole number, or leave it empty."
        }
        return toLottery("", zone = zone).firstSettingsError(now, zone)
    }

    /** Turns date picker values into times and text; used by the form and by toLottery. */
    companion object {
        /** The picked day (midnight UTC) as the start of that day in the owner's zone. */
        fun startOfDay(pickerMillis: Long, zone: ZoneId): Long {
            return pickedDay(pickerMillis).atStartOfDay(zone).toInstant().toEpochMilli()
        }

        /** The picked day (midnight UTC) as the last millisecond of that day in the owner's zone. */
        fun endOfDay(pickerMillis: Long, zone: ZoneId): Long {
            return pickedDay(pickerMillis).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        }

        /** A picked range as text, e.g. "Nov 3, 2026 to Nov 10, 2026", or "Not set". */
        fun describe(start: Long?, end: Long?): String {
            if (start == null || end == null) return "Not set"
            val format = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
            return "${pickedDay(start).format(format)} to ${pickedDay(end).format(format)}"
        }

        private fun pickedDay(pickerMillis: Long): LocalDate {
            return Instant.ofEpochMilli(pickerMillis).atZone(ZoneOffset.UTC).toLocalDate()
        }
    }
}

/**
 * The lottery settings part of the create-lottery screen: registration period,
 * optional booking period, number of winners and optional waiting list limit.
 * It only shows and edits the settings; the screen that uses it keeps them
 * and decides when to save, for example:
 *
 * ```
 * var settings by remember { mutableStateOf(LotterySettings()) }
 * LotterySettingsForm(settings, onSettingsChange = { settings = it })
 * Button(onClick = {
 *     val error = settings.firstError()
 *     if (error == null) {
 *         LotteryRepository.save(settings.toLottery(campsiteId)) { worked -> ... }
 *     }
 * }) { Text("Save") }
 * ```
 *
 * Used for US 02.04.03, US 02.04.04, US 02.04.05 and US 02.04.07.
 *
 * Outstanding issues: it is used by CreateLotteryScreen, which was written
 * only to test it; the full create-lottery screen (US 02.04.01, US 02.04.02)
 * is Mejida's to complete.
 *
 * @param settings what the owner has entered so far
 * @param onSettingsChange called with the new settings whenever the owner changes something
 * @param modifier layout options from the screen that uses the form
 */
@Composable
fun LotterySettingsForm(
    settings: LotterySettings,
    onSettingsChange: (LotterySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    // which calendar pop-up is open: "registration", "booking" or null
    var picking by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {

        // US 02.04.03: registration period
        DateRangeRow(
            title = "Registration period",
            value = LotterySettings.describe(settings.registrationStart, settings.registrationEnd),
            tag = "registration",
            onPick = { picking = "registration" }
        )

        // US 02.04.04: booking period (optional)
        DateRangeRow(
            title = "Booking period (optional)",
            value = LotterySettings.describe(settings.bookingStart, settings.bookingEnd),
            tag = "booking",
            onPick = { picking = "booking" },
            onClear = if (settings.bookingStart != null || settings.bookingEnd != null) {
                { onSettingsChange(settings.copy(bookingStart = null, bookingEnd = null)) }
            } else null
        )

        // US 02.04.05: number of winners
        OutlinedTextField(
            value = settings.numberOfWinners,
            onValueChange = { onSettingsChange(settings.copy(numberOfWinners = it)) },
            label = { Text("Number of winners") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("winners_field")
        )

        // US 02.04.07: waiting list limit (optional)
        OutlinedTextField(
            value = settings.waitlistCapacity,
            onValueChange = { onSettingsChange(settings.copy(waitlistCapacity = it)) },
            label = { Text("Waiting list limit (optional)") },
            placeholder = { Text("No limit") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("capacity_field")
        )
    }

    when (picking) {
        "registration" -> PickDatesDialog(
            initialStart = settings.registrationStart,
            initialEnd = settings.registrationEnd,
            onDismiss = { picking = null },
            onPick = { start, end ->
                onSettingsChange(settings.copy(registrationStart = start, registrationEnd = end))
                picking = null
            }
        )
        "booking" -> PickDatesDialog(
            initialStart = settings.bookingStart,
            initialEnd = settings.bookingEnd,
            onDismiss = { picking = null },
            onPick = { start, end ->
                onSettingsChange(settings.copy(bookingStart = start, bookingEnd = end))
                picking = null
            }
        )
    }
}

/** One date range: its title, the picked dates, a Choose dates button and an optional Clear button. */
@Composable
private fun DateRangeRow(
    title: String,
    value: String,
    tag: String,
    onPick: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value, modifier = Modifier.weight(1f).testTag("${tag}_dates"))
        if (onClear != null) {
            TextButton(onClick = onClear, modifier = Modifier.testTag("${tag}_clear")) {
                Text("Clear")
            }
        }
        OutlinedButton(onClick = onPick, modifier = Modifier.testTag("${tag}_button")) {
            Text("Choose dates", color = Color(0xff566C11))
        }
    }
}

/** Calendar pop-up for picking a start and end day. OK only turns on once both are picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickDatesDialog(
    initialStart: Long?,
    initialEnd: Long?,
    onDismiss: () -> Unit,
    onPick: (Long, Long) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStart,
        initialSelectedEndDateMillis = initialEnd
    )
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = start != null && end != null,
                onClick = { if (start != null && end != null) onPick(start, end) }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(state = state, modifier = Modifier.height(480.dp), showModeToggle = false)
    }
}

/** Android Studio preview of LotterySettingsForm with a registration period picked. */
@Preview(showBackground = true)
@Composable
fun LotterySettingsFormPreview() {
    var settings by remember {
        mutableStateOf(LotterySettings(registrationStart = 1_793_664_000_000, registrationEnd = 1_794_268_800_000))
    }
    Column(modifier = Modifier.padding(16.dp)) {
        LotterySettingsForm(settings, onSettingsChange = { settings = it })
    }
}
