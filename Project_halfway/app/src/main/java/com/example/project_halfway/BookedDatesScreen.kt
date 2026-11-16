package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Booked dates screen for one of the owner's campsites (US 02.06.01).
 *
 * Shows a month calendar where every booked night has a green circle, with
 * arrows to move between months, and under it a list of the stays
 * ("2026-06-03 to 2026-06-05 · Alex"). Cancelled and rejected reservations
 * are left out, so their dates show as free again. The reservations are loaded
 * again every time the screen opens, so new bookings and cancellations show up.
 *
 * Outstanding issues: the calendar starts on the current month; the owner has
 * to press the arrows to reach far-off bookings.
 *
 * **AI assistance (Samarth Sathya Karthik):** this code was implemented using
 * Claude AI; Samarth tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @param campsite the campsite whose bookings to show
 * @param onBackClick called when the back arrow is pressed
 * @param loadReservations loads the campsite's reservations with names;
 * Firestore by default, and replaced in tests
 */
@Composable
fun BookedDatesScreen(
    campsite: CampsiteModel,
    onBackClick: () -> Unit = {},
    loadReservations: (CampsiteModel, (List<OwnerReservationRow>) -> Unit) -> Unit = { site, onResult ->
        // Load this one campsite's reservations, then the entrants' names
        ReservationRepository.getForCampsites(listOf(site.getId())) { reservations ->
            addNames(reservations, mapOf(site.getId() to site.getCampsiteName()), onResult)
        }
    }
) {
    var rows by remember { mutableStateOf(listOf<OwnerReservationRow>()) }

    // Runs once when the screen opens
    LaunchedEffect(campsite.getId()) {
        loadReservations(campsite) { result -> rows = result }
    }

    BookedDatesCalendar(
        campsiteName = campsite.getCampsiteName(),
        rows = rows,
        onBackClick = onBackClick
    )
}

/**
 * Works out every booked night from a list of reservations.
 * Each reservation books every day from its startDate to its endDate.
 * Cancelled and rejected reservations are skipped, so those days are free.
 *
 * It has no Android code, so it is unit tested in BookedDatesTest.
 *
 * @param reservations the campsite's reservations
 * @return the booked days as YYYY-MM-DD text
 */
fun bookedDays(reservations: List<Reservation>): Set<String> {
    val days = mutableSetOf<String>()
    for (r in reservations) {
        // A cancelled stay doesn't take up any dates
        if (r.isCancelled()) continue

        // Skip reservations with missing or broken dates instead of crashing
        val start = try { LocalDate.parse(r.startDate) } catch (e: Exception) { continue }
        val end = try { LocalDate.parse(r.endDate) } catch (e: Exception) { continue }

        // Walk from the first day to the last, one day at a time
        var day = start
        while (!day.isAfter(end)) {
            days.add(day.toString())
            day = day.plusDays(1)
        }
    }
    return days
}

/**
 * Lays out one month as calendar rows of 7 days, Sunday first.
 * Empty spots before the 1st and after the last day are null.
 *
 * @param month the month to lay out
 * @return a list of weeks; each week is 7 days or nulls
 */
fun monthWeeks(month: YearMonth): List<List<LocalDate?>> {
    val cells = mutableListOf<LocalDate?>()

    // DayOfWeek.value is 1 (Monday) to 7 (Sunday); % 7 turns Sunday into 0,
    // which is how many blank spots go before the 1st
    val blanks = month.atDay(1).dayOfWeek.value % 7
    repeat(blanks) { cells.add(null) }

    for (day in 1..month.lengthOfMonth()) {
        cells.add(month.atDay(day))
    }

    // Fill the last week up to 7
    while (cells.size % 7 != 0) cells.add(null)

    return cells.chunked(7)
}

/**
 * Draws the booked dates screen. It does not talk to Firestore, so it can be
 * tested on its own (BookedDatesScreenTest).
 *
 * @param campsiteName shown in the title
 * @param rows the campsite's reservations with names
 * @param onBackClick called when the back arrow is pressed
 * @param startMonth the month shown first; the current month by default
 */
@Composable
fun BookedDatesCalendar(
    campsiteName: String,
    rows: List<OwnerReservationRow>,
    onBackClick: () -> Unit = {},
    startMonth: YearMonth = YearMonth.now()
) {
    // Which month the calendar shows
    var month by remember { mutableStateOf(startMonth) }

    // Every booked night, worked out once per list
    val booked = remember(rows) { bookedDays(rows.map { it.reservation }) }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 16.dp, bottom = 16.dp)
    ) {
        GreenScreenHeader(title = "Booked Dates", onBackClick = onBackClick)
        Spacer(Modifier.height(HeaderGap))

        WhitePanel(modifier = Modifier.padding(start = 8.dp).weight(1f), scrollable = true) {
            Text(campsiteName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // Month title with arrows on each side
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = month.minusMonths(1) }, modifier = Modifier.testTag("booked_prev")) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month", tint = AppGreen)
                }
                Text(
                    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).testTag("booked_month_title"),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { month = month.plusMonths(1) }, modifier = Modifier.testTag("booked_next")) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month", tint = AppGreen)
                }
            }

            // Day-of-week letters
            Row(modifier = Modifier.fillMaxWidth()) {
                for (letter in listOf("S", "M", "T", "W", "T", "F", "S")) {
                    Text(
                        letter,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF5F6368)
                    )
                }
            }

            // The days, one row per week
            for (week in monthWeeks(month)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in week) {
                        DayCell(day = day, isBooked = day != null && day.toString() in booked, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Legend, so the owner knows what the circles mean
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(14.dp).background(AppGreen, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("Booked", color = Color(0xFF5F6368))
            }

            // The stays, written out under the calendar
            Spacer(Modifier.height(16.dp))
            PanelLabel("Bookings", first = true)
            val stays = rows.filter { !it.reservation.isCancelled() }.sortedBy { it.reservation.startDate }
            if (stays.isEmpty()) {
                Text("No dates are booked yet.", modifier = Modifier.testTag("booked_empty"))
            } else {
                for (row in stays) {
                    Text(
                        "${row.reservation.startDate} to ${row.reservation.endDate} · ${row.entrantName}",
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * One square in the calendar: the day number, inside a green circle if it is booked.
 *
 * @param day the date, or null for an empty spot
 * @param isBooked true to draw the green circle
 * @param modifier sets the cell's width
 */
@Composable
private fun DayCell(day: LocalDate?, isBooked: Boolean, modifier: Modifier) {
    // Booked days get a tag of their own so tests can find them
    val tag = when {
        day == null -> "day_empty"
        isBooked -> "booked_day_$day"
        else -> "day_$day"
    }
    Box(
        modifier = modifier.aspectRatio(1f).padding(3.dp).testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isBooked) AppGreen else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    day.dayOfMonth.toString(),
                    color = if (isBooked) Color.White else Color(0xFF1C1B1F),
                    fontWeight = if (isBooked) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
