package com.flappyclone

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Główna aktywność gry Flappy Clone.
 * Inicjalizuje GameView i ustawia tryb pełnoekranowy.
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pełny ekran - ukryj pasek stanu i nawigacji
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Ekran zawsze włączony podczas gry
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Utworzenie i ustawienie GameView
        gameView = GameView(this)
        setContentView(gameView)
    }
    
    override fun onPause() {
        super.onPause()
        // Można dodać pauzę gry
    }
    
    override fun onResume() {
        super.onResume()
        // Można wznowić grę
    }
}
