package com.game.woodoku.audio

import kotlin.math.PI
import kotlin.math.sin

object SoundGenerator {

    private const val SAMPLE_RATE = 44100

    fun generateBlockPlace(): ShortArray {
        return generateTone(440f, 50, 0.3f, fadeOut = true)
    }

    fun generateLineClear(): ShortArray {
        // Ascending sweep from 330Hz to 660Hz
        return generateSweep(330f, 660f, 150, 0.4f)
    }

    fun generateLineClearCombo(): ShortArray {
        // Higher pitch ascending sweep with harmonics
        return generateSweep(440f, 880f, 180, 0.5f, addHarmonic = true)
    }

    fun generateGameOver(): ShortArray {
        // Descending tone
        return generateSweep(440f, 110f, 400, 0.4f)
    }

    fun generateHighScore(): ShortArray {
        // Arpeggio: C-E-G-C (262, 330, 392, 523 Hz)
        val c = generateTone(262f, 100, 0.3f)
        val e = generateTone(330f, 100, 0.3f)
        val g = generateTone(392f, 100, 0.3f)
        val cHigh = generateTone(523f, 200, 0.4f, fadeOut = true)

        return c + e + g + cHigh
    }

    fun generateStreakMilestone(): ShortArray {
        // Quick achievement ping
        return generateTone(880f, 80, 0.3f, fadeOut = true)
    }

    private fun generateTone(
        frequency: Float,
        durationMs: Int,
        amplitude: Float,
        fadeOut: Boolean = false
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000f).toInt()
        val samples = ShortArray(numSamples)
        val twoPiF = 2.0 * PI * frequency / SAMPLE_RATE

        for (i in 0 until numSamples) {
            var value = sin(twoPiF * i).toFloat() * amplitude

            if (fadeOut) {
                val fadeProgress = i.toFloat() / numSamples
                value *= (1f - fadeProgress)
            }

            samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
        }

        return samples
    }

    private fun generateSweep(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        amplitude: Float,
        addHarmonic: Boolean = false
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * durationMs / 1000f).toInt()
        val samples = ShortArray(numSamples)

        var phase = 0.0
        var harmonicPhase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val fadeAmount = 1f - progress * 0.5f

            var value = sin(phase).toFloat() * amplitude * fadeAmount

            if (addHarmonic) {
                value += sin(harmonicPhase).toFloat() * amplitude * 0.3f * fadeAmount
            }

            samples[i] = (value * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            phase += 2.0 * PI * currentFreq / SAMPLE_RATE
            if (addHarmonic) {
                harmonicPhase += 2.0 * PI * (currentFreq * 2) / SAMPLE_RATE
            }
        }

        return samples
    }

    private operator fun ShortArray.plus(other: ShortArray): ShortArray {
        val result = ShortArray(this.size + other.size)
        System.arraycopy(this, 0, result, 0, this.size)
        System.arraycopy(other, 0, result, this.size, other.size)
        return result
    }
}
