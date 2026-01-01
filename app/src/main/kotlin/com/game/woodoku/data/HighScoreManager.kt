package com.game.woodoku.data

import android.content.Context

class HighScoreManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHighScore(): Int {
        return prefs.getInt(KEY_HIGH_SCORE, 0)
    }

    fun saveHighScore(score: Int): Boolean {
        val currentHigh = getHighScore()
        return if (score > currentHigh) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
            true
        } else {
            false
        }
    }

    companion object {
        private const val PREFS_NAME = "woodoku_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
    }
}
