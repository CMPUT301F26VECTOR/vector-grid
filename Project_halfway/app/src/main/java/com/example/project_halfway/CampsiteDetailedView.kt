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
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import java.util.Locale

// Detailed campsite view
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
                CampsiteCalendar()

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
 * Calendar on the campsite details screen showing the campsite's available dates.
 *
 * Outstanding issues: not built yet; it only prints a message for now.
 *
 * @param modifier layout options from the screen that shows it
 */
@Composable
fun CampsiteCalendar( modifier: Modifier = Modifier) {
    print("CALANDER TO BE IMPLEMENTED")
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
                Text("Open in Google Maps", color = Color(0xff566C11))
            }
        }
    }
}
