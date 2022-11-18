package com.github.naz013.clockapp

import android.app.Application
import com.github.naz013.clockapp.util.Prefs

class ClockApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}