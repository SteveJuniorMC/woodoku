package com.game.woodoku.view

import android.content.ClipData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
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

    // Grid cell size for drag shadow (set by MainActivity)
    var gridCellSize = 0f

    // Prevent double-drag
    private var isDragging = false

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
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isDragging) return false

                val state = gameState ?: return false
                val slotIndex = (event.x / shapeSlotWidth).toInt().coerceIn(0, 2)
                val shape = state.availableShapes.getOrNull(slotIndex) ?: return false

                isDragging = true

                val dragData = GameView.DragData(shape, slotIndex)
                val useCellSize = if (gridCellSize > 0) gridCellSize else cellSize
                val shadowBuilder = ShapeDragShadowBuilder(context, shape, useCellSize)
                val clipData = ClipData.newPlainText("shape", slotIndex.toString())

                startDragAndDrop(clipData, shadowBuilder, dragData, 0)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return super.onTouchEvent(event)
    }

    fun onDragEnded() {
        isDragging = false
    }

    private class ShapeDragShadowBuilder(
        private val context: Context,
        private val shape: Shape,
        private val cellSize: Float
    ) : DragShadowBuilder() {

        private val cellPadding = 2f
        private val cornerRadius = 4f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val cellRect = RectF()

        override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
            val width = (shape.width * cellSize).toInt()
            val height = (shape.height * cellSize).toInt()
            outShadowSize.set(width, height)
            // Touch point at bottom center so shape appears above finger
            // Use smaller offset (0.5 cells) to allow placing in bottom rows
            outShadowTouchPoint.set(width / 2, height + (cellSize * 0.5f).toInt())
        }

        override fun onDrawShadow(canvas: Canvas) {
            paint.color = ContextCompat.getColor(context, shape.colorResId)

            for ((dx, dy) in shape.cells) {
                val left = dx * cellSize + cellPadding
                val top = dy * cellSize + cellPadding
                val right = (dx + 1) * cellSize - cellPadding
                val bottom = (dy + 1) * cellSize - cellPadding

                cellRect.set(left, top, right, bottom)
                canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, paint)
            }
        }
    }
}
