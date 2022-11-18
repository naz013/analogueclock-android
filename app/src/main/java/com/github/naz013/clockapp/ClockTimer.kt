package com.github.naz013.clockapp

import java.util.Timer
import java.util.TimerTask

class ClockTimer(
    private val repeatIntervalMillis: Long = 1000L,
    private val listener: () -> Unit
) {
    private var timer: Timer? = null

    fun start(delayMillis: Long = 0L) {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() { listener.invoke() }
        }, delayMillis, repeatIntervalMillis)
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }
}