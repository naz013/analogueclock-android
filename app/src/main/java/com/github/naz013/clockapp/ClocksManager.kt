package com.github.naz013.clockapp

import android.util.Log
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat

class ClocksManager(
    private val prefs: Prefs
) {

    private var timeZones: List<DateTimeZone> = emptyList()

    fun getNumberOfSecondsUntilEndOfMinute(): Int {
        return 60 - LocalTime.now().secondOfMinute
    }

    fun getCurrentClock(): ClockData {
        return getUserClock() ?: getDefaultClock()
    }

    fun loadAllTimeZones() {
        timeZones = DateTimeZone.getAvailableIDs().map { DateTimeZone.forID(it) }
    }

    private fun getUserClock(): ClockData? {
        val mainClockId = prefs.getMainClockId() ?: return null
        return getClockData(DateTimeZone.forID(mainClockId))
    }

    private fun getDefaultClock(): ClockData {
        return getClockData(DateTimeZone.getDefault())
    }

    private fun getClockData(timeZone: DateTimeZone): ClockData {
        val time = LocalTime.now(timeZone)
        return ClockData(
            time = formatTime(time),
            location = timeZone.getName(System.currentTimeMillis()),
            timeZone = timeZone,
            amPm = formatAmpm(time)
        )
    }

    private fun formatTime(time: LocalTime): String {
        return if (prefs.is12HourFormat()) {
            TIME_12.print(time)
        } else {
            TIME_24.print(time)
        }
    }

    private fun formatAmpm(time: LocalTime): String? {
        return if (prefs.is12HourFormat()) {
            TIME_AMPM.print(time)
        } else {
            null
        }
    }

    private fun log(message: String) {
        Log.d("MainActivityVM", message)
    }

    companion object {
        private val TIME_24 = DateTimeFormat.forPattern("HH:mm")
        private val TIME_12 = DateTimeFormat.forPattern("hh:mm")
        private val TIME_AMPM = DateTimeFormat.forPattern("aa")
    }
}