package com.jamie.hn.core.ui

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CoreDataMapper(
    private val resourceProvider: CoreResourceProvider
) {
    fun time(time: Long): String {
        val timePost = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
        val timeNow = LocalDateTime.now()

        val timeBetween = Duration.between(timePost, timeNow)
        if (timeBetween.toDays() > 0) return "${timeBetween.toDays()}${resourceProvider.days}"
        if (timeBetween.toHours() > 0L) return "${timeBetween.toHours()}${resourceProvider.hours}"
        if (timeBetween.toMinutes() > 0L) return "${timeBetween.toMinutes()}${resourceProvider.minutes}"

        return ""
    }

//    fun time(time: Long): String {
//        val sdf = SimpleDateFormat("MM/dd/yyyy")
//        val netDate = Date(time)
//        return sdf.format(netDate)
//    }
}
