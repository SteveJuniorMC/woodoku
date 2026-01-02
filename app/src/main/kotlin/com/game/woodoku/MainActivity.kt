package com.game.woodoku

import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.game.woodoku.audio.SoundManager
import com.game.woodoku.data.HighScoreManager
import com.game.woodoku.data.SettingsManager
import com.game.woodoku.game.GameLogic
import com.game.woodoku.game.GameState
import com.game.woodoku.util.VibrationHelper
import com.game.woodoku.view.GameView
import com.game.woodoku.view.ShapeSelectorView

class MainActivity : AppCompatActivity(), GameLogic.GameListener {

    private lateinit var gameState: GameState
    private lateinit var gameLogic: GameLogic
    private lateinit var highScoreManager: HighScoreManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var soundManager: SoundManager
    private lateinit var vibrationHelper: VibrationHelper

    private lateinit var gameView: GameView
    private lateinit var shapeSelectorView: ShapeSelectorView
    private lateinit var scoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var comboText: TextView
    private lateinit var streakText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize managers
        highScoreManager = HighScoreManager(this)
        settingsManager = SettingsManager(this)
        soundManager = SoundManager(this, settingsManager)
        vibrationHelper = VibrationHelper(this, settingsManager)

        gameState = GameState()
        gameLogic = GameLogic(gameState)
        gameLogic.listener = this
        gameLogic.highScoreChecker = { score -> highScoreManager.saveHighScore(score) }

        gameView = findViewById(R.id.gameView)
        shapeSelectorView = findViewById(R.id.shapeSelectorView)
        scoreText = findViewById(R.id.scoreText)
        highScoreText = findViewById(R.id.highScoreText)
        comboText = findViewById(R.id.comboText)
        streakText = findViewById(R.id.streakText)

        gameView.setGame(gameState, gameLogic)
        shapeSelectorView.setGameState(gameState)

        gameView.shapePlacedListener = object : GameView.OnShapePlacedListener {
            override fun onShapePlaced(shapeIndex: Int, gridX: Int, gridY: Int) {
                gameLogic.placeShape(shapeIndex, gridX, gridY)
            }
        }

        gameView.dragEndedListener = object : GameView.OnDragEndedListener {
            override fun onDragEnded() {
                shapeSelectorView.onDragEnded()
            }
        }

        // Settings button
        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Pass grid cell size to shape selector after layout
        gameView.post {
            shapeSelectorView.gridCellSize = gameView.cellSize
        }

        // Set up root layout drag listener to forward drag events to GameView
        // This allows ghost to appear when shadow enters grid (even if finger is below)
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        rootLayout.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_LOCATION -> {
                    // Convert screen coordinates to GameView coordinates
                    val gameViewLocation = IntArray(2)
                    gameView.getLocationOnScreen(gameViewLocation)
                    val rootLocation = IntArray(2)
                    rootLayout.getLocationOnScreen(rootLocation)

                    val relativeX = event.x - (gameViewLocation[0] - rootLocation[0])
                    val relativeY = event.y - (gameViewLocation[1] - rootLocation[1])

                    gameView.updateGhostFromParent(relativeX, relativeY)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Handle drop here to prevent shadow return animation
                    // GameView's DRAG_ENDED will handle the actual placement
                    true
                }
                DragEvent.ACTION_DRAG_STARTED, DragEvent.ACTION_DRAG_ENTERED,
                DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED -> true
                else -> false
            }
        }

        highScoreText.text = highScoreManager.getHighScore().toString()

        gameLogic.startNewGame()
    }

    override fun onResume() {
        super.onResume()
        // Refresh high score in case it was reset in settings
        highScoreText.text = highScoreManager.getHighScore().toString()
    }

    override fun onScoreChanged(score: Int, combo: Int, streak: Int, pointsGained: Int) {
        scoreText.text = score.toString()
        comboText.text = combo.toString()
        streakText.text = streak.toString()

        // Show score popup for significant points
        if (pointsGained > 0) {
            val centerX = gameView.getGridCenterX()
            val centerY = gameView.getGridCenterY()
            gameView.scorePopupManager.showScoreGain(pointsGained, centerX, centerY)

            if (combo > 1) {
                gameView.scorePopupManager.showComboText(combo, centerX, centerY)
                vibrationHelper.vibrateCombo(combo)
            }
            gameView.startAnimationLoop()
        }
    }

    override fun onShapesChanged() {
        shapeSelectorView.refresh()
    }

    override fun onGridChanged() {
        gameView.refresh()
    }

    override fun onShapePlaced(cells: List<Pair<Int, Int>>, color: Int) {
        gameView.animateBlockPlace(cells, color)
        soundManager.playBlockPlace()
        vibrationHelper.vibrateBlockPlace()
    }

    override fun onLinesCleared(count: Int, clearedCells: Set<Pair<Int, Int>>, cellColors: Map<Pair<Int, Int>, Int>) {
        gameView.animateLineClear(clearedCells, cellColors, count)
        soundManager.playLineClear()
        vibrationHelper.vibrateLineClear(count)
    }

    override fun onStreakMilestone(streak: Int, bonus: Int) {
        val centerX = gameView.getGridCenterX()
        val centerY = gameView.getGridCenterY()
        gameView.scorePopupManager.showStreakMilestone(streak, bonus, centerX, centerY)
        gameView.startAnimationLoop()
        soundManager.playStreakMilestone()
    }

    override fun onGameOver(isNewHighScore: Boolean) {
        highScoreText.text = highScoreManager.getHighScore().toString()

        if (isNewHighScore) {
            val centerX = gameView.getGridCenterX()
            val centerY = gameView.getGridCenterY()
            gameView.scorePopupManager.showNewHighScore(centerX, centerY)
            gameView.startAnimationLoop()
            soundManager.playHighScore()
            vibrationHelper.vibrateHighScore()
        } else {
            soundManager.playGameOver()
            vibrationHelper.vibrateGameOver()
        }

        // Delay the dialog slightly to let animations play
        gameView.postDelayed({
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.game_over))
                .setMessage("${getString(R.string.score_label)}: ${gameState.score}")
                .setPositiveButton(getString(R.string.play_again)) { _, _ ->
                    gameView.animationEngine.clear()
                    gameLogic.startNewGame()
                }
                .setCancelable(false)
                .show()
        }, if (isNewHighScore) 1500L else 500L)
    }
}
