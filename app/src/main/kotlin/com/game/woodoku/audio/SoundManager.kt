package com.game.woodoku.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.game.woodoku.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundManager(context: Context, private val settingsManager: SettingsManager) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val sounds = mutableMapOf<Sound, ShortArray>()

    enum class Sound {
        BLOCK_PLACE,
        LINE_CLEAR,
        LINE_CLEAR_COMBO,
        GAME_OVER,
        HIGH_SCORE,
        STREAK_MILESTONE
    }

    init {
        // Generate sounds on background thread
        scope.launch {
            sounds[Sound.BLOCK_PLACE] = SoundGenerator.generateBlockPlace()
            sounds[Sound.LINE_CLEAR] = SoundGenerator.generateLineClear()
            sounds[Sound.LINE_CLEAR_COMBO] = SoundGenerator.generateLineClearCombo()
            sounds[Sound.GAME_OVER] = SoundGenerator.generateGameOver()
            sounds[Sound.HIGH_SCORE] = SoundGenerator.generateHighScore()
            sounds[Sound.STREAK_MILESTONE] = SoundGenerator.generateStreakMilestone()
        }
    }

    fun play(sound: Sound, pitchMultiplier: Float = 1f) {
        if (!settingsManager.isSoundEnabled) return

        val samples = sounds[sound] ?: return

        scope.launch {
            playSound(samples, pitchMultiplier)
        }
    }

    fun playBlockPlace() = play(Sound.BLOCK_PLACE)

    fun playLineClear() {
        play(Sound.LINE_CLEAR)
    }

    fun playGameOver() = play(Sound.GAME_OVER)

    fun playHighScore() = play(Sound.HIGH_SCORE)

    fun playStreakMilestone() = play(Sound.STREAK_MILESTONE)

    private fun playSound(samples: ShortArray, pitchMultiplier: Float) {
        val sampleRate = (44100 * pitchMultiplier).toInt()

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferSize, samples.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Wait for playback to complete
            val durationMs = (samples.size * 1000L / sampleRate)
            Thread.sleep(durationMs + 50)
        } finally {
            audioTrack.stop()
            audioTrack.release()
        }
    }
}
