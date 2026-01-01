package com.game.woodoku.game

data class GameState(
    val grid: Array<IntArray> = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } },
    val availableShapes: MutableList<Shape?> = mutableListOf(null, null, null),
    var score: Int = 0,
    var combo: Int = 0,
    var streak: Int = 0,
    var isGameOver: Boolean = false
) {
    companion object {
        const val GRID_SIZE = 9
        const val SECTION_SIZE = 3
    }

    fun copy(): GameState {
        return GameState(
            grid = Array(GRID_SIZE) { row -> grid[row].copyOf() },
            availableShapes = availableShapes.toMutableList(),
            score = score,
            combo = combo,
            streak = streak,
            isGameOver = isGameOver
        )
    }

    fun reset() {
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                grid[row][col] = 0
            }
        }
        availableShapes[0] = null
        availableShapes[1] = null
        availableShapes[2] = null
        score = 0
        combo = 0
        streak = 0
        isGameOver = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (availableShapes != other.availableShapes) return false
        if (score != other.score) return false
        if (combo != other.combo) return false
        if (streak != other.streak) return false
        if (isGameOver != other.isGameOver) return false
        return true
    }

    override fun hashCode(): Int {
        var result = grid.contentDeepHashCode()
        result = 31 * result + availableShapes.hashCode()
        result = 31 * result + score
        result = 31 * result + combo
        result = 31 * result + streak
        result = 31 * result + isGameOver.hashCode()
        return result
    }
}
