package com.game.woodoku.view

import android.content.ClipData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.game.woodoku.R
import com.game.woodoku.game.GameState
import com.game.woodoku.game.Shape

class ShapeSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gameState: GameState? = null
    private var shapeSlotWidth = 0f
    private var cellSize = 0f
    private val cellPadding = 2f
    private val cornerRadius = 4f

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_background)
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellRect = RectF()

    fun setGameState(state: GameState) {
        gameState = state
        invalidate()
    }

    fun refresh() {
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * 0.35f).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shapeSlotWidth = w / 3f
        cellSize = minOf(shapeSlotWidth / 6f, h / 6f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val state = gameState ?: return

        for (i in 0 until 3) {
            val shape = state.availableShapes.getOrNull(i)
            val slotCenterX = shapeSlotWidth * i + shapeSlotWidth / 2
            val slotCenterY = height / 2f

            if (shape != null) {
                drawShape(canvas, shape, slotCenterX, slotCenterY)
            }
        }
    }

    private fun drawShape(canvas: Canvas, shape: Shape, centerX: Float, centerY: Float) {
        val shapeWidth = shape.width * cellSize
        val shapeHeight = shape.height * cellSize
        val startX = centerX - shapeWidth / 2
        val startY = centerY - shapeHeight / 2

        cellPaint.color = ContextCompat.getColor(context, shape.colorResId)

        for ((dx, dy) in shape.cells) {
            val left = startX + dx * cellSize + cellPadding
            val top = startY + dy * cellSize + cellPadding
            val right = startX + (dx + 1) * cellSize - cellPadding
            val bottom = startY + (dy + 1) * cellSize - cellPadding

            cellRect.set(left, top, right, bottom)
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, cellPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val state = gameState ?: return false
            val slotIndex = (event.x / shapeSlotWidth).toInt().coerceIn(0, 2)
            val shape = state.availableShapes.getOrNull(slotIndex) ?: return false

            val dragData = GameView.DragData(shape, slotIndex)
            val shadowBuilder = DragShadowBuilder(this)
            val clipData = ClipData.newPlainText("shape", slotIndex.toString())

            startDragAndDrop(clipData, shadowBuilder, dragData, 0)
            return true
        }
        return super.onTouchEvent(event)
    }
}
