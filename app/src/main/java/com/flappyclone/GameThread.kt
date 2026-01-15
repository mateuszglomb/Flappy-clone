package com.flappyclone

import android.graphics.Canvas
import android.view.SurfaceHolder

/**
 * Główny wątek gry odpowiedzialny za pętlę aktualizacji i renderowania.
 * Działa z targetem ~60 FPS.
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {
    
    companion object {
        private const val TARGET_FPS = 60
        private const val FRAME_TIME_MS = 1000 / TARGET_FPS
    }
    
    @Volatile
    var running: Boolean = false
    
    override fun run() {
        var lastTime = System.currentTimeMillis()
        
        while (running) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastTime
            
            var canvas: Canvas? = null
            
            try {
                canvas = surfaceHolder.lockCanvas()
                
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        // Aktualizacja stanu gry
                        gameView.updateGame()
                        
                        // Renderowanie
                        gameView.drawGame(canvas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Utrzymanie stałego FPS
            val frameTime = System.currentTimeMillis() - currentTime
            val sleepTime = FRAME_TIME_MS - frameTime
            
            if (sleepTime > 0) {
                try {
                    sleep(sleepTime)
                } catch (e: InterruptedException) {
                    // Ignoruj
                }
            }
            
            lastTime = currentTime
        }
    }
}
