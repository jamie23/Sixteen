package com.jamie.hn.core.ui

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class CoreDataMapper(
    private val resourceProvider: CoreResourceProvider
) {
    fun time(time: Long): String {
        val timePost = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneOffset.UTC)
        val timeNow = LocalDateTime.now()

        val timeBetween = Duration.between(timePost, timeNow)
        if (timeBetween.toDays() > 0) return "${timeBetween.toDays()}${resourceProvider.days}"
        if (timeBetween.toHours() > 0L) return "${timeBetween.toHours()}${resourceProvider.hours}"
        if (timeBetween.toMinutes() > 0L) return "${timeBetween.toMinutes()}${resourceProvider.minutes}"

        return ""
    }
}
