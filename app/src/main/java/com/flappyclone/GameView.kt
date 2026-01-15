package com.flappyclone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Główny widok gry - SurfaceView obsługujący renderowanie i interakcję.
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    
    private var gameThread: GameThread? = null
    private var gameState: GameState = GameState.MENU
    
    // Obiekty gry
    private lateinit var bird: Bird
    private lateinit var pipeManager: PipeManager
    private lateinit var scoreManager: ScoreManager
    
    // Wymiary ekranu
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var groundY: Float = 0f
    
    // Paints do rysowania
    private lateinit var skyPaint: Paint
    private lateinit var groundPaint: Paint
    private lateinit var groundLinePaint: Paint
    
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(8f, 4f, 4f, Color.argb(150, 0, 0, 0))
    }
    
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(6f, 3f, 3f, Color.argb(150, 0, 0, 0))
    }
    
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        alpha = 200
    }
    
    private val overlayPaint = Paint().apply {
        color = Color.argb(120, 0, 0, 0)
    }
    
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 50, 50, 50)
    }
    
    private val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    // Animacja tekstu "Dotknij aby zacząć"
    private var menuAnimTimer = 0f
    
    init {
        holder.addCallback(this)
        isFocusable = true
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Inicjalizacja wymiarów
        screenWidth = width
        screenHeight = height
        groundY = screenHeight * 0.85f
        
        // Inicjalizacja paintów
        skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, screenHeight.toFloat(),
                Color.rgb(135, 206, 235), // Sky blue top
                Color.rgb(77, 166, 255),   // Lighter blue bottom
                Shader.TileMode.CLAMP
            )
        }
        
        groundPaint = Paint().apply {
            shader = LinearGradient(
                0f, groundY, 0f, screenHeight.toFloat(),
                Color.rgb(222, 184, 135), // Burlywood
                Color.rgb(139, 119, 101),  // Darker
                Shader.TileMode.CLAMP
            )
        }
        
        groundLinePaint = Paint().apply {
            color = Color.rgb(101, 67, 33)
            strokeWidth = 8f
        }
        
        // Rozmiary tekstów
        scorePaint.textSize = screenWidth * 0.15f
        titlePaint.textSize = screenWidth * 0.12f
        subtitlePaint.textSize = screenWidth * 0.05f
        goldPaint.textSize = screenWidth * 0.08f
        
        // Inicjalizacja obiektów gry
        bird = Bird(screenWidth, screenHeight)
        pipeManager = PipeManager(screenWidth, screenHeight, groundY)
        scoreManager = ScoreManager(context)
        
        // Start wątku gry
        gameThread = GameThread(holder, this)
        gameThread?.running = true
        gameThread?.start()
    }
    
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Nie potrzebne dla tej implementacji
    }
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread?.running = false
        
        while (retry) {
            try {
                gameThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                // Próbuj dalej
            }
        }
        
        gameThread = null
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (gameState) {
                    GameState.MENU -> {
                        // Start gry
                        startGame()
                    }
                    GameState.PLAYING -> {
                        // Skok ptaka
                        bird.jump()
                    }
                    GameState.GAME_OVER -> {
                        // Restart po krótkim opóźnieniu
                        restartGame()
                    }
                }
            }
        }
        return true
    }
    
    /**
     * Rozpoczęcie nowej gry
     */
    private fun startGame() {
        gameState = GameState.PLAYING
        bird.reset()
        pipeManager.reset()
        scoreManager.reset()
    }
    
    /**
     * Restart gry po Game Over
     */
    private fun restartGame() {
        startGame()
    }
    
    /**
     * Aktualizacja stanu gry (wywoływana przez GameThread)
     */
    fun updateGame() {
        when (gameState) {
            GameState.MENU -> {
                // Animacja menu
                menuAnimTimer += 0.05f
            }
            GameState.PLAYING -> {
                // Aktualizacja ptaka
                bird.update()
                
                // Aktualizacja rur
                pipeManager.update()
                
                // Sprawdzenie punktów
                val points = pipeManager.checkScore(bird.x)
                if (points > 0) {
                    scoreManager.addPoints(points)
                }
                
                // Sprawdzenie kolizji z rurami
                if (pipeManager.checkCollision(bird)) {
                    gameOver()
                }
                
                // Sprawdzenie kolizji z podłożem/sufitem
                if (bird.isOutOfBounds(groundY)) {
                    gameOver()
                }
            }
            GameState.GAME_OVER -> {
                // Można dodać animację
            }
        }
    }
    
    /**
     * Koniec gry
     */
    private fun gameOver() {
        gameState = GameState.GAME_OVER
    }
    
    /**
     * Rysowanie gry (wywoływane przez GameThread)
     */
    fun drawGame(canvas: Canvas) {
        // Tło - niebo
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), skyPaint)
        
        // Rury (za ptakiem)
        for (pipe in pipeManager.pipes) {
            pipe.draw(canvas)
        }
        
        // Ptak
        bird.draw(canvas)
        
        // Ziemia
        canvas.drawRect(0f, groundY, screenWidth.toFloat(), screenHeight.toFloat(), groundPaint)
        canvas.drawLine(0f, groundY, screenWidth.toFloat(), groundY, groundLinePaint)
        
        // Dekoracja ziemi - linie
        val lineSpacing = screenWidth / 8f
        for (i in 0..8) {
            val x = i * lineSpacing
            canvas.drawLine(x, groundY + 15f, x + 20f, screenHeight.toFloat(), 
                Paint().apply { 
                    color = Color.rgb(160, 120, 80)
                    strokeWidth = 3f 
                })
        }
        
        // UI w zależności od stanu
        when (gameState) {
            GameState.MENU -> drawMenu(canvas)
            GameState.PLAYING -> drawPlayingUI(canvas)
            GameState.GAME_OVER -> drawGameOver(canvas)
        }
    }
    
    /**
     * Rysowanie ekranu menu
     */
    private fun drawMenu(canvas: Canvas) {
        // Tytuł
        canvas.drawText(
            "Flappy Clone",
            screenWidth / 2f,
            screenHeight * 0.25f,
            titlePaint
        )
        
        // Animowany tekst "Dotknij aby zacząć"
        val alpha = ((kotlin.math.sin(menuAnimTimer.toDouble()) + 1) / 2 * 150 + 105).toInt()
        subtitlePaint.alpha = alpha
        
        canvas.drawText(
            "Dotknij aby zacząć",
            screenWidth / 2f,
            screenHeight * 0.65f,
            subtitlePaint
        )
        
        // Najlepszy wynik
        if (scoreManager.bestScore > 0) {
            canvas.drawText(
                "Najlepszy wynik: ${scoreManager.bestScore}",
                screenWidth / 2f,
                screenHeight * 0.75f,
                subtitlePaint.apply { alpha = 180 }
            )
        }
    }
    
    /**
     * Rysowanie UI podczas gry
     */
    private fun drawPlayingUI(canvas: Canvas) {
        // Wynik na górze ekranu
        canvas.drawText(
            scoreManager.currentScore.toString(),
            screenWidth / 2f,
            screenHeight * 0.12f,
            scorePaint
        )
    }
    
    /**
     * Rysowanie ekranu Game Over
     */
    private fun drawGameOver(canvas: Canvas) {
        // Przyciemnienie tła
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)
        
        // Panel wyników
        val panelWidth = screenWidth * 0.7f
        val panelHeight = screenHeight * 0.35f
        val panelLeft = (screenWidth - panelWidth) / 2
        val panelTop = screenHeight * 0.25f
        val panelRect = RectF(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight)
        
        canvas.drawRoundRect(panelRect, 30f, 30f, panelPaint)
        
        // Tytuł "Koniec gry!"
        canvas.drawText(
            "Koniec gry!",
            screenWidth / 2f,
            panelTop + panelHeight * 0.2f,
            titlePaint.apply { textSize = screenWidth * 0.08f }
        )
        
        // Wynik
        canvas.drawText(
            "Wynik: ${scoreManager.currentScore}",
            screenWidth / 2f,
            panelTop + panelHeight * 0.5f,
            subtitlePaint.apply { 
                textSize = screenWidth * 0.06f 
                alpha = 255
            }
        )
        
        // Najlepszy wynik
        val bestText = if (scoreManager.isNewBest) "Nowy rekord!" else "Najlepszy: ${scoreManager.bestScore}"
        val bestPaint = if (scoreManager.isNewBest) goldPaint else subtitlePaint
        
        canvas.drawText(
            bestText,
            screenWidth / 2f,
            panelTop + panelHeight * 0.7f,
            bestPaint.apply { textSize = screenWidth * 0.05f }
        )
        
        // "Dotknij aby zagrać ponownie"
        val alpha = ((kotlin.math.sin(menuAnimTimer.toDouble()) + 1) / 2 * 150 + 105).toInt()
        subtitlePaint.alpha = alpha
        
        canvas.drawText(
            "Dotknij aby zagrać ponownie",
            screenWidth / 2f,
            screenHeight * 0.75f,
            subtitlePaint.apply { textSize = screenWidth * 0.045f }
        )
        
        // Aktualizacja animacji
        menuAnimTimer += 0.05f
    }
}
