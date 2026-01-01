package com.game.woodoku.game

import com.game.woodoku.R

data class Shape(
    val cells: List<Pair<Int, Int>>,
    val colorResId: Int
) {
    val width: Int get() = cells.maxOf { it.first } - cells.minOf { it.first } + 1
    val height: Int get() = cells.maxOf { it.second } - cells.minOf { it.second } + 1

    fun normalized(): Shape {
        val minX = cells.minOf { it.first }
        val minY = cells.minOf { it.second }
        return copy(cells = cells.map { Pair(it.first - minX, it.second - minY) })
    }

    companion object {
        private val colors = listOf(
            R.color.block_red,
            R.color.block_orange,
            R.color.block_yellow,
            R.color.block_green,
            R.color.block_blue,
            R.color.block_purple,
            R.color.block_pink,
            R.color.block_teal
        )

        // Single dot
        private val DOT = listOf(Pair(0, 0))

        // Dominoes
        private val DOMINO_H = listOf(Pair(0, 0), Pair(1, 0))
        private val DOMINO_V = listOf(Pair(0, 0), Pair(0, 1))

        // Trominoes
        private val TROMINO_I_H = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0))
        private val TROMINO_I_V = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2))
        private val TROMINO_L1 = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1))
        private val TROMINO_L2 = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1))
        private val TROMINO_L3 = listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1))
        private val TROMINO_L4 = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 1))

        // Small square
        private val SQUARE_2X2 = listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1))

        // Tetrominoes
        private val TETRO_I_H = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0))
        private val TETRO_I_V = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3))
        private val TETRO_L1 = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2))
        private val TETRO_L2 = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(0, 1))
        private val TETRO_L3 = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(1, 2))
        private val TETRO_L4 = listOf(Pair(2, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1))
        private val TETRO_J1 = listOf(Pair(1, 0), Pair(1, 1), Pair(0, 2), Pair(1, 2))
        private val TETRO_J2 = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1))
        private val TETRO_J3 = listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(0, 2))
        private val TETRO_J4 = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1))
        private val TETRO_T1 = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(1, 1))
        private val TETRO_T2 = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(0, 2))
        private val TETRO_T3 = listOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(2, 1))
        private val TETRO_T4 = listOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2))
        private val TETRO_S1 = listOf(Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(1, 1))
        private val TETRO_S2 = listOf(Pair(0, 0), Pair(0, 1), Pair(1, 1), Pair(1, 2))
        private val TETRO_Z1 = listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(2, 1))
        private val TETRO_Z2 = listOf(Pair(1, 0), Pair(0, 1), Pair(1, 1), Pair(0, 2))

        // Large shapes
        private val BIG_L1 = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 2), Pair(2, 2))
        private val BIG_L2 = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(2, 1), Pair(2, 2))
        private val BIG_L3 = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(0, 1), Pair(0, 2))
        private val BIG_L4 = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(1, 0), Pair(2, 0))
        private val SQUARE_3X3 = listOf(
            Pair(0, 0), Pair(1, 0), Pair(2, 0),
            Pair(0, 1), Pair(1, 1), Pair(2, 1),
            Pair(0, 2), Pair(1, 2), Pair(2, 2)
        )

        // 5-cell shapes
        private val PENTO_I_H = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(4, 0))
        private val PENTO_I_V = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4))

        private val allShapes = listOf(
            DOT,
            DOMINO_H, DOMINO_V,
            TROMINO_I_H, TROMINO_I_V, TROMINO_L1, TROMINO_L2, TROMINO_L3, TROMINO_L4,
            SQUARE_2X2,
            TETRO_I_H, TETRO_I_V,
            TETRO_L1, TETRO_L2, TETRO_L3, TETRO_L4,
            TETRO_J1, TETRO_J2, TETRO_J3, TETRO_J4,
            TETRO_T1, TETRO_T2, TETRO_T3, TETRO_T4,
            TETRO_S1, TETRO_S2,
            TETRO_Z1, TETRO_Z2,
            BIG_L1, BIG_L2, BIG_L3, BIG_L4,
            SQUARE_3X3,
            PENTO_I_H, PENTO_I_V
        )

        fun random(): Shape {
            val cells = allShapes.random()
            val color = colors.random()
            return Shape(cells, color).normalized()
        }
    }
}
