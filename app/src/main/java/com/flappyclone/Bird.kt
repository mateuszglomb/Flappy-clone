package com.flappyclone

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Klasa reprezentująca ptaka w grze.
 * Obsługuje fizykę (grawitacja, skok) oraz renderowanie.
 */
class Bird(
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    companion object {
        // Stałe fizyki
        private const val GRAVITY = 0.8f
        private const val JUMP_VELOCITY = -12f
        private const val MAX_FALL_SPEED = 15f
        private const val ROTATION_FACTOR = 3f
        private const val MAX_UP_ROTATION = -25f
        private const val MAX_DOWN_ROTATION = 70f
    }
    
    // Rozmiar ptaka
    val size: Float = screenWidth * 0.1f
    
    // Pozycja ptaka (środek)
    var x: Float = screenWidth * 0.25f
    var y: Float = screenHeight / 2f
    
    // Prędkość pionowa
    private var velocity: Float = 0f
    
    // Rotacja ptaka
    private var rotation: Float = 0f
    
    // Animacja skrzydeł
    private var wingFrame: Int = 0
    private var wingTimer: Int = 0
    
    // Paint do rysowania
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 215, 0) // Złoty
    }
    
    private val beakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 102, 0) // Pomarańczowy
    }
    
    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    
    private val eyeBlackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    
    private val wingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 190, 0) // Ciemniejszy złoty
    }
    
    /**
     * Hitbox ptaka do detekcji kolizji
     */
    fun getHitbox(): RectF {
        val margin = size * 0.15f // Mniejszy hitbox niż wizualny rozmiar
        return RectF(
            x - size / 2 + margin,
            y - size / 2 + margin,
            x + size / 2 - margin,
            y + size / 2 - margin
        )
    }
    
    /**
     * Aktualizacja fizyki ptaka
     */
    fun update() {
        // Grawitacja
        velocity += GRAVITY
        velocity = velocity.coerceAtMost(MAX_FALL_SPEED)
        
        // Aktualizacja pozycji
        y += velocity
        
        // Aktualizacja rotacji na podstawie prędkości
        rotation = (velocity * ROTATION_FACTOR).coerceIn(MAX_UP_ROTATION, MAX_DOWN_ROTATION)
        
        // Animacja skrzydeł
        wingTimer++
        if (wingTimer > 5) {
            wingTimer = 0
            wingFrame = (wingFrame + 1) % 3
        }
    }
    
    /**
     * Skok ptaka
     */
    fun jump() {
        velocity = JUMP_VELOCITY
    }
    
    /**
     * Reset ptaka do pozycji startowej
     */
    fun reset() {
        y = screenHeight / 2f
        velocity = 0f
        rotation = 0f
        wingFrame = 0
        wingTimer = 0
    }
    
    /**
     * Rysowanie ptaka na Canvas
     */
    fun draw(canvas: Canvas) {
        canvas.save()
        
        // Translacja i rotacja
        canvas.translate(x, y)
        canvas.rotate(rotation)
        
        val halfSize = size / 2
        
        // Ciało (okrąg)
        canvas.drawCircle(0f, 0f, halfSize * 0.9f, bodyPaint)
        
        // Skrzydło
        val wingOffset = when (wingFrame) {
            0 -> 0f
            1 -> -size * 0.1f
            else -> size * 0.1f
        }
        canvas.drawOval(
            -halfSize * 0.6f,
            wingOffset,
            halfSize * 0.2f,
            wingOffset + halfSize * 0.6f,
            wingPaint
        )
        
        // Oko (białe)
        canvas.drawCircle(halfSize * 0.3f, -halfSize * 0.2f, halfSize * 0.35f, eyeWhitePaint)
        
        // Źrenica
        canvas.drawCircle(halfSize * 0.4f, -halfSize * 0.15f, halfSize * 0.18f, eyeBlackPaint)
        
        // Dziób
        val beakPath = android.graphics.Path().apply {
            moveTo(halfSize * 0.6f, 0f)
            lineTo(halfSize * 1.3f, halfSize * 0.1f)
            lineTo(halfSize * 0.6f, halfSize * 0.35f)
            close()
        }
        canvas.drawPath(beakPath, beakPaint)
        
        canvas.restore()
    }
    
    /**
     * Sprawdza czy ptak wyleciał poza ekran
     */
    fun isOutOfBounds(groundY: Float): Boolean {
        return y - size / 2 < 0 || y + size / 2 > groundY
    }
}
