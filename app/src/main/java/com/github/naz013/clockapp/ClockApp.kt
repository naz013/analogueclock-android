package com.github.naz013.clockapp

import android.app.Application

class ClockApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}