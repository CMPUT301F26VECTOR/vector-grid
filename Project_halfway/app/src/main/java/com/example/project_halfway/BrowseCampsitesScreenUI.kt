
package com.example.project_halfway

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val BrowseGreen = Color(0xFF526B0D)

/**
 * Displays campsite listings with search and filtering.
 *
 * US 01.01.01 - Search by name, campground, or location.
 * US 01.01.02 - Filter by location, occupancy, and dates.
 * US 01.01.03 - Display matching campsite listings.
 * US 01.01.05 - Tapping a campsite card opens its details.
 * US 01.01.06, 01.02.01, 01.02.02 - The details show LotterySection, so the
 * entrant can join or leave that campsite's lottery waitlist.
 *
 * Campsite data is retrieved from Firestore. If loading fails, a message
 * replaces the loading spinner.
 *
 * Outstanding issues: the tapped campsite is kept in memory only, so turning
 * the phone goes back to the list. The phone's back button closes the details
 * but is not handled on the list itself.
 *
 * @param onBackClick Returns to Entrant Home.
 * @param onFilterClick Optional callback when filters are opened.
 */
@Composable
fun BrowseCampsitesScreenUI(
    onBackClick: () -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    var campsites by remember {
        mutableStateOf<List<CampsiteModel>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    // Switch between Browse and Filter layouts.
    var showFilters by rememberSaveable {
        mutableStateOf(false)
    }

    // Currently applied filters.
    var locationFilter by rememberSaveable {
        mutableStateOf("")
    }

    var guestsFilter by rememberSaveable {
        mutableStateOf("")
    }

    var checkInFilter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var checkOutFilter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // The campsite whose card was tapped, or null while the list is showing.
    var selectedCampsite by remember {
        mutableStateOf<CampsiteModel?>(null)
    }

    // Shown instead of the list if Firestore could not be reached.
    var loadError by remember {
        mutableStateOf("")
    }

    // Retrieve campsite listings from Firebase.
    LaunchedEffect(Unit) {
        CampsiteRepository.getAll(
            onFailure = {
                loadError = "Could not load campsites. Check your connection and try again."
                isLoading = false
            },
            onResult = { loadedCampsites ->
                campsites = loadedCampsites
                isLoading = false
            }
        )
    }

    val search = remember { CampSearch() }

    // US 01.01.01: Search by name, campground, or location.
    val searchedCampsites = search.searchCampsites(
        campsites,
        searchText
    )

    // US 01.01.02: Filter by location and occupancy.
    val locationAndGuestResults = search.filterCampsites(
        searchedCampsites,
        locationFilter,
        guestsFilter.toIntOrNull()
    )

    // US 01.01.02: Apply selected date range.
    val results = if (
        checkInFilter != null &&
        checkOutFilter != null
    ) {
        search.filterByDates(
            locationAndGuestResults,
            LocalDate.parse(checkInFilter),
            LocalDate.parse(checkOutFilter)
        )
    } else {
        locationAndGuestResults
    }

    val chosen = selectedCampsite

    if (chosen != null) {
        // US 01.01.05: the tapped campsite's details (Mirah's card).
        // Its action area holds Parneet's LotterySection: join or leave the waitlist.
        BackHandler { selectedCampsite = null }

        CampsiteDetailedCard(
            campsite = chosen,
            isLotteryActive = true,
            onBackClick = { selectedCampsite = null },
            onJoinClick = {},
            actionArea = { LotterySection(chosen.getId()) }
        )
    } else if (showFilters) {
        // Display the Add Search Filters layout.
        CampFilterScreenUI(
            initialLocation = locationFilter,
            initialGuests = guestsFilter,
            initialCheckIn = checkInFilter,
            initialCheckOut = checkOutFilter,
            onBackClick = {
                showFilters = false
            },
            onApplyFilters = { location, guests, checkIn, checkOut ->
                locationFilter = location
                guestsFilter = guests
                checkInFilter = checkIn
                checkOutFilter = checkOut

                // Return to Browse with updated results.
                showFilters = false
            }
        )
    } else {
        // Display Browse Campsites.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrowseGreen)
                .padding(16.dp)
        ) {
            // Back arrow and title.
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Browse Campsites",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar and filter button.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    placeholder = {
                        Text("Search by area, campground")
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White
                    )
                )

                IconButton(
                    onClick = {
                        showFilters = true
                        onFilterClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter campsites",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // US 01.01.03: Display matching campsites.
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(20.dp),
                    color = Color.White
                )
            } else if (loadError.isNotEmpty()) {
                Text(
                    text = loadError,
                    color = Color.White,
                    modifier = Modifier.padding(20.dp)
                )
            } else if (results.isEmpty()) {
                Text(
                    text = "No campsites found",
                    color = Color.White,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { campsite ->
                        // Tapping a card opens its details (see the top of this function)
                        CampsiteDescriptionCard(campsite, onClick = { selectedCampsite = campsite })
                    }
                }
            }
        }
    }
}

