package com.example.project_halfway

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * QR code screen for one of the owner's campsites (US 02.02.01).
 *
 * The app makes the QR code itself, so there is nothing to save: the code
 * holds the link "campermax://campsite/<campsite id>". Firestore gives every
 * campsite a different id, so every campsite gets a different code, and the
 * same campsite always gets the same code. The owner can print it on posters
 * or share the link with the Share button.
 *
 * The QR picture is drawn with the ZXing core library (Apache 2.0,
 * https://github.com/zxing/zxing).
 *
 * Outstanding issues: scanning the code and opening the campsite's details is
 * the entrant's QR story; it needs CampsiteQr.campsiteIdFrom and a link
 * handler in AndroidManifest.xml. Share sends the link as text, not the picture.
 *
 * **AI assistance (Samarth Sathya Karthik):** this code was implemented using
 * Claude AI; Samarth tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @param campsite the campsite to show the QR code for
 * @param onBackClick called when the back arrow is pressed
 */
@Composable
fun CampsiteQrScreen(campsite: CampsiteModel, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val link = CampsiteQr.linkFor(campsite.getId())

    // Draw the QR picture once per link, not on every redraw
    val picture = remember(link) { CampsiteQr.toBitmap(CampsiteQr.matrixFor(link, 600)) }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 16.dp, bottom = 16.dp)
    ) {
        GreenScreenHeader(title = "Campsite QR Code", onBackClick = onBackClick)
        Spacer(Modifier.height(HeaderGap))

        WhitePanel(modifier = Modifier.padding(start = 8.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(campsite.getCampsiteName(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Entrants can scan this code to open the campsite's details.",
                    color = Color(0xFF5F6368),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Image(
                    bitmap = picture.asImageBitmap(),
                    contentDescription = "QR code for ${campsite.getCampsiteName()}",
                    modifier = Modifier.size(260.dp).testTag("campsite_qr_image")
                )
                Spacer(Modifier.height(8.dp))
                Text(link, fontSize = 12.sp, color = Color(0xFF5F6368), modifier = Modifier.testTag("campsite_qr_link"))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Opens Android's share sheet with the campsite's name and link
        Button(
            onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out ${campsite.getCampsiteName()} on CamperMax: $link")
                }
                context.startActivity(Intent.createChooser(send, "Share campsite"))
            },
            colors = whiteButtonColors(),
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp).height(48.dp).testTag("campsite_qr_share")
        ) {
            Text("SHARE", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Makes the text and picture of a campsite's QR code (US 02.02.01).
 * linkFor, campsiteIdFrom and matrixFor have no Android code, so they are unit
 * tested in CampsiteQrTest.
 */
object CampsiteQr {

    /** Every campsite link starts with this. */
    const val PREFIX = "campermax://campsite/"

    /**
     * The text stored in a campsite's QR code.
     *
     * @param campsiteId the campsite's Firestore id
     * @return e.g. "campermax://campsite/abc123"
     */
    fun linkFor(campsiteId: String): String {
        return PREFIX + campsiteId
    }

    /**
     * Reads the campsite id back out of a scanned QR code.
     *
     * @param text what the scanner read
     * @return the campsite id, or null if it isn't one of our campsite codes
     */
    fun campsiteIdFrom(text: String): String? {
        if (!text.startsWith(PREFIX)) return null
        val id = text.removePrefix(PREFIX).trim()
        return if (id.isEmpty()) null else id
    }

    /**
     * Turns text into a QR code grid with ZXing: true squares are black.
     *
     * @param text what the QR code should hold
     * @param size width and height in squares (pixels when drawn)
     * @return the QR code grid
     */
    fun matrixFor(text: String, size: Int): BitMatrix {
        return QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    }

    /**
     * Draws a QR code grid as a black and white picture.
     *
     * @param matrix the grid from matrixFor
     * @return the picture to show on screen
     */
    fun toBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)

        // Go over every square and colour it black or white
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
