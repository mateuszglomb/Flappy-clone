package com.flappyclone

import android.content.Context
import android.content.SharedPreferences

/**
 * Zarządza punktacją i zapisywaniem najlepszego wyniku.
 */
class ScoreManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "flappy_clone_prefs"
        private const val KEY_BEST_SCORE = "best_score"
    }
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Aktualny wynik
    var currentScore: Int = 0
        private set
    
    // Najlepszy wynik
    var bestScore: Int = prefs.getInt(KEY_BEST_SCORE, 0)
        private set
    
    // Czy pobito rekord w tej rundzie
    var isNewBest: Boolean = false
        private set
    
    /**
     * Dodaje punkty do aktualnego wyniku
     */
    fun addPoints(points: Int) {
        currentScore += points
        
        if (currentScore > bestScore) {
            bestScore = currentScore
            isNewBest = true
            saveBestScore()
        }
    }
    
    /**
     * Reset wyniku dla nowej gry
     */
    fun reset() {
        currentScore = 0
        isNewBest = false
    }
    
    /**
     * Zapisuje najlepszy wynik do SharedPreferences
     */
    private fun saveBestScore() {
        prefs.edit().putInt(KEY_BEST_SCORE, bestScore).apply()
    }
}
