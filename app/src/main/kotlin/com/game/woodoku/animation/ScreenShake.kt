package com.game.woodoku.animation

import kotlin.random.Random

class ScreenShake(
    private val intensity: Float,
    private val duration: Long = 200L
) {
    var offsetX = 0f
        private set
    var offsetY = 0f
        private set

    private var elapsed = 0f
    private val durationMs = duration.toFloat()

    val isComplete: Boolean
        get() = elapsed >= durationMs

    fun update(deltaTime: Float) {
        elapsed += deltaTime

        if (!isComplete) {
            val progress = elapsed / durationMs
            val decay = 1f - progress
            val currentIntensity = intensity * decay

            offsetX = (Random.nextFloat() * 2 - 1) * currentIntensity
            offsetY = (Random.nextFloat() * 2 - 1) * currentIntensity
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }
}
