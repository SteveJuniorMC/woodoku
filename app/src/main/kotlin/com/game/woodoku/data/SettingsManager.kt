package com.game.woodoku.data

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("woodoku_settings", Context.MODE_PRIVATE)

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    companion object {
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
    }
}
