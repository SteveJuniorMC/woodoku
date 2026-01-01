package com.game.woodoku.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.game.woodoku.data.SettingsManager

class VibrationHelper(context: Context, private val settingsManager: SettingsManager) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrateBlockPlace() {
        if (!settingsManager.isVibrationEnabled) return
        vibrate(20, 80)
    }

    fun vibrateLineClear(linesCleared: Int) {
        if (!settingsManager.isVibrationEnabled) return
        val duration = (30L * linesCleared).coerceAtMost(100L)
        val amplitude = (100 + linesCleared * 20).coerceAtMost(200)
        vibrate(duration, amplitude)
    }

    fun vibrateCombo(combo: Int) {
        if (!settingsManager.isVibrationEnabled) return
        if (combo >= 2) {
            vibrate(50L + combo * 10, 150)
        }
    }

    fun vibrateGameOver() {
        if (!settingsManager.isVibrationEnabled) return
        vibratePattern(longArrayOf(0, 100, 50, 100, 50, 150))
    }

    fun vibrateHighScore() {
        if (!settingsManager.isVibrationEnabled) return
        vibratePattern(longArrayOf(0, 50, 50, 50, 50, 100, 50, 150))
    }

    private fun vibrate(duration: Long, amplitude: Int) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(pattern, -1)
            }
        }
    }
}
