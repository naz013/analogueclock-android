package com.github.naz013.clockapp.util

import android.content.Context

class Prefs private constructor(context: Context) {

    private val shared = context.getSharedPreferences("clock_app", Context.MODE_PRIVATE)

    fun getUiMode(): Int {
        return shared.getInt(KEY_UI_MODE, 0)
    }

    fun saveUiMode(value: Int) {
        shared.edit().putInt(KEY_UI_MODE, value).apply()
    }

    fun is12HourFormat(): Boolean {
        return shared.getBoolean(KEY_12_HOUR_FORMAT, true)
    }

    fun set12HourFormat(value: Boolean) {
        shared.edit().putBoolean(KEY_12_HOUR_FORMAT, value).apply()
    }

    fun getMainClockId(): String? {
        return shared.getString(KEY_MAIN_CLOCK, null)
    }

    fun saveMainClockId(id: String) {
        shared.edit().putString(KEY_MAIN_CLOCK, id).apply()
    }

    fun getClockIds(): List<String> {
        return shared.getStringSet(KEY_CLOCKS, null)?.toList() ?: emptyList()
    }

    fun saveClockIds(ids: List<String>) {
        shared.edit().putStringSet(KEY_CLOCKS, ids.toSet()).apply()
    }

    fun isCatModeEnabled(): Boolean {
        return shared.getBoolean(KEY_CAT_MODE, false)
    }

    fun setCatModeEnabled(value: Boolean) {
        shared.edit().putBoolean(KEY_CAT_MODE, value).apply()
    }

    companion object {

        private const val KEY_MAIN_CLOCK = "main_clock"
        private const val KEY_CLOCKS = "clocks"
        private const val KEY_12_HOUR_FORMAT = "twelve_hour_format"
        private const val KEY_UI_MODE = "ui_mode"
        private const val KEY_CAT_MODE = "cat_mode"

        private var instance: Prefs? = null

        fun init(context: Context): Prefs {
            if (instance == null) {
                instance = Prefs(context)
            }
            return instance!!
        }

        fun getInstance(): Prefs {
            return instance!!
        }
    }
}