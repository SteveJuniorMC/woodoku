package com.game.woodoku.game

import com.game.woodoku.game.GameState.Companion.GRID_SIZE
import com.game.woodoku.game.GameState.Companion.SECTION_SIZE

class GameLogic(private val state: GameState) {

    interface GameListener {
        fun onScoreChanged(score: Int, combo: Int, streak: Int)
        fun onShapesChanged()
        fun onGridChanged()
        fun onLinesCleared(count: Int)
        fun onGameOver()
    }

    var listener: GameListener? = null

    fun startNewGame() {
        state.reset()
        generateShapes()
        listener?.onGridChanged()
        listener?.onScoreChanged(state.score, state.combo, state.streak)
        listener?.onShapesChanged()
    }

    fun canPlace(shape: Shape, gridX: Int, gridY: Int): Boolean {
        for ((dx, dy) in shape.cells) {
            val x = gridX + dx
            val y = gridY + dy
            if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
                return false
            }
            if (state.grid[y][x] != 0) {
                return false
            }
        }
        return true
    }

    fun canPlaceAnywhere(shape: Shape): Boolean {
        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                if (canPlace(shape, x, y)) {
                    return true
                }
            }
        }
        return false
    }

    fun placeShape(shapeIndex: Int, gridX: Int, gridY: Int): Boolean {
        val shape = state.availableShapes[shapeIndex] ?: return false

        if (!canPlace(shape, gridX, gridY)) {
            return false
        }

        // Place the shape on the grid
        for ((dx, dy) in shape.cells) {
            val x = gridX + dx
            val y = gridY + dy
            state.grid[y][x] = shape.colorResId
        }

        // Remove shape from available
        state.availableShapes[shapeIndex] = null

        // Calculate base score for placing
        val placementScore = shape.cells.size * 10
        state.score += placementScore

        // Check and clear lines
        val linesCleared = checkAndClearLines()

        if (linesCleared > 0) {
            state.combo++
            val lineScore = linesCleared * 100 * state.combo
            state.score += lineScore
            listener?.onLinesCleared(linesCleared)
        } else {
            state.combo = 0
        }

        // Update streak
        state.streak++
        checkStreakBonus()

        listener?.onGridChanged()
        listener?.onScoreChanged(state.score, state.combo, state.streak)

        // Generate new shapes if all used
        if (state.availableShapes.all { it == null }) {
            generateShapes()
        }

        listener?.onShapesChanged()

        // Check game over
        if (checkGameOver()) {
            state.isGameOver = true
            listener?.onGameOver()
        }

        return true
    }

    private fun checkAndClearLines(): Int {
        val rowsToClear = mutableSetOf<Int>()
        val colsToClear = mutableSetOf<Int>()
        val boxesToClear = mutableSetOf<Pair<Int, Int>>()

        // Check rows
        for (y in 0 until GRID_SIZE) {
            if ((0 until GRID_SIZE).all { x -> state.grid[y][x] != 0 }) {
                rowsToClear.add(y)
            }
        }

        // Check columns
        for (x in 0 until GRID_SIZE) {
            if ((0 until GRID_SIZE).all { y -> state.grid[y][x] != 0 }) {
                colsToClear.add(x)
            }
        }

        // Check 3x3 boxes
        for (boxY in 0 until SECTION_SIZE) {
            for (boxX in 0 until SECTION_SIZE) {
                val startX = boxX * SECTION_SIZE
                val startY = boxY * SECTION_SIZE
                var boxFull = true
                outer@ for (dy in 0 until SECTION_SIZE) {
                    for (dx in 0 until SECTION_SIZE) {
                        if (state.grid[startY + dy][startX + dx] == 0) {
                            boxFull = false
                            break@outer
                        }
                    }
                }
                if (boxFull) {
                    boxesToClear.add(Pair(boxX, boxY))
                }
            }
        }

        // Clear the lines
        val cellsToClear = mutableSetOf<Pair<Int, Int>>()

        for (y in rowsToClear) {
            for (x in 0 until GRID_SIZE) {
                cellsToClear.add(Pair(x, y))
            }
        }

        for (x in colsToClear) {
            for (y in 0 until GRID_SIZE) {
                cellsToClear.add(Pair(x, y))
            }
        }

        for ((boxX, boxY) in boxesToClear) {
            val startX = boxX * SECTION_SIZE
            val startY = boxY * SECTION_SIZE
            for (dy in 0 until SECTION_SIZE) {
                for (dx in 0 until SECTION_SIZE) {
                    cellsToClear.add(Pair(startX + dx, startY + dy))
                }
            }
        }

        // Actually clear the cells
        for ((x, y) in cellsToClear) {
            state.grid[y][x] = 0
        }

        return rowsToClear.size + colsToClear.size + boxesToClear.size
    }

    private fun checkStreakBonus() {
        val bonus = when (state.streak) {
            10 -> 100
            25 -> 250
            50 -> 500
            100 -> 1000
            else -> 0
        }
        if (bonus > 0) {
            state.score += bonus
        }
    }

    private fun generateShapes() {
        for (i in state.availableShapes.indices) {
            if (state.availableShapes[i] == null) {
                state.availableShapes[i] = Shape.random()
            }
        }
    }

    private fun checkGameOver(): Boolean {
        for (shape in state.availableShapes) {
            if (shape != null && canPlaceAnywhere(shape)) {
                return false
            }
        }
        return true
    }

    fun getState(): GameState = state
}
