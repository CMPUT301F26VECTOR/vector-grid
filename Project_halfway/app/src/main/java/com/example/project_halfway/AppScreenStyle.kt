/*
 * Shared look for the app's green screens, copied from Browse Campsites:
 * the olive green background, a white bold title next to a back arrow,
 * white rounded panels and cards, and green buttons. Used by the Welcome,
 * owner login, owner sign-up, owner home, Register Campsite, entrant home and
 * App Policy screens.
 *
 * Outstanding issues: Browse Campsites and the other screens still set their
 * own colours instead of using these.
 */
package com.example.project_halfway

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/** The app's green, the same as Browse Campsites and the campsite cards. */
internal val AppGreen = Color(0xFF566C11)

/**
 * The title row used at the top of green screens: a white back arrow (when
 * [onBackClick] is given) and the title in white bold 22sp, as on Browse.
 *
 * @param title the screen's title
 * @param onBackClick called when the arrow is pressed; null shows no arrow
 * @param backButtonTag test tag for the back arrow
 */
@Composable
internal fun GreenScreenHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    backButtonTag: String = "back_button"
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick, modifier = Modifier.testTag(backButtonTag)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * A white rounded panel, like the Add Search Filters panel and the campsite
 * cards, holding a form or list on the green background.
 *
 * @param modifier modifier for the panel (size, weight)
 * @param scrollable true to scroll the contents inside the panel, keeping
 * its rounded corners in place
 * @param content what goes in the panel, laid out top to bottom
 */
@Composable
internal fun WhitePanel(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White, shape)
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(16.dp),
        content = content
    )
}

/**
 * A bold section label inside a [WhitePanel], like "Location" on the filters.
 *
 * @param text the label
 * @param modifier modifier for the label
 * @param first true for the panel's first label, which needs no space above it
 */
@Composable
internal fun PanelLabel(text: String, modifier: Modifier = Modifier, first: Boolean = false) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = if (first) 0.dp else 12.dp, bottom = 4.dp)
    )
}

/** Text box colours inside a [WhitePanel]: green border, label and cursor while typing. */
@Composable
internal fun greenFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppGreen,
    focusedLabelColor = AppGreen,
    focusedLeadingIconColor = AppGreen,
    cursorColor = AppGreen
)

/** The main button colours: green with white text, like Apply Filters. */
@Composable
internal fun greenButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = AppGreen, contentColor = Color.White)

/** Outlined button text colour inside a [WhitePanel]. */
@Composable
internal fun greenOutlinedButtonColors(): ButtonColors =
    ButtonDefaults.outlinedButtonColors(contentColor = AppGreen)

/**
 * Makes the status bar icons (clock, battery) white while this screen shows,
 * so they can be read on the green background. The old setting comes back
 * when the screen closes.
 */
@Composable
internal fun LightStatusBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val wasLight = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (wasLight != null) controller.isAppearanceLightStatusBars = wasLight }
    }
}

/** Space between the header and the panel, as on Browse. */
internal val HeaderGap = 12.dp

/** A small vertical gap between items in a panel. */
@Composable
internal fun PanelGap() = Spacer(Modifier.height(8.dp))

/**
 * A white rounded card that opens another screen, used on the entrant and
 * owner home screens: a green icon, a bold title and a grey line under it.
 * Same corners and shadow as the campsite cards on Browse.
 *
 * @param icon icon shown on the left
 * @param title the card's title, e.g. "Browse Campsites"
 * @param subtitle one short line explaining what the card opens
 * @param onClick called when the card is tapped
 * @param modifier modifier for the card
 */
@Composable
internal fun HomeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppGreen, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF5F6368), lineHeight = 17.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF9AA0A6)
            )
        }
    }
}

/** Colours for a delete button: a dark red that reads well on the green. */
@Composable
internal fun dangerButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E), contentColor = Color.White)

/** A white button with green text, for main actions sitting on the green background. */
@Composable
internal fun whiteButtonColors(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppGreen)
