package com.github.naz013.clockapp.data

import org.joda.time.DateTimeZone

data class TimeZoneData(
    val displayName: String,
    val shortName: String,
    val timeZone: DateTimeZone,
    var isSelected: Boolean = false
)
