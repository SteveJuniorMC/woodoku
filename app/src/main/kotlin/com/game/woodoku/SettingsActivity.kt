package com.game.woodoku

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.game.woodoku.data.HighScoreManager
import com.game.woodoku.data.SettingsManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var highScoreManager: HighScoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingsManager = SettingsManager(this)
        highScoreManager = HighScoreManager(this)

        setupBackButton()
        setupSwitches()
        setupResetButton()
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupSwitches() {
        val soundSwitch = findViewById<SwitchMaterial>(R.id.soundSwitch)
        val vibrationSwitch = findViewById<SwitchMaterial>(R.id.vibrationSwitch)

        soundSwitch.isChecked = settingsManager.isSoundEnabled
        vibrationSwitch.isChecked = settingsManager.isVibrationEnabled

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isSoundEnabled = isChecked
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.isVibrationEnabled = isChecked
        }
    }

    private fun setupResetButton() {
        findViewById<Button>(R.id.resetHighScoreButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_high_score_title))
                .setMessage(getString(R.string.reset_high_score_message))
                .setPositiveButton(getString(R.string.reset)) { _, _ ->
                    highScoreManager.resetHighScore()
                    Toast.makeText(this, getString(R.string.high_score_reset), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}
