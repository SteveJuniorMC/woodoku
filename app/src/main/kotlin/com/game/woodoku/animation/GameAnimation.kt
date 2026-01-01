package com.game.woodoku.animation

import android.graphics.Canvas

interface GameAnimation {
    val isComplete: Boolean
    fun update(deltaTime: Float)
    fun render(canvas: Canvas)
}
