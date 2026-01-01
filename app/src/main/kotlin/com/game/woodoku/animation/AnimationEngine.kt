package com.game.woodoku.animation

import android.graphics.Canvas

class AnimationEngine {
    private val animations = mutableListOf<GameAnimation>()
    private val pendingAdditions = mutableListOf<GameAnimation>()

    var screenShake: ScreenShake? = null
        private set

    fun addAnimation(animation: GameAnimation) {
        pendingAdditions.add(animation)
    }

    fun triggerScreenShake(intensity: Float, duration: Long = 200L) {
        screenShake = ScreenShake(intensity, duration)
    }

    fun update(deltaTime: Float): Boolean {
        // Add pending animations
        animations.addAll(pendingAdditions)
        pendingAdditions.clear()

        // Update screen shake
        screenShake?.let {
            it.update(deltaTime)
            if (it.isComplete) {
                screenShake = null
            }
        }

        // Update all animations
        animations.forEach { it.update(deltaTime) }

        // Remove completed animations
        animations.removeAll { it.isComplete }

        return animations.isNotEmpty() || screenShake != null
    }

    fun render(canvas: Canvas) {
        animations.forEach { it.render(canvas) }
    }

    fun clear() {
        animations.clear()
        pendingAdditions.clear()
        screenShake = null
    }

    fun hasActiveAnimations(): Boolean = animations.isNotEmpty() || screenShake != null
}
