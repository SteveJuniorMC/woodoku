package com.game.woodoku.animation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.ColorUtils

class LineClearAnimation(
    private val cells: Set<Pair<Int, Int>>,
    private val cellColors: Map<Pair<Int, Int>, Int>,
    private val cellSize: Float,
    private val gridOffset: Float,
    private val cellPadding: Float = 2f,
    private val cornerRadius: Float = 4f
) : GameAnimation {

    private var elapsed = 0f
    private val duration = 400f // ms

    private val glowDuration = 150f
    private val fadeDuration = duration - glowDuration

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellRect = RectF()

    override val isComplete: Boolean
        get() = elapsed >= duration

    override fun update(deltaTime: Float) {
        elapsed += deltaTime
    }

    override fun render(canvas: Canvas) {
        val progress = (elapsed / duration).coerceIn(0f, 1f)

        for ((x, y) in cells) {
            val color = cellColors[Pair(x, y)] ?: continue

            val left = gridOffset + x * cellSize + cellPadding
            val top = gridOffset + y * cellSize + cellPadding
            val right = gridOffset + (x + 1) * cellSize - cellPadding
            val bottom = gridOffset + (y + 1) * cellSize - cellPadding
            cellRect.set(left, top, right, bottom)

            if (elapsed < glowDuration) {
                // Glow phase: brighten the cell
                val glowProgress = elapsed / glowDuration
                val brightenedColor = ColorUtils.blendARGB(color, Color.WHITE, glowProgress * 0.5f)
                paint.color = brightenedColor

                // Slight scale up
                val scale = 1f + glowProgress * 0.1f
                canvas.save()
                canvas.scale(scale, scale, cellRect.centerX(), cellRect.centerY())
                canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, paint)
                canvas.restore()
            } else {
                // Fade phase: fade out with scale down
                val fadeProgress = (elapsed - glowDuration) / fadeDuration
                val alpha = (255 * (1f - fadeProgress)).toInt().coerceIn(0, 255)
                val scale = 1.1f - fadeProgress * 0.3f

                paint.color = color
                paint.alpha = alpha

                canvas.save()
                canvas.scale(scale, scale, cellRect.centerX(), cellRect.centerY())
                canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, paint)
                canvas.restore()
            }
        }
    }
}
