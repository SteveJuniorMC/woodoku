package com.game.woodoku.animation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class BlockPlaceAnimation(
    private val cells: List<Pair<Int, Int>>,
    private val color: Int,
    private val cellSize: Float,
    private val gridOffset: Float,
    private val cellPadding: Float = 2f,
    private val cornerRadius: Float = 4f
) : GameAnimation {

    private var elapsed = 0f
    private val duration = 150f // ms

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellRect = RectF()

    override val isComplete: Boolean
        get() = elapsed >= duration

    override fun update(deltaTime: Float) {
        elapsed += deltaTime
    }

    override fun render(canvas: Canvas) {
        val progress = (elapsed / duration).coerceIn(0f, 1f)
        // Ease out: starts fast, slows down
        val easeOut = 1f - (1f - progress) * (1f - progress)
        val scale = 0.7f + easeOut * 0.3f

        paint.color = color

        for ((x, y) in cells) {
            val left = gridOffset + x * cellSize + cellPadding
            val top = gridOffset + y * cellSize + cellPadding
            val right = gridOffset + (x + 1) * cellSize - cellPadding
            val bottom = gridOffset + (y + 1) * cellSize - cellPadding
            cellRect.set(left, top, right, bottom)

            canvas.save()
            canvas.scale(scale, scale, cellRect.centerX(), cellRect.centerY())
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, paint)
            canvas.restore()
        }
    }
}
