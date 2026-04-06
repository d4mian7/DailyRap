package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LiteralnieGameState(
    val dailyWord: DailyWord,
    val guesses: List<String> = emptyList(),
    val maxGuesses: Int,
    val currentGuess: String = "",
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val extraAttemptsUsed: Int = 0
) {
    companion object {
        const val MAX_EXTRA_ATTEMPTS = 3
    }
}
