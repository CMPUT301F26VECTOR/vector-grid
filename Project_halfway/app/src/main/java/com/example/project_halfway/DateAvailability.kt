package com.example.project_halfway

/**
 * Checks whether a selected date range is available.
 * US 01.04.01
 *
 * Cancelled and rejected reservations do not block dates.
 * Start and end dates are inclusive.
 *
 */
fun isDateRangeAvailable(
    startDate: String,
    endDate: String,
    reservations: List<Reservation>
): Boolean {
    if (startDate > endDate) return false

    return reservations.none { reservation ->
        !reservation.isCancelled() &&
                startDate <= reservation.endDate &&
                endDate >= reservation.startDate
    }
}