package com.github.naz013.clockapp.util

import com.github.naz013.clockapp.data.ClockData
import com.github.naz013.clockapp.data.TimeZoneData
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.FormatUtils

class ClocksManager(private val prefs: Prefs) {

    private var timeZones: List<DateTimeZone> = emptyList()

    fun getNumberOfSecondsUntilEndOfMinute(): Int {
        return 60 - LocalTime.now().secondOfMinute
    }

    fun getCurrentClock(): ClockData {
        return getUserClock() ?: getDefaultClock()
    }

    fun getTimeZoneList(): List<TimeZoneData> {
        val timeZones = this.timeZones.takeIf { it.isNotEmpty() } ?: loadAllTimeZones()
        val userTimeZones = getUserTimeZoneIds()
        return timeZones.map { getTimeZoneData(it, userTimeZones.contains(it.id)) }
    }

    fun getUserClocks(): List<ClockData> {
        return getUserTimeZoneIds().map { DateTimeZone.forID(it) }.map { getClockData(it) }
    }

    private fun getUserTimeZoneIds(): List<String> {
        return prefs.getClockIds()
    }

    private fun loadAllTimeZones(): List<DateTimeZone> {
        val currentTimeZone = DateTimeZone.getDefault()
        timeZones = DateTimeZone.getAvailableIDs()
            .map { DateTimeZone.forID(it) }
            .filterNot { it.id == currentTimeZone.id }
            .toMutableList()
            .apply {
                add(0, currentTimeZone)
            }
        return timeZones
    }

    private fun getUserClock(): ClockData? {
        val mainClockId = prefs.getMainClockId() ?: return null
        return getClockData(DateTimeZone.forID(mainClockId))
    }

    private fun getDefaultClock(): ClockData {
        return getClockData(DateTimeZone.getDefault())
    }

    private fun getTimeZoneData(timeZone: DateTimeZone, isSelected: Boolean = false): TimeZoneData {
        val time = DateTime.now(DateTimeZone.UTC).millis
        return TimeZoneData(
            displayName = timeZone.id + " | " + printOffset(timeZone.getOffset(time)),
            shortName = timeZone.getShortName(time),
            timeZone = timeZone,
            isSelected = isSelected
        )
    }

    private fun getClockData(timeZone: DateTimeZone): ClockData {
        val time = DateTime.now(timeZone)
        val myTimeZone = prefs.getMainClockId()
            ?.let { DateTimeZone.forID(it) }
            ?: DateTimeZone.getDefault()
        val myTime = DateTime.now(myTimeZone)
        return ClockData(
            time = formatTime(time),
            displayName = timeZone.id + " | " + timeZone.getName(time.millis),
            timeZone = timeZone,
            amPm = formatAmpm(time),
            offset = printOffset(timeZone.getOffset(myTime)),
            dateTime = time
        )
    }

    private fun formatTime(time: DateTime): String {
        return if (prefs.is12HourFormat()) {
            TIME_12.print(time)
        } else {
            TIME_24.print(time)
        }
    }

    private fun formatAmpm(time: DateTime): String? {
        return if (prefs.is12HourFormat()) {
            TIME_AMPM.print(time)
        } else {
            null
        }
    }

    private fun printOffset(offsetMillis: Int): String {
        var offset = offsetMillis
        val buf = StringBuffer()
        if (offset >= 0) {
            buf.append('+')
        } else {
            buf.append('-')
            offset = -offset
        }
        val hours = offset / DateTimeConstants.MILLIS_PER_HOUR
        FormatUtils.appendPaddedInteger(buf, hours, 2)
        offset -= hours * DateTimeConstants.MILLIS_PER_HOUR
        val minutes = offset / DateTimeConstants.MILLIS_PER_MINUTE
        buf.append(':')
        FormatUtils.appendPaddedInteger(buf, minutes, 2)
        offset -= minutes * DateTimeConstants.MILLIS_PER_MINUTE
        if (offset == 0) {
            return buf.toString()
        }
        val seconds = offset / DateTimeConstants.MILLIS_PER_SECOND
        buf.append(':')
        FormatUtils.appendPaddedInteger(buf, seconds, 2)
        offset -= seconds * DateTimeConstants.MILLIS_PER_SECOND
        if (offset == 0) {
            return buf.toString()
        }
        buf.append('.')
        FormatUtils.appendPaddedInteger(buf, offset, 3)
        return buf.toString()
    }

    companion object {
        private val TIME_24 = DateTimeFormat.forPattern("HH:mm")
        private val TIME_12 = DateTimeFormat.forPattern("hh:mm")
        private val TIME_AMPM = DateTimeFormat.forPattern("aa")
    }
}