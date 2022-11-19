package com.github.naz013.clockapp.data

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class ClockData(
    val time: String,
    val displayName: String,
    val dateTime: DateTime,
    val timeZone: DateTimeZone,
    val amPm: String? = null,
    val offset: String? = null
)
