package com.github.naz013.clockapp

data class ClockData(
    val time: String,
    val location: String,
    val timeZone: String,
    val amPm: String? = null,
    val offset: String? = null
)
