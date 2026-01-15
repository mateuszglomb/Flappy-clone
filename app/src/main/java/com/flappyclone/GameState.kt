package com.flappyclone

/**
 * Stany gry Flappy Bird
 */
enum class GameState {
    /** Ekran startowy - oczekiwanie na dotknięcie */
    MENU,
    
    /** Gra w toku */
    PLAYING,
    
    /** Koniec gry - wyświetlanie wyniku */
    GAME_OVER
}
