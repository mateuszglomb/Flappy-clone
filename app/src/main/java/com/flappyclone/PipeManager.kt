package com.flappyclone

/**
 * Zarządza tworzeniem i usuwaniem rur.
 */
class PipeManager(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val groundY: Float
) {
    companion object {
        // Odstęp między rurami (w pikselach)
        private const val PIPE_SPACING_RATIO = 0.45f
    }
    
    private val pipeSpacing: Float = screenWidth * PIPE_SPACING_RATIO
    
    // Lista aktywnych rur
    val pipes: MutableList<Pipe> = mutableListOf()
    
    // Dystans od ostatniej rury
    private var distanceSinceLastPipe: Float = 0f
    
    /**
     * Reset managera - usuwa wszystkie rury
     */
    fun reset() {
        pipes.clear()
        distanceSinceLastPipe = pipeSpacing // Pierwsza rura pojawi się od razu
    }
    
    /**
     * Aktualizacja rur - ruch, generowanie nowych, usuwanie starych
     */
    fun update(): Int {
        var scoreIncrease = 0
        
        // Aktualizacja istniejących rur
        val iterator = pipes.iterator()
        while (iterator.hasNext()) {
            val pipe = iterator.next()
            pipe.update()
            
            // Usuwanie rur poza ekranem
            if (pipe.isOffScreen()) {
                iterator.remove()
            }
        }
        
        // Zwiększanie dystansu (rury przesuwają się o 5 pikseli na klatkę)
        distanceSinceLastPipe += 5f
        
        // Generowanie nowej rury
        if (distanceSinceLastPipe >= pipeSpacing) {
            pipes.add(Pipe(screenWidth, screenHeight, groundY))
            distanceSinceLastPipe = 0f
        }
        
        return scoreIncrease
    }
    
    /**
     * Sprawdza kolizje ptaka z rurami
     */
    fun checkCollision(bird: Bird): Boolean {
        val birdHitbox = bird.getHitbox()
        
        for (pipe in pipes) {
            // Kolizja z górną rurą
            if (android.graphics.RectF.intersects(birdHitbox, pipe.getTopHitbox())) {
                return true
            }
            
            // Kolizja z dolną rurą
            if (android.graphics.RectF.intersects(birdHitbox, pipe.getBottomHitbox())) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Sprawdza czy ptak przeleciał przez rurę i zwraca liczbę punktów
     */
    fun checkScore(birdX: Float): Int {
        var points = 0
        
        for (pipe in pipes) {
            if (!pipe.passed && birdX > pipe.getGapCenterX()) {
                pipe.passed = true
                points++
            }
        }
        
        return points
    }
}
