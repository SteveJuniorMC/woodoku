package com.game.woodoku

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.game.woodoku.data.HighScoreManager
import com.game.woodoku.game.GameLogic
import com.game.woodoku.game.GameState
import com.game.woodoku.view.GameView
import com.game.woodoku.view.ShapeSelectorView

class MainActivity : AppCompatActivity(), GameLogic.GameListener {

    private lateinit var gameState: GameState
    private lateinit var gameLogic: GameLogic
    private lateinit var highScoreManager: HighScoreManager

    private lateinit var gameView: GameView
    private lateinit var shapeSelectorView: ShapeSelectorView
    private lateinit var scoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var comboText: TextView
    private lateinit var streakText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        highScoreManager = HighScoreManager(this)

        gameState = GameState()
        gameLogic = GameLogic(gameState)
        gameLogic.listener = this

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

        highScoreText.text = highScoreManager.getHighScore().toString()

        gameLogic.startNewGame()
    }

    override fun onScoreChanged(score: Int, combo: Int, streak: Int) {
        scoreText.text = score.toString()
        comboText.text = combo.toString()
        streakText.text = streak.toString()
    }

    override fun onShapesChanged() {
        shapeSelectorView.refresh()
    }

    override fun onGridChanged() {
        gameView.refresh()
    }

    override fun onLinesCleared(count: Int) {
        // Could add animation/sound here
    }

    override fun onGameOver() {
        val isNewHighScore = highScoreManager.saveHighScore(gameState.score)
        highScoreText.text = highScoreManager.getHighScore().toString()

        val message = if (isNewHighScore) {
            getString(R.string.new_high_score) + "\n" + getString(R.string.game_over)
        } else {
            getString(R.string.game_over)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over))
            .setMessage("${getString(R.string.score_label)}: ${gameState.score}")
            .setPositiveButton(getString(R.string.play_again)) { _, _ ->
                gameLogic.startNewGame()
            }
            .setCancelable(false)
            .show()
    }
}
