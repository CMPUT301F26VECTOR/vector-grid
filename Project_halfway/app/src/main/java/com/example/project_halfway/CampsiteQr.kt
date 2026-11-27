package com.example.project_halfway

/**
 * What is inside a campsite's QR code, and how to get the campsite id back
 * from a scanned code.
 *
 * A QR code has text like "campermax://campsite/abc123". When the entrant
 * scans it, we take out the campsite id and open that campsite. A code that
 * only has the campsite id also works.
 *
 * Used for US 01.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: the owner side that makes the QR code (US 02.02.01) should use textFor so the codes match.
 */
object CampsiteQr {

    /** The start of the text in every campsite QR code. */
    const val PREFIX = "campermax://campsite/"

    /**
     * Gets the text to put in a campsite's QR code.
     *
     * @param campsiteId the campsite's id
     * @return the text for the QR code
     */
    fun textFor(campsiteId: String): String {
        return PREFIX + campsiteId
    }

    /**
     * Gets the campsite id from a scanned QR code.
     *
     * @param scanned the text in the QR code
     * @return the campsite id, or null if it is not a campsite code
     */
    fun campsiteIdFrom(scanned: String?): String? {
        if (scanned == null) {
            return null
        }
        var id = scanned.trim()
        if (id.startsWith(PREFIX)) {
            id = id.removePrefix(PREFIX)
        }
        // a campsite id can't be empty and has no spaces, slashes or colons
        if (id == "" || id.contains(" ") || id.contains("/") || id.contains(":")) {
            return null
        }
        return id
    }
}
