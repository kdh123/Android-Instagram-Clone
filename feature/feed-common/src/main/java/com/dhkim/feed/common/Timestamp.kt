package com.dhkim.feed.common


sealed interface Timestamp {

    data object JustNow : Timestamp
    data class MinutesAgo(val minutes: Long) : Timestamp
    data class HoursAgo(val hours: Long) : Timestamp
    data class DaysAgo(val days: Long) : Timestamp
    data class Date(val date: String) : Timestamp
}