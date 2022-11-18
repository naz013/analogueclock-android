package com.github.naz013.clockapp

import android.util.Log
import org.joda.time.DateTimeZone

class ClocksProvider {

    private var timeZones: List<DateTimeZone> = emptyList()

    fun getCurrentClock(): ClockData {
        val timeZone = DateTimeZone.getDefault()
        log("name=${timeZone.getName(System.currentTimeMillis())}")
        log("short name=${timeZone.getShortName(System.currentTimeMillis())}")
        log("id=${timeZone.id}")
        return ClockData(
            time = "10:54",
            location = "New York, USA",
            timeZone = "EST",
            amPm = "PM"
        )
    }

    fun loadAllTimeZones() {
        timeZones = DateTimeZone.getAvailableIDs().map { DateTimeZone.forID(it) }
    }

    private fun log(message: String) {
        Log.d("MainActivityVM", message)
    }

    companion object {

    }
}