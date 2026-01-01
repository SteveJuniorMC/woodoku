package com.game.woodoku.animation

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

class ParticleSystem(
    private val cells: Set<Pair<Int, Int>>,
    private val cellColors: Map<Pair<Int, Int>, Int>,
    private val cellSize: Float,
    private val gridOffset: Float,
    private val particlesPerCell: Int = 8
) : GameAnimation {

    private data class Particle(
        var x: Float,
        var y: Float,
        var velocityX: Float,
        var velocityY: Float,
        var alpha: Float,
        var size: Float,
        var color: Int,
        var life: Float
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val duration = 600f // ms
    private var elapsed = 0f

    init {
        for ((cellX, cellY) in cells) {
            val color = cellColors[Pair(cellX, cellY)] ?: continue
            val centerX = gridOffset + (cellX + 0.5f) * cellSize
            val centerY = gridOffset + (cellY + 0.5f) * cellSize

            repeat(particlesPerCell) {
                val angle = Random.nextFloat() * Math.PI.toFloat() * 2
                val speed = Random.nextFloat() * 0.3f + 0.15f
                particles.add(
                    Particle(
                        x = centerX,
                        y = centerY,
                        velocityX = kotlin.math.cos(angle) * speed,
                        velocityY = kotlin.math.sin(angle) * speed,
                        alpha = 1f,
                        size = Random.nextFloat() * 4f + 3f,
                        color = color,
                        life = 1f
                    )
                )
            }
        }
    }

    override val isComplete: Boolean
        get() = elapsed >= duration

    override fun update(deltaTime: Float) {
        elapsed += deltaTime

        val gravity = 0.0008f

        for (particle in particles) {
            particle.x += particle.velocityX * deltaTime
            particle.y += particle.velocityY * deltaTime
            particle.velocityY += gravity * deltaTime

            val lifeProgress = elapsed / duration
            particle.life = 1f - lifeProgress
            particle.alpha = particle.life
            particle.size *= 0.995f
        }
    }

    override fun render(canvas: Canvas) {
        for (particle in particles) {
            if (particle.alpha <= 0 || particle.size <= 0) continue

            paint.color = particle.color
            paint.alpha = (particle.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(particle.x, particle.y, particle.size, paint)
        }
    }
}