/**
 * Add Search Filters layout for US 01.01.02.
 *
 * Allows entrants to select location, guest capacity and a check-in/check-out date range.
 *
 * Filter values are returned to Browse Campsites.
 *
 * @param initialLocation the location filter already chosen, shown in the box
 * @param initialGuests the guest count already chosen, as text
 * @param initialCheckIn the check-in date already chosen, or null
 * @param initialCheckOut the check-out date already chosen, or null
 * @param onBackClick called when the back arrow is pressed
 * @param onApplyFilters called with location, guests, check-in and check-out
 * when the filters are applied
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampFilterScreenUI(
    initialLocation: String,
    initialGuests: String,
    initialCheckIn: String?,
    initialCheckOut: String?,
    onBackClick: () -> Unit,
    onApplyFilters: (String, String, String?, String?) -> Unit
) {
    var location by rememberSaveable {
        mutableStateOf(initialLocation)
    }

    var guests by rememberSaveable {
        mutableStateOf(initialGuests)
    }

    // Convert a saved date to milliseconds for the calendar.
    fun dateToMillis(date: String?): Long? {
        return date?.let {
            LocalDate.parse(it)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }
    }

    // Convert a selected calendar date to YYYY-MM-DD.
    fun millisToDate(millis: Long?): String? {
        return millis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .toString()
        }
    }

    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = dateToMillis(initialCheckIn),
        initialSelectedEndDateMillis = dateToMillis(initialCheckOut)
    )

    // Prevent incomplete date selections from being applied.
    val datesValid =
        (datePickerState.selectedStartDateMillis == null &&
                datePickerState.selectedEndDateMillis == null) ||
                (datePickerState.selectedStartDateMillis != null &&
                        datePickerState.selectedEndDateMillis != null)

    // Guests must be positive when specified.
    val guestsValid =
        guests.isBlank() ||
                (guests.toIntOrNull() ?: 0) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrowseGreen)
            .padding(16.dp)
    ) {
        // Back arrow and title.
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Add Search Filters",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable filter panel for smaller screens.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Color.White,
                    RoundedCornerShape(8.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Location",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                },
                placeholder = {
                    Text("Search by location")
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Maximum Occupancy",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = guests,
                onValueChange = { value ->
                    if (value.all { it.isDigit() }) {
                        guests = value
                    }
                },
                placeholder = {
                    Text("Number of guests")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Check-in and Check-out Dates",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Select both dates using the Material 3 calendar.
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp),
                showModeToggle = false,
                title = null,
                headline = null
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Apply the selected filters.
            Button(
                onClick = {
                    onApplyFilters(
                        location,
                        guests,
                        millisToDate(
                            datePickerState.selectedStartDateMillis
                        ),
                        millisToDate(
                            datePickerState.selectedEndDateMillis
                        )
                    )
                },
                enabled = guestsValid && datesValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrowseGreen
                )
            ) {
                Text("Apply Filters")
            }

            // Remove all filter selections.
            TextButton(
                onClick = {
                    location = ""
                    guests = ""
                    datePickerState.setSelection(null, null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Filters")
            }
        }
    }
}
