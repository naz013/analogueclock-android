package com.github.naz013.clockapp.util

import androidx.appcompat.app.AppCompatDelegate

class UiModeManager(private val prefs: Prefs) {

    fun getUiMode(): Int {
        return prefs.getUiMode().takeIf { it != 0 } ?: LIGHT
    }

    fun toggleUiMode(): Int {
        val newMode = when (getUiMode()) {
            DARK -> AUTO
            LIGHT -> DARK
            AUTO -> LIGHT
            else -> LIGHT
        }
        prefs.saveUiMode(newMode)
        return newMode
    }

    companion object {
        private const val DARK = AppCompatDelegate.MODE_NIGHT_YES
        private const val LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        private const val AUTO = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}