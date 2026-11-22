package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Edit Lottery screen for one of the owner's campsites (US 02.04.06).
 *
 * First it lists the lotteries this campsite is in. The owner taps one, and
 * the same LotterySettingsForm used by Create Lottery opens, filled in with
 * that lottery's registration period, booking period, number of winners and
 * waiting list limit, plus checkboxes for which campsites are in it.
 * Save checks the changes and writes only those fields to Firestore, so the
 * waiting list and any winners stay as they are. Entrants see the new values
 * the next time they open the campsite.
 *
 * Once the winners have been drawn the lottery can't be changed: the screen
 * says so and Save is turned off.
 *
 * Outstanding issues: lotteries have no name in the model yet, so there is no
 * name to edit. Two owners editing the same lottery at once: the last save wins.
 *
 * **AI assistance (Samarth Sathya Karthik):** this code was implemented using
 * Claude AI; Samarth tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @param campsiteId id of the campsite whose lotteries to list
 * @param ownerId id of the signed-in owner, used to list their campsites for the checkboxes
 * @param initialLotteries lotteries to show right away (tests use this instead of Firestore)
 * @param initialCampsites campsites to show right away (tests use this instead of Firestore)
 * @param save writes the changed fields; updateLottery by default, and replaced in tests
 */
@Composable
fun EditLotteryScreen(
    campsiteId: String,
    ownerId: String = "",
    initialLotteries: List<Lottery> = emptyList(),
    initialCampsites: List<CampsiteModel> = emptyList(),
    save: (String, Map<String, Any?>, (Boolean) -> Unit) -> Unit = { id, fields, onDone ->
        updateLottery(id, fields, onDone)
    }
) {
    var lotteries by remember { mutableStateOf(initialLotteries) }
    var ownerCampsites by remember { mutableStateOf(initialCampsites) }

    // The lottery being edited, or null while the list is showing
    var editing by remember { mutableStateOf<Lottery?>(null) }

    // Load the lotteries and the owner's campsites once, unless a test gave them
    LaunchedEffect(campsiteId, ownerId) {
        if (initialLotteries.isEmpty() && campsiteId.isNotBlank()) {
            LotteryRepository.getLotteriesForCampsite(campsiteId) { found -> lotteries = found }
        }
        if (initialCampsites.isEmpty() && ownerId.isNotBlank()) {
            CampsiteRepository.getByOwner(ownerId) { found -> ownerCampsites = found }
        }
    }

    val chosen = editing
    if (chosen == null) {
        LotteryPicker(lotteries = lotteries, onPick = { editing = it })
    } else {
        EditLotteryForm(
            lottery = chosen,
            ownerCampsites = ownerCampsites,
            save = save,
            onBackToList = { editing = null }
        )
    }
}

/**
 * The list of lotteries to pick from. A drawn lottery says so on its button.
 *
 * @param lotteries the campsite's lotteries
 * @param onPick called with the lottery the owner taps
 */
@Composable
private fun LotteryPicker(lotteries: List<Lottery>, onPick: (Lottery) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Lottery", style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("edit_lottery_title"))
        Spacer(Modifier.height(8.dp))

        if (lotteries.isEmpty()) {
            Text("This campsite isn't in any lottery yet.", modifier = Modifier.testTag("edit_lottery_none"))
        }

        for (lottery in lotteries) {
            OutlinedButton(
                onClick = { onPick(lottery) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("edit_lottery_pick_${lottery.id}")
            ) {
                // e.g. "Registration: Nov 3, 2026 to Nov 10, 2026 (drawn)"
                val period = LotterySettings.describe(
                    lotterySettingsFrom(lottery).registrationStart,
                    lotterySettingsFrom(lottery).registrationEnd
                )
                val drawn = if (lottery.isDrawn) " (drawn)" else ""
                Text("Registration: $period$drawn", color = AppGreen)
            }
        }
    }
}

/**
 * The edit form for one lottery: campsite checkboxes, LotterySettingsForm and Save.
 *
 * @param lottery the lottery being edited
 * @param ownerCampsites the owner's campsites, for the checkboxes
 * @param save writes the changed fields
 * @param onBackToList goes back to the list of lotteries
 */
@Composable
private fun EditLotteryForm(
    lottery: Lottery,
    ownerCampsites: List<CampsiteModel>,
    save: (String, Map<String, Any?>, (Boolean) -> Unit) -> Unit,
    onBackToList: () -> Unit
) {
    // The form starts filled in with the lottery's current values
    var settings by remember(lottery.id) { mutableStateOf(lotterySettingsFrom(lottery)) }
    var selectedIds by remember(lottery.id) { mutableStateOf(lottery.campsiteIds) }

    // A drawn lottery is locked, so start with the message already showing
    var message by remember(lottery.id) { mutableStateOf(if (lottery.isDrawn) DRAWN_MESSAGE else "") }
    var saving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Lottery", style = MaterialTheme.typography.titleLarge)

        // The form scrolls; the buttons below it always stay on screen
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (ownerCampsites.isNotEmpty()) {
                Text("Campsites in this lottery", style = MaterialTheme.typography.titleMedium)
                for (campsite in ownerCampsites) {
                    val id = campsite.getId()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = id in selectedIds,
                            enabled = !lottery.isDrawn,
                            onCheckedChange = { ticked ->
                                selectedIds = if (ticked) selectedIds + id else selectedIds - id
                            },
                            modifier = Modifier.testTag("edit_campsite_check_$id")
                        )
                        Text(campsite.getCampsiteName())
                    }
                }
            }
            LotterySettingsForm(settings = settings, onSettingsChange = { settings = it })
        }

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).testTag("edit_lottery_save"),
            enabled = !lottery.isDrawn && !saving,
            colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
            onClick = {
                // Check everything first; only save when there is no problem
                val error = editLotteryError(lottery, settings, selectedIds)
                if (error != null) {
                    message = error
                } else {
                    message = "Saving..."
                    saving = true
                    save(lottery.id, lotteryChanges(settings, selectedIds)) { worked ->
                        saving = false
                        message = if (worked) "Lottery updated" else "Could not save, please try again."
                    }
                }
            }
        ) {
            Text("Save changes")
        }
        OutlinedButton(onClick = onBackToList, modifier = Modifier.fillMaxWidth()) {
            Text("Back to lotteries", color = AppGreen)
        }

        Text(
            message,
            color = if (message == DRAWN_MESSAGE) Color(0xFFB3261E) else Color.Unspecified,
            modifier = Modifier.padding(top = 8.dp).testTag("edit_lottery_message")
        )
    }
}

/** Shown when the owner opens a lottery whose winners were already drawn. */
const val DRAWN_MESSAGE = "Winners already drawn, this lottery can't be changed."

/**
 * Fills LotterySettings in from a saved lottery, so the form shows its values.
 *
 * The date picker works with midnight UTC of the picked day, but the lottery
 * stores registration as times in the owner's zone. So the registration times
 * are turned back into "which day" and then into midnight UTC. Booking dates
 * are already stored as picker values, and 0 means not set.
 *
 * @param lottery the saved lottery
 * @param zone the owner's time zone
 * @return the settings to show in LotterySettingsForm
 */
fun lotterySettingsFrom(lottery: Lottery, zone: ZoneId = ZoneId.systemDefault()): LotterySettings {
    return LotterySettings(
        registrationStart = toPickerValue(lottery.startDate, zone),
        registrationEnd = toPickerValue(lottery.endDate, zone),
        bookingStart = if (lottery.bookingStartDate > 0L) lottery.bookingStartDate else null,
        bookingEnd = if (lottery.bookingEndDate > 0L) lottery.bookingEndDate else null,
        numberOfWinners = lottery.numberOfWinners.toString(),
        waitlistCapacity = lottery.waitlistCapacity?.toString() ?: ""
    )
}

/** A time in the owner's zone -> midnight UTC of that day (the date picker's format), or null for 0. */
private fun toPickerValue(millis: Long, zone: ZoneId): Long? {
    if (millis <= 0L) return null
    val day = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

/**
 * Checks an edit before saving, top to bottom (US 02.04.06).
 *
 * @param lottery the lottery as it is saved now
 * @param settings what the owner changed it to
 * @param campsiteIds the campsites ticked in the form
 * @param now the current time in milliseconds
 * @param zone the owner's time zone
 * @return a message to show the owner, or null if it can be saved
 */
fun editLotteryError(
    lottery: Lottery,
    settings: LotterySettings,
    campsiteIds: List<String>,
    now: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault()
): String? {
    // Rule from the story: no edits after the draw
    if (lottery.isDrawn) {
        return DRAWN_MESSAGE
    }
    if (campsiteIds.isEmpty()) {
        return "Please pick at least one campsite."
    }

    // The same rules as Create Lottery (dates, winners, limit)
    val settingsError = settings.firstError(now, zone)
    if (settingsError != null) {
        return settingsError
    }

    // The limit can't be lower than the number of people already waiting
    val capacity = settings.waitlistCapacity.trim().toIntOrNull()
    if (capacity != null && capacity < lottery.waitingList.size) {
        return "${lottery.waitingList.size} entrants are already waiting, so the limit can't be lower than that."
    }
    return null
}

/**
 * Turns the edited settings into the Firestore fields to change.
 * The waiting list, winners and drawn flag are not in it, so they stay as they are.
 * A value of null means "remove this field" (the booking period or limit was cleared).
 *
 * @param settings the edited settings
 * @param campsiteIds the campsites ticked in the form; the first is the main one
 * @param zone the owner's time zone
 * @return field name -> new value
 */
fun lotteryChanges(
    settings: LotterySettings,
    campsiteIds: List<String>,
    zone: ZoneId = ZoneId.systemDefault()
): Map<String, Any?> {
    // toLottery does the date maths the same way Create Lottery does
    val edited = settings.toLottery(campsiteIds.first(), zone = zone, campsiteIds = campsiteIds)

    return mapOf(
        "campsiteId" to campsiteIds.first(),
        "campsiteIds" to campsiteIds,
        "startDate" to edited.startDate,
        "endDate" to edited.endDate,
        "numberOfWinners" to edited.numberOfWinners,
        "bookingStartDate" to (if (edited.bookingStartDate > 0L) edited.bookingStartDate else null),
        "bookingEndDate" to (if (edited.bookingEndDate > 0L) edited.bookingEndDate else null),
        "waitlistCapacity" to edited.waitlistCapacity
    )
}

/**
 * Writes the changed fields to one lottery in Firestore, as the owner.
 * Fields whose value is null are deleted from the document.
 *
 * @param lotteryId the lottery's Firestore id
 * @param fields field name -> new value, from lotteryChanges
 * @param onDone gets true once it is saved, false if it failed
 */
fun updateLottery(lotteryId: String, fields: Map<String, Any?>, onDone: (Boolean) -> Unit) {
    // Firestore can't store "null means delete", so swap nulls for FieldValue.delete()
    val toWrite = mutableMapOf<String, Any>()
    for ((name, value) in fields) {
        toWrite[name] = value ?: FieldValue.delete()
    }

    FirebaseSecurity.ownerDb.collection("lotteries").document(lotteryId)
        .update(toWrite)
        .addOnSuccessListener { onDone(true) }
        .addOnFailureListener {
            println("Updating the lottery failed: ${it.message}")
            onDone(false)
        }
}
