package com.example.project_halfway

/**
 * Makes the text for a CSV file with the entrant's reservations, so they
 * can download it and open it in Excel or Google Sheets.
 *
 * Each reservation is one row and shows its status, so the file shows the
 * entrant's reservations (US 01.05.02) and also which ones were cancelled
 * (US 01.07.02).
 *
 * Used for US 01.05.02 and US 01.07.02.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: none known.
 */
object ReservationCsv {

    /** The first line of the file with the column names. */
    const val HEADER = "Campsite,Start date,End date,Guests,Status,Reserved on,Cancelled on"

    /**
     * Makes the whole CSV text: the header and then one line for each reservation.
     *
     * @param reservations the entrant's reservations
     * @param campsiteNames a map of campsite id to campsite name
     * @return the CSV text
     */
    fun build(reservations: List<Reservation>, campsiteNames: Map<String, String>): String {
        var csv = HEADER + "\n"
        val sorted = reservations.sortedBy { it.startDate }
        for (r in sorted) {
            // use the id if we don't have the campsite name
            val name = campsiteNames[r.campsiteId] ?: r.campsiteId
            csv = csv + row(r, name) + "\n"
        }
        return csv
    }

    /**
     * Makes one line of the file for one reservation.
     *
     * @param r the reservation
     * @param campsiteName the campsite name
     * @return the line
     */
    fun row(r: Reservation, campsiteName: String): String {
        var cancelledOn = ""
        if (r.isCancelled()) {
            cancelledOn = EntrantNotification.timeText(r.cancelledAt)
        }
        val reservedOn = EntrantNotification.timeText(r.createdAt)

        return clean(campsiteName) + "," +
            clean(r.startDate) + "," +
            clean(r.endDate) + "," +
            r.guests + "," +
            clean(r.status) + "," +
            reservedOn + "," +
            cancelledOn
    }

    /**
     * Makes a value safe to put in a CSV file. If it has a comma, a quote or
     * a new line, it is put inside quotes, and quotes inside it are doubled.
     *
     * @param value the value
     * @return the safe value
     */
    fun clean(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\""
        }
        return value
    }

    /**
     * The file name to use when saving, like "my_reservations_2026-11-23.csv".
     *
     * @param today today's date as "yyyy-MM-dd"
     * @return the file name
     */
    fun fileName(today: String): String {
        return "my_reservations_" + today + ".csv"
    }
}
