package com.example.project_halfway

import android.content.Context
import android.content.Intent
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Opens the camera to scan a campsite's QR code, then opens that
 * campsite's details (CampsiteDetailsActivity).
 *
 * It uses the Google code scanner, which has its own camera screen, so our
 * app doesn't need the camera permission.
 *
 * Used for US 01.12.01.
 *
 * Library: Google code scanner, com.google.android.gms:play-services-code-scanner (https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner).
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: the phone or emulator needs Google Play services. The first scan can be slow because the scanner gets downloaded.
 */
object QrScanner {

    /**
     * Scans a QR code and opens the campsite in it.
     *
     * @param context the screen that starts the scan
     * @param onError gets a message if the scan didn't work or it is not a campsite code
     */
    fun scanAndOpen(context: Context, onError: (String) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val campsiteId = CampsiteQr.campsiteIdFrom(barcode.rawValue)
                if (campsiteId == null) {
                    onError("This QR code is not a campsite code")
                } else {
                    val intent = Intent(context, CampsiteDetailsActivity::class.java)
                    intent.putExtra("campsiteId", campsiteId)
                    context.startActivity(intent)
                }
            }
            .addOnFailureListener {
                onError("Could not scan: " + it.message)
            }
    }
}
