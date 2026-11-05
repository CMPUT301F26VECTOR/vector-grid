package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment

/**
 * Create Lottery screen for one of the owner's campsites (US 02.04.01).
 * The owner sets the registration period, the optional booking period, the
 * number of winners and the optional waiting list limit with
 * LotterySettingsForm, then presses Save. The settings are checked with
 * LotterySettings.firstError() and the new lottery is stored with
 * LotteryRepository.save. Entrants then see it on the campsite's details.
 *
 * Also used for US 02.04.03, US 02.04.04, US 02.04.05 and US 02.04.07.
 *
 * Outstanding issues: Save is turned off after one lottery is created, so the
 * owner has to go back and open the screen again to create another.
 *
 * @param campsiteId id of the campsite the lottery is for; saving is refused if blank
 * @param campsiteName name shown in the title so the owner knows which campsite it is for
 * @param ownerId id of the signed-in owner, used to load their campsites; blank means no list
 * @param initialCampsites campsites to show right away (tests use this instead of Firestore)
 * @param initialSettings what the form starts with; empty by default
 * @param save stores the lottery and reports whether it worked; LotteryRepository.save
 * by default, and replaced in tests so they don't need Firestore
 */
@Composable
fun CreateLotteryScreen(
    campsiteId: String,
    campsiteName: String = "",
    ownerId: String = "",
    initialCampsites: List<CampsiteModel> = emptyList(),
    initialSettings: LotterySettings = LotterySettings(),
    save: (Lottery, (Boolean) -> Unit) -> Unit = { lottery, onDone ->
        LotteryRepository.save(lottery, onDone)
    }
) {
    var settings by remember { mutableStateOf(initialSettings) }

    // Message shown under the Save button (an error, "Saving..." or "Lottery created")
    var message by remember { mutableStateOf("") }

    // true while saving and after it worked, so one press makes one lottery
    var saveLocked by remember { mutableStateOf(false) }
    var ownerCampsites by remember { mutableStateOf(initialCampsites) }
    var selectedIds by remember {
        mutableStateOf(if (campsiteId.isBlank()) emptyList() else listOf(campsiteId))
    }

    LaunchedEffect(ownerId) {
        if (ownerId.isNotBlank()) {
            CampsiteRepository.getByOwner(ownerId) { found -> ownerCampsites = found }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        val title = if (campsiteName.isBlank()) "Create Lottery" else "Create Lottery for $campsiteName"
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("create_lottery_title"))

        // The form scrolls; the Save button below it always stays on screen.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (ownerCampsites.isNotEmpty()) {
                Text("Campsites in this lottery", style = MaterialTheme.typography.titleMedium)
                ownerCampsites.forEach { campsite ->
                    val id = campsite.getId()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = id in selectedIds,
                            enabled = !saveLocked,
                            onCheckedChange = { ticked ->
                                selectedIds = if (ticked) selectedIds + id else selectedIds - id
                                if (!saveLocked) message = ""
                            },
                            modifier = Modifier.testTag("campsite_check_$id")
                        )
                        Text(campsite.getCampsiteName())
                    }
                }
            }
            LotterySettingsForm(
                settings = settings,
                onSettingsChange = {
                    settings = it
                    if (!saveLocked) message = ""
                }
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("create_lottery_save"),
            enabled = !saveLocked,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11)),
            onClick = {
                val error = settings.firstError()
                if (error != null) {
                    message = error
                } else if (campsiteId.isBlank()) {
                    message = "Please pick a campsite first."
                } else if (selectedIds.isEmpty()) {
                    message = "Please pick at least one campsite."
                } else {
                    message = "Saving..."
                    saveLocked = true
                    save(settings.toLottery(selectedIds.first(), campsiteIds = selectedIds)) { worked ->
                        if (worked) {
                            message = "Lottery created"
                        } else {
                            message = "Could not save, please try again."
                            saveLocked = false
                        }
                    }
                }
            }
        ) {
            Text("Save")
        }

        Text(message, modifier = Modifier.padding(top = 8.dp).testTag("create_lottery_message"))
    }
}
