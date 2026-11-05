package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/**
 * The full campsite details screen, styled like the mockup: a green "Details"
 * header with a back arrow, then a card with the campsite's rating, name,
 * location, a Google Map with the campsite's pin (with Directions and Open in
 * Google Maps buttons), description, services, price per night, a calendar
 * and a Join lottery / Join waitlist button.
 *
 * Used for US 01.01.05 (view campsite details) and US 02.01.04 (owner views
 * their campsite's details).
 *
 * The bottom of the card is an "action area". A screen can fill it with its
 * own content: Browse puts LotterySection there so entrants can join the
 * lottery (US 01.01.06), and the owner's screens put a View waitlist button
 * there. With no actionArea, the card shows its Join button.
 *
 * Outstanding issues: the calendar is not built yet, and the Join button on
 * its own only calls onJoinClick.
 *
 * @param campsite the campsite to show
 * @param isLotteryActive true to show "Join lottery", false to show "Join waitlist"
 * @param onBackClick called when the back arrow is pressed
 * @param onJoinClick called when the Join button is pressed
 * @param actionArea content to show instead of the Join button, or null to
 * show the Join button
 */
@Composable
fun CampsiteDetailedCard(
    campsite: CampsiteModel,
    isLotteryActive: Boolean, // True for "Join lottery", false for "Join waitlist"
    onBackClick: () -> Unit,
    onJoinClick: () -> Unit,
    actionArea: (@Composable () -> Unit)? = null
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff566C11))
            // scrolls, so a long description or the lottery section never gets cut off
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .clickable(onClick = onBackClick)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Text(
                text = "Details",
                fontFamily = FontFamily.SansSerif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Container Card
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campsite & Campground Name
                Text(
                    text = campsite.getCampsiteName() + " - " + campsite.getCampgroundName(),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location
                Text(
                    text = campsite.getCampsiteLocation(),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = Color(0xFF424242),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Map of the campsite's pin, with buttons that open Google Maps
                CampsiteMapSection(campsite)

                Spacer(modifier = Modifier.height(12.dp))

                // Rating Stars
                RatingStars(campsite)

                Spacer(modifier = Modifier.height(20.dp))

                // Specifications
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Maximum number of guests: " + campsite.getOccupancy(),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Type: " + campsite.getType(),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Services: " + campsite.getServices().joinToString(", "),
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Rate: " + String.format(Locale.getDefault(), "%.2f", campsite.getPricePerNight()) + "per night",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Available Dates Section
                Text(
                    text = "Available Dates",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Calendar
                CampsiteCalendar(availableDateRanges = campsite.getAvailableDateRanges())

                Spacer(modifier = Modifier.height(20.dp))

                // Action area: the screen's own content if it gave one, otherwise the Join button
                if (actionArea != null) {
                    actionArea()
                } else {
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier
                            .width(200.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xff566C11)
                        )
                    ) {
                        Text(
                            text = if (isLotteryActive) "Join lottery" else "Join waitlist",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calendar component for the campsite details screen showing available dates.
 *
 * Displays date ranges set by the campsite owner (US 01.01.05).
 * Highlights the start, end, and intermediate dates of available ranges.
 *
 * @param availableDateRanges list of date range maps containing "start" and "end" date strings (YYYY-MM-DD)
 * @param modifier layout options from the screen that shows it
 */
@Composable
fun CampsiteCalendar(
    modifier: Modifier = Modifier,
    availableDateRanges: List<Map<String, String>> = emptyList()
) {
    val monthNames = remember { listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec") }
    val daysOfWeek = remember { listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa") }

    // Parse date ranges into a list of (start, end) LocalDate Pairs inside the function.
    val parsedRanges = remember(availableDateRanges) {
        availableDateRanges.mapNotNull { range ->
            val startStr = range["start"]
            val endStr = range["end"]
            if (startStr != null && endStr != null) {
                try {
                    LocalDate.parse(startStr) to LocalDate.parse(endStr)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    // Determine initial month and year from owner's first available date range (defaulting to Sep 2025).
    val initialYearMonth = remember(parsedRanges) {
        val firstDate = parsedRanges.firstOrNull()?.first
        if (firstDate != null) YearMonth.from(firstDate) else YearMonth.of(2025, 9)
    }
    // Interactive states for currently displayed month and dropdown menus
    var currentYearMonth by remember(initialYearMonth) { mutableStateOf(initialYearMonth) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    //Container Card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: month/year navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Month Button
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color(0xFF424242)
                    )
                }

                // Center Dropdowns for Month & Year Selection
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Dropdown Pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                            color = Color.White,
                            modifier = Modifier.clickable {
                                monthMenuExpanded = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = monthNames[currentYearMonth.monthValue - 1],
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Month",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF666666)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = {
                                monthMenuExpanded = false
                            }
                        ) {
                            monthNames.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        currentYearMonth = currentYearMonth.withMonth(index + 1)
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Dropdown Pill
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                            color = Color.White,
                            modifier = Modifier.clickable {
                                yearMenuExpanded = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentYearMonth.year.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Year",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFF666666)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = yearMenuExpanded,
                            onDismissRequest = {
                                yearMenuExpanded = false
                            }
                        ) {
                            (2024..2030).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year.toString()) },
                                    onClick = {
                                        currentYearMonth = currentYearMonth.withYear(year)
                                        yearMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                // Next Month Button
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = Color(0xFF424242)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of the Week Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid math calculations for rows and columns based on month start offset
            val daysInMonth = currentYearMonth.lengthOfMonth()
            val startOffset = currentYearMonth.atDay(1).dayOfWeek.value % 7
            val totalCells = startOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until totalRows) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1

                            Box(modifier = Modifier.weight(1f).height(36.dp), contentAlignment = Alignment.Center) {
                                when {
                                    // Valid days belonging to the current month
                                    dayNumber in 1..daysInMonth -> {
                                        val date = currentYearMonth.atDay(dayNumber)

                                        // Determine if date matches availability ranges or preview mock defaults
                                        val (isStart, isEnd, isInRange) = if (parsedRanges.isEmpty() && availableDateRanges.isEmpty() && currentYearMonth == YearMonth.of(2025, 9)) {
                                            Triple(dayNumber == 9, dayNumber == 13, dayNumber in 10..12)
                                        } else {
                                            Triple(
                                                parsedRanges.any { it.first == date },
                                                parsedRanges.any { it.second == date },
                                                parsedRanges.any { date.isAfter(it.first) && date.isBefore(it.second) }
                                            )
                                        }
                                        // Style configuration based on range status
                                        val bgColor = when {
                                            isStart || isEnd -> Color(0xFF333333)
                                            isInRange -> Color(0xFFEEEEEE)
                                            else -> Color.Transparent
                                        }
                                        val textColor = if (isStart || isEnd) Color.White else Color(0xFF212121)

                                        Box(
                                            modifier = Modifier.size(32.dp).background(bgColor, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                fontSize = 14.sp,
                                                fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal,
                                                color = textColor
                                            )
                                        }
                                    }
                                    // Trailing days from the following month
                                    dayNumber > daysInMonth -> {
                                        Text(text = (dayNumber - daysInMonth).toString(), fontSize = 14.sp, color = Color(0xFFBDBDBD))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The map part of the details card: a small Google Map with the campsite's pin, and
 * Directions and Open in Google Maps buttons (US 01.01.05, 02.01.04). A campsite
 * with no map location shows "No location yet" and no buttons.
 *
 * @param campsite the campsite whose location to show
 */
@Composable
fun CampsiteMapSection(campsite: CampsiteModel) {
    val context = LocalContext.current
    val position = campsite.mapPosition()

    CampsiteMapPreview(
        position = position,
        modifier = Modifier.fillMaxWidth().height(180.dp),
        onClick = { position?.let { GoogleMapsLinks.openPin(context, it) } }
    )

    if (position != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { GoogleMapsLinks.openDirections(context, position) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11)),
                modifier = Modifier.testTag("directions_button")
            ) {
                Text("Directions")
            }
            OutlinedButton(
                onClick = { GoogleMapsLinks.openPin(context, position) },
                modifier = Modifier.testTag("open_in_maps_button")
            ) {

            }
        }
    }
}
