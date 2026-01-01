package com.game.woodoku.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.game.woodoku.R
import com.game.woodoku.game.GameLogic
import com.game.woodoku.game.GameState
import com.game.woodoku.game.GameState.Companion.GRID_SIZE
import com.game.woodoku.game.GameState.Companion.SECTION_SIZE
import com.game.woodoku.game.Shape

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gameState: GameState? = null
    private var gameLogic: GameLogic? = null

    var cellSize = 0f
        private set
    private var gridOffset = 0f
    private val cellPadding = 2f
    private val cornerRadius = 4f

    private var ghostShape: Shape? = null
    private var ghostX = -1
    private var ghostY = -1
    private var ghostShapeIndex = -1
    private var isGhostValid = false

    private val gridBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_background)
    }

    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_line)
        strokeWidth = 1f
    }

    private val gridLineBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_line_bold)
        strokeWidth = 2f
    }

    private val cellEmptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cell_empty)
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val ghostValidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghost_valid)
    }

    private val ghostInvalidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghost_invalid)
    }

    private val cellRect = RectF()

    interface OnShapePlacedListener {
        fun onShapePlaced(shapeIndex: Int, gridX: Int, gridY: Int)
    }

    interface OnDragEndedListener {
        fun onDragEnded()
    }

    var shapePlacedListener: OnShapePlacedListener? = null
    var dragEndedListener: OnDragEndedListener? = null

    init {
        setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    val data = event.localState as? DragData
                    if (data != null) {
                        ghostShape = data.shape
                        ghostShapeIndex = data.index
                        true
                    } else false
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    updateGhostPosition(event.x, event.y)
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    clearGhost()
                    true
                }
                DragEvent.ACTION_DROP -> {
                    if (isGhostValid && ghostX >= 0 && ghostY >= 0) {
                        shapePlacedListener?.onShapePlaced(ghostShapeIndex, ghostX, ghostY)
                    }
                    clearGhost()
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    clearGhost()
                    dragEndedListener?.onDragEnded()
                    true
                }
                else -> false
            }
        }
    }

    fun setGame(state: GameState, logic: GameLogic) {
        gameState = state
        gameLogic = logic
        invalidate()
    }

    fun refresh() {
        invalidate()
    }

    private fun updateGhostPosition(x: Float, y: Float) {
        val shape = ghostShape ?: return

        val gridX = ((x - gridOffset) / cellSize).toInt() - shape.width / 2
        val gridY = ((y - gridOffset) / cellSize).toInt() - shape.height / 2

        if (gridX != ghostX || gridY != ghostY) {
            ghostX = gridX
            ghostY = gridY
            isGhostValid = gameLogic?.canPlace(shape, gridX, gridY) ?: false
            invalidate()
        }
    }

    private fun clearGhost() {
        ghostShape = null
        ghostX = -1
        ghostY = -1
        ghostShapeIndex = -1
        isGhostValid = false
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = minOf(w, h)
        val padding = 16f
        cellSize = (size - padding * 2) / GRID_SIZE
        gridOffset = padding
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val state = gameState ?: return

        // Draw grid background
        canvas.drawRoundRect(
            gridOffset, gridOffset,
            gridOffset + cellSize * GRID_SIZE,
            gridOffset + cellSize * GRID_SIZE,
            8f, 8f, gridBgPaint
        )

        // Draw cells
        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                val left = gridOffset + x * cellSize + cellPadding
                val top = gridOffset + y * cellSize + cellPadding
                val right = gridOffset + (x + 1) * cellSize - cellPadding
                val bottom = gridOffset + (y + 1) * cellSize - cellPadding

                cellRect.set(left, top, right, bottom)

                val colorRes = state.grid[y][x]
                if (colorRes != 0) {
                    cellPaint.color = ContextCompat.getColor(context, colorRes)
                    canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, cellPaint)
                } else {
                    canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, cellEmptyPaint)
                }
            }
        }

        // Draw ghost preview
        ghostShape?.let { shape ->
            val paint = if (isGhostValid) ghostValidPaint else ghostInvalidPaint
            for ((dx, dy) in shape.cells) {
                val x = ghostX + dx
                val y = ghostY + dy
                if (x in 0 until GRID_SIZE && y in 0 until GRID_SIZE) {
                    val left = gridOffset + x * cellSize + cellPadding
                    val top = gridOffset + y * cellSize + cellPadding
                    val right = gridOffset + (x + 1) * cellSize - cellPadding
                    val bottom = gridOffset + (y + 1) * cellSize - cellPadding
                    cellRect.set(left, top, right, bottom)
                    canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, paint)
                }
            }
        }

        // Draw grid lines (3x3 sections)
        for (i in 0..SECTION_SIZE) {
            val pos = gridOffset + i * SECTION_SIZE * cellSize
            canvas.drawLine(pos, gridOffset, pos, gridOffset + GRID_SIZE * cellSize, gridLineBoldPaint)
            canvas.drawLine(gridOffset, pos, gridOffset + GRID_SIZE * cellSize, pos, gridLineBoldPaint)
        }
    }

    data class DragData(val shape: Shape, val index: Int)
}
