package com.game.woodoku.animation

import android.graphics.Color

class ScorePopupManager(private val animationEngine: AnimationEngine) {

    fun showScoreGain(points: Int, x: Float, y: Float) {
        val color = when {
            points >= 500 -> FloatingText.COLOR_GOLD
            points >= 200 -> FloatingText.COLOR_ORANGE
            else -> Color.WHITE
        }
        val size = when {
            points >= 500 -> 56f
            points >= 200 -> 52f
            else -> 48f
        }
        animationEngine.addAnimation(
            FloatingText("+$points", x, y, color, size)
        )
    }

    fun showComboText(combo: Int, x: Float, y: Float) {
        if (combo > 1) {
            animationEngine.addAnimation(
                FloatingText("${combo}x COMBO!", x, y - 50f, FloatingText.COLOR_COMBO, 52f)
            )
        }
    }

    fun showStreakMilestone(streak: Int, bonus: Int, x: Float, y: Float) {
        val text = when (streak) {
            10 -> "10 STREAK! +$bonus"
            25 -> "25 STREAK! +$bonus"
            50 -> "AMAZING 50! +$bonus"
            100 -> "LEGENDARY! +$bonus"
            else -> return
        }
        animationEngine.addAnimation(
            FloatingText(text, x, y - 100f, FloatingText.COLOR_STREAK, 56f, 1200f)
        )
    }

    fun showNewHighScore(x: Float, y: Float) {
        animationEngine.addAnimation(
            FloatingText("NEW HIGH SCORE!", x, y, FloatingText.COLOR_GOLD, 64f, 1500f)
        )
    }
}
