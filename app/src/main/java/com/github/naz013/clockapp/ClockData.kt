package com.github.naz013.clockapp

import org.joda.time.DateTimeZone

data class ClockData(
    val time: String,
    val displayName: String,
    val timeZone: DateTimeZone,
    val amPm: String? = null,
    val offset: String? = null
)
