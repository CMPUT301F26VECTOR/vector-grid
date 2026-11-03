package com.example.project_halfway

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Shows whichever campsite owner screen is open and moves between them: the
 * owner home, Register Campsite (US 02.01.01, US 02.01.02), My Campsites
 * (US 02.03.01), one campsite's details (US 02.01.04), its lottery's
 * waitlist and winners (US 02.05.01, US 02.09.01) and Create Lottery
 * (US 02.04.01, US 02.04.03, US 02.04.04, US 02.04.05, US 02.04.07).
 *
 * It works like AuthGate: one variable holds the name of the open screen, and
 * each screen gets callbacks that change it. The back arrows and the phone's
 * back button go one step back, as ownerBackTarget says.
 *
 * Outstanding issues: which screen is open is kept in memory only, so turning
 * the phone or restarting the app returns to the owner home. The owner home
 * itself is still the placeholder layout in OwnerHomeScreenUI.
 *
 * @param ownerId user id of the signed in owner; saved on new campsites and
 * used to load My Campsites
 * @param onSignOut called when the owner presses Sign Out on the owner home
 */
@Composable
fun OwnerScreens(ownerId: String, onSignOut: () -> Unit) {

    // Which owner screen is open: "home", "register", "mine", "details", "waitlist"
    // or "createLottery"
    var screen by remember { mutableStateOf("home") }

    // The campsite the owner tapped in My Campsites (used by "details", "waitlist"
    // and "createLottery")
    var selectedCampsite by remember { mutableStateOf<CampsiteModel?>(null) }

    // The phone's back button does the same as the back arrows.
    // On the owner home there is nowhere to go back to, so it is turned off there.
    BackHandler(enabled = screen != "home") {
        screen = ownerBackTarget(screen)
    }

    val chosen = selectedCampsite

    when (screen) {
        "register" -> WithBackArrow(onBack = { screen = ownerBackTarget(screen) }) {
            RegisterCampsiteScreen(ownerId = ownerId)
        }

        "mine" -> WithBackArrow(onBack = { screen = ownerBackTarget(screen) }) {
            RegisteredCampsitesScreen(
                ownerId = ownerId,
                onCampsiteClick = { campsite ->
                    selectedCampsite = campsite
                    screen = "details"
                }
            )
        }

        "details" -> {
            if (chosen != null) {
                // Mirah's details card, with a waitlist button where entrants see Join
                CampsiteDetailedCard(
                    campsite = chosen,
                    isLotteryActive = false,
                    onBackClick = { screen = ownerBackTarget(screen) },
                    onJoinClick = {},
                    actionArea = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = { screen = "waitlist" },
                                modifier = Modifier.testTag("view_waitlist_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11))
                            ) {
                                Text("View waitlist and winners")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { screen = "createLottery" },
                                modifier = Modifier.testTag("create_lottery_button")
                            ) {
                                Text("Create lottery", color = Color(0xff566C11))
                            }
                        }
                    }
                )
            }
        }

        "waitlist" -> {
            if (chosen != null) {
                WithBackArrow(onBack = { screen = ownerBackTarget(screen) }) {
                    LotteryEntrantsScreen(campsiteId = chosen.getId())
                }
            }
        }

        "createLottery" -> {
            if (chosen != null) {
                WithBackArrow(onBack = { screen = ownerBackTarget(screen) }) {
                    CreateLotteryScreen(campsiteId = chosen.getId(), campsiteName = chosen.getCampsiteName())
                }
            }
        }

        else -> OwnerHomeScreenUI(
            onSignOut = onSignOut,
            onRegisterClick = { screen = "register" },
            onMyCampsitesClick = { screen = "mine" }
        )
    }
}

/**
 * Says which owner screen the back arrow or the phone's back button leads to.
 * Details goes back to My Campsites, the waitlist and Create Lottery go back
 * to details, and everything else goes back to the owner home.
 *
 * @param screen the name of the screen that is open now
 * @return the name of the screen to go back to
 */
fun ownerBackTarget(screen: String): String {
    return when (screen) {
        "details" -> "mine"
        "waitlist", "createLottery" -> "details"
        else -> "home"
    }
}

/**
 * Puts a back arrow above a screen that has none of its own, and gives the
 * screen the rest of the space.
 */
@Composable
private fun WithBackArrow(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 24.dp, start = 8.dp)
                .testTag("owner_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xff566C11)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
