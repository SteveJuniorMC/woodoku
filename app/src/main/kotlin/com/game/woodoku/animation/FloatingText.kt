package com.game.woodoku.animation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class FloatingText(
    private val text: String,
    private val startX: Float,
    private val startY: Float,
    private val textColor: Int = Color.WHITE,
    private val textSize: Float = 48f,
    private val duration: Float = 800f
) : GameAnimation {

    private var elapsed = 0f
    private var currentY = startY

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        this.textSize = this@FloatingText.textSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    override val isComplete: Boolean
        get() = elapsed >= duration

    override fun update(deltaTime: Float) {
        elapsed += deltaTime
        currentY = startY - (elapsed / duration) * 80f
    }

    override fun render(canvas: Canvas) {
        val progress = elapsed / duration
        val fadeStart = 0.6f

        // Scale animation: start small, grow quickly, then stay
        val scaleProgress = (progress * 3f).coerceAtMost(1f)
        val scale = 0.5f + scaleProgress * 0.5f

        // Fade out in the last 40%
        val alpha = if (progress > fadeStart) {
            ((1f - progress) / (1f - fadeStart)).coerceIn(0f, 1f)
        } else {
            1f
        }

        paint.alpha = (alpha * 255).toInt()

        canvas.save()
        canvas.scale(scale, scale, startX, currentY)
        canvas.drawText(text, startX, currentY, paint)
        canvas.restore()
    }

    companion object {
        val COLOR_GOLD = Color.rgb(255, 215, 0)
        val COLOR_ORANGE = Color.rgb(255, 165, 0)
        val COLOR_COMBO = Color.rgb(255, 255, 100)
        val COLOR_STREAK = Color.rgb(255, 100, 255)
    }
}
