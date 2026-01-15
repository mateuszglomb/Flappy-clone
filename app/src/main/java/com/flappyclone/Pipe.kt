package com.flappyclone

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader

/**
 * Klasa reprezentująca parę rur (górna i dolna) jako przeszkodę.
 */
class Pipe(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val groundY: Float
) {
    companion object {
        // Stałe wymiarów
        private const val GAP_HEIGHT_RATIO = 0.25f  // Wysokość szczeliny względem ekranu
        private const val PIPE_WIDTH_RATIO = 0.15f   // Szerokość rury względem ekranu
        private const val CAP_HEIGHT_RATIO = 0.04f   // Wysokość "czapki" rury
        private const val SPEED = 5f                  // Prędkość przesuwania
    }
    
    // Wymiary
    val width: Float = screenWidth * PIPE_WIDTH_RATIO
    private val gapHeight: Float = screenHeight * GAP_HEIGHT_RATIO
    private val capHeight: Float = screenHeight * CAP_HEIGHT_RATIO
    private val capOverhang: Float = width * 0.1f
    
    // Pozycja X (prawa krawędź rury zaczyna poza ekranem)
    var x: Float = screenWidth.toFloat()
    
    // Pozycja Y szczeliny (górna krawędź szczeliny)
    private val gapY: Float
    
    // Czy ptak przeleciał przez tę rurę (do liczenia punktów)
    var passed: Boolean = false
    
    // Paint dla różnych części rury
    private val pipePaint: Paint
    private val capPaint: Paint
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(76, 175, 80) // Jaśniejszy zielony
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(27, 94, 32) // Ciemny zielony
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    init {
        // Losowa pozycja szczeliny (z marginesem od góry i dołu)
        val minGapY = screenHeight * 0.15f
        val maxGapY = groundY - gapHeight - screenHeight * 0.15f
        gapY = minGapY + (Math.random() * (maxGapY - minGapY)).toFloat()
        
        // Gradient dla rury
        pipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width, 0f,
                intArrayOf(
                    Color.rgb(56, 142, 60),   // Lewy brzeg
                    Color.rgb(76, 175, 80),   // Środek
                    Color.rgb(46, 125, 50)    // Prawy brzeg
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        
        capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width + capOverhang * 2, 0f,
                intArrayOf(
                    Color.rgb(67, 160, 71),
                    Color.rgb(102, 187, 106),
                    Color.rgb(56, 142, 60)
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }
    
    /**
     * Hitbox górnej rury
     */
    fun getTopHitbox(): RectF {
        return RectF(x, 0f, x + width, gapY)
    }
    
    /**
     * Hitbox dolnej rury
     */
    fun getBottomHitbox(): RectF {
        return RectF(x, gapY + gapHeight, x + width, groundY)
    }
    
    /**
     * Aktualizacja pozycji rury
     */
    fun update() {
        x -= SPEED
    }
    
    /**
     * Czy rura jest poza ekranem (do usunięcia)
     */
    fun isOffScreen(): Boolean {
        return x + width < 0
    }
    
    /**
     * Rysowanie rur na Canvas
     */
    fun draw(canvas: Canvas) {
        // Aktualizacja shaderów dla aktualnej pozycji X
        pipePaint.shader = LinearGradient(
            x, 0f, x + width, 0f,
            intArrayOf(
                Color.rgb(56, 142, 60),
                Color.rgb(76, 175, 80),
                Color.rgb(46, 125, 50)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        // Górna rura
        drawPipe(canvas, 0f, gapY, isTop = true)
        
        // Dolna rura  
        drawPipe(canvas, gapY + gapHeight, groundY, isTop = false)
    }
    
    private fun drawPipe(canvas: Canvas, top: Float, bottom: Float, isTop: Boolean) {
        // Główna część rury
        val pipeRect = RectF(x, top, x + width, bottom)
        canvas.drawRect(pipeRect, pipePaint)
        
        // "Czapka" rury
        val capY = if (isTop) bottom - capHeight else top
        val capRect = RectF(
            x - capOverhang,
            capY,
            x + width + capOverhang,
            capY + capHeight
        )
        
        capPaint.shader = LinearGradient(
            x - capOverhang, 0f, x + width + capOverhang, 0f,
            intArrayOf(
                Color.rgb(67, 160, 71),
                Color.rgb(102, 187, 106),
                Color.rgb(56, 142, 60)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        
        canvas.drawRoundRect(capRect, 8f, 8f, capPaint)
        
        // Światło (lewa krawędź)
        canvas.drawLine(x + 4f, top, x + 4f, bottom, highlightPaint)
        
        // Cień (prawa krawędź)
        canvas.drawLine(x + width - 4f, top, x + width - 4f, bottom, shadowPaint)
    }
    
    /**
     * Środek szczeliny (do sprawdzania czy ptak przeleciał)
     */
    fun getGapCenterX(): Float = x + width / 2
}
