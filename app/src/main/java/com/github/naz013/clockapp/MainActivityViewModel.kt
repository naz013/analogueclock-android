package com.github.naz013.clockapp

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.clockapp.data.ClockData
import com.github.naz013.analoguewatch.TimeData
import com.github.naz013.clockapp.data.TimeZoneData
import com.github.naz013.clockapp.util.ClocksManager
import com.github.naz013.clockapp.util.Prefs
import com.github.naz013.clockapp.util.UiModeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel(), DefaultLifecycleObserver {

    private val prefs = Prefs.getInstance()
    private val clocksManager = ClocksManager(prefs)
    private val uiModeManager = UiModeManager(prefs)

    private val _uiMode = MutableLiveData<Int>()
    val uiMode: LiveData<Int> = _uiMode

    private val _time = MutableLiveData<TimeData>()
    val time: LiveData<TimeData> = _time

    private val _animateClock = MutableLiveData<TimeData>()
    val animateClock: LiveData<TimeData> = _animateClock

    private val _mainClock = MutableLiveData<ClockData>()
    val mainClock: LiveData<ClockData> = _mainClock

    private val _clocks = MutableLiveData<List<ClockData>>()
    val clocks: LiveData<List<ClockData>> = _clocks

    private val _timeZones = MutableLiveData<List<TimeZoneData>>()
    val timeZones: LiveData<List<TimeZoneData>> = _timeZones

    private val secondsTimer = ClockTimer(1000L) { updateMainClock() }
    private val minuteTimer = ClockTimer(60000L) { updateClocks() }

    fun is12HourFormatEnabled() = prefs.is12HourFormat()

    fun toggleHourFormat(isEnabled: Boolean) {
        if (isEnabled == is12HourFormatEnabled()) return
        prefs.set12HourFormat(isEnabled)
        updateClocks()
    }

    fun toggleUiMode() {
        _uiMode.postValue(uiModeManager.toggleUiMode())
    }

    fun loadTimeZones() {
        viewModelScope.launch {
            val timeZones = withContext(Dispatchers.Default) { clocksManager.getTimeZoneList() }
            _timeZones.postValue(timeZones)
        }
    }

    fun onItemClick(clockData: ClockData) {
        viewModelScope.launch {
            prefs.saveMainClockId(clockData.timeZone.id)

            pauseTimer()

            val mainClock = withContext(Dispatchers.Default) { clocksManager.getCurrentClock() }
            _mainClock.postValue(mainClock)

            val userClocks = withContext(Dispatchers.Default) { clocksManager.getUserClocks() }
            _clocks.postValue(userClocks)

            val time = clocksManager.getCurrentClock().dateTime
            _animateClock.postValue(
                TimeData(
                    hour = time.hourOfDay,
                    minute = time.minuteOfHour,
                    second = time.secondOfMinute
                )
            )
        }
    }

    fun toggleTimeZone(timeZoneData: TimeZoneData) {
        if (timeZoneData.isSelected) {
            addClock(timeZoneData)
        } else {
            removeClock(timeZoneData)
        }
    }

    private fun updateMainClock() {
        val time = clocksManager.getCurrentClock().dateTime
        _time.postValue(
            TimeData(
                hour = time.hourOfDay,
                minute = time.minuteOfHour,
                second = time.secondOfMinute
            )
        )
    }

    private fun addClock(timeZoneData: TimeZoneData) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val clockIds = prefs.getClockIds().toMutableList().apply {
                    add(timeZoneData.timeZone.id)
                }
                prefs.saveClockIds(clockIds)
            }
            updateClocks()
        }
    }

    private fun removeClock(timeZoneData: TimeZoneData) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val clockIds = prefs.getClockIds().toMutableList().apply {
                    remove(timeZoneData.timeZone.id)
                }
                prefs.saveClockIds(clockIds)
            }
            updateClocks()
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        _uiMode.postValue(uiModeManager.getUiMode())
        updateClocks()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        startClock()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        stopClock()
    }

    private fun updateClocks() {
        viewModelScope.launch {
            val mainClock = withContext(Dispatchers.Default) { clocksManager.getCurrentClock() }
            _mainClock.postValue(mainClock)

            val userClocks = withContext(Dispatchers.Default) { clocksManager.getUserClocks() }
            _clocks.postValue(userClocks)
        }
    }

    private fun startClock() {
        stopClock()
        secondsTimer.start()
        minuteTimer.start(clocksManager.getNumberOfSecondsUntilEndOfMinute() * 1000L)
    }

    private fun stopClock() {
        secondsTimer.stop()
        minuteTimer.stop()
    }

    fun resumeTimer() {
        secondsTimer.start()
    }

    private fun pauseTimer() {
        secondsTimer.stop()
    }
}