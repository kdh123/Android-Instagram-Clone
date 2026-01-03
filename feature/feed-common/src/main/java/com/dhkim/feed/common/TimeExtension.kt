package com.dhkim.feed.common

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toRelativeTime(): Timestamp {
    val now = Instant.now()
    val past = Instant.ofEpochMilli(this)
    val duration = Duration.between(past, now)

    val seconds = duration.seconds
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> Timestamp.JustNow
        minutes < 60 -> Timestamp.MinutesAgo(minutes)
        hours < 24 -> Timestamp.HoursAgo(hours)
        days < 7 -> Timestamp.DaysAgo(days)
        else -> {
            val date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.systemDefault())
                .format(past)
            Timestamp.Date(date)
        }
    }
}