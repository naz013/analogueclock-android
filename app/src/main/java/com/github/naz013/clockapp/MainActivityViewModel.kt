package com.github.naz013.clockapp

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel(), DefaultLifecycleObserver {

    private val prefs = Prefs.getInstance()
    private val clocksManager = ClocksManager(prefs)
    private val uiModeManager = UiModeManager(prefs)

    private val _uiMode = MutableLiveData<Int>()
    val uiMode: LiveData<Int> = _uiMode

    private val _time = MutableLiveData<Long>()
    val time: LiveData<Long> = _time

    private val _mainClock = MutableLiveData<ClockData>()
    val mainClock: LiveData<ClockData> = _mainClock

    private val _clocks = MutableLiveData<List<ClockData>>()
    val clocks: LiveData<List<ClockData>> = _clocks

    private val secondsTimer = ClockTimer(1000L) {
        _time.postValue(System.currentTimeMillis())
    }
    private val minuteTimer = ClockTimer(60000L) {
        updateClocks()
    }

    fun toggleUiMode() {
        _uiMode.postValue(uiModeManager.toggleUiMode())
    }

    fun loadTimeZones() {

    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        _uiMode.postValue(uiModeManager.getUiMode())

        viewModelScope.launch {
            val mainClock = withContext(Dispatchers.Default) { clocksManager.getCurrentClock() }
            _mainClock.postValue(mainClock)

            val timeZones = withContext(Dispatchers.Default) { clocksManager.loadAllTimeZones() }
        }
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
            val mainClock = withContext(Dispatchers.IO) { clocksManager.getCurrentClock() }
            _mainClock.postValue(mainClock)
        }
    }

    private fun startClock() {
        stopClock()
        secondsTimer.start()
        minuteTimer.start(clocksManager.getNumberOfSecondsUntilEndOfMinute() * 1000L)
        log("startClock")
    }

    private fun stopClock() {
        log("stopClock")
        secondsTimer.stop()
        minuteTimer.stop()
    }

    private fun log(message: String) {
        Log.d("MainActivityVM", message)
    }
}