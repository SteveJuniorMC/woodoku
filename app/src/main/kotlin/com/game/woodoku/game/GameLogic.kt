package com.game.woodoku.game

import com.game.woodoku.game.GameState.Companion.GRID_SIZE
import com.game.woodoku.game.GameState.Companion.SECTION_SIZE

class GameLogic(private val state: GameState) {

    interface GameListener {
        fun onScoreChanged(score: Int, combo: Int, streak: Int, pointsGained: Int)
        fun onShapesChanged()
        fun onGridChanged()
        fun onShapePlaced(cells: List<Pair<Int, Int>>, color: Int)
        fun onLinesCleared(count: Int, clearedCells: Set<Pair<Int, Int>>, cellColors: Map<Pair<Int, Int>, Int>)
        fun onStreakMilestone(streak: Int, bonus: Int)
        fun onGameOver(isNewHighScore: Boolean)
    }

    var listener: GameListener? = null

    var highScoreChecker: ((Int) -> Boolean)? = null

    fun startNewGame() {
        state.reset()
        generateShapes()
        listener?.onGridChanged()
        listener?.onScoreChanged(state.score, state.combo, state.streak, 0)
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

        // Calculate placed cells for animation
        val placedCells = shape.cells.map { (dx, dy) -> Pair(gridX + dx, gridY + dy) }

        // Place the shape on the grid
        for ((dx, dy) in shape.cells) {
            val x = gridX + dx
            val y = gridY + dy
            state.grid[y][x] = shape.colorResId
        }

        // Notify shape placed for animation
        listener?.onShapePlaced(placedCells, shape.colorResId)

        // Remove shape from available
        state.availableShapes[shapeIndex] = null

        // Check and clear lines
        val (linesCleared, clearedCells, cellColors) = checkAndClearLines()

        var totalPointsGained = 0

        if (linesCleared > 0) {
            state.combo++
            state.streak++
            val lineScore = linesCleared * 100 * state.combo
            state.score += lineScore
            totalPointsGained += lineScore
            listener?.onLinesCleared(linesCleared, clearedCells, cellColors)

            // Check streak bonus
            val streakBonus = getStreakBonus(state.streak)
            if (streakBonus > 0) {
                state.score += streakBonus
                totalPointsGained += streakBonus
                listener?.onStreakMilestone(state.streak, streakBonus)
            }
        } else {
            state.combo = 0
            state.streak = 0
        }

        listener?.onGridChanged()
        listener?.onScoreChanged(state.score, state.combo, state.streak, totalPointsGained)

        // Generate new shapes if all used
        if (state.availableShapes.all { it == null }) {
            generateShapes()
        }

        listener?.onShapesChanged()

        // Check game over
        if (checkGameOver()) {
            state.isGameOver = true
            val isNewHighScore = highScoreChecker?.invoke(state.score) ?: false
            listener?.onGameOver(isNewHighScore)
        }

        return true
    }

    private fun checkAndClearLines(): Triple<Int, Set<Pair<Int, Int>>, Map<Pair<Int, Int>, Int>> {
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

        // Collect cells to clear
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

        // Capture cell colors before clearing
        val cellColors = cellsToClear.associateWith { (x, y) -> state.grid[y][x] }

        // Actually clear the cells
        for ((x, y) in cellsToClear) {
            state.grid[y][x] = 0
        }

        val linesCleared = rowsToClear.size + colsToClear.size + boxesToClear.size
        return Triple(linesCleared, cellsToClear, cellColors)
    }

    private fun getStreakBonus(streak: Int): Int {
        return when (streak) {
            10 -> 100
            25 -> 250
            50 -> 500
            100 -> 1000
            else -> 0
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
