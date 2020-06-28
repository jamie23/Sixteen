package com.jamie.hn.core.ui

import org.joda.time.DateTime
import org.joda.time.Duration

class CoreDataMapper(
    private val resourceProvider: CoreResourceProvider
) {
    fun time(time: DateTime): String {
        val duration = Duration(time, DateTime.now())

        val timeDays = duration.standardDays
        if (timeDays > 0) return "${timeDays}${resourceProvider.days}"

        val timeHours = duration.standardHours
        if (timeHours > 0) return "${timeHours}${resourceProvider.hours}"

        val timeMinutes = duration.standardMinutes
        if (timeMinutes > 0L) return "${timeMinutes}${resourceProvider.minutes}"

        return ""
    }
}
