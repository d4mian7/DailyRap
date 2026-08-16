package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsGameState(
    val puzzle: Puzzle,
    val remainingWords: List<String>,
    val solvedCategories: List<Category> = emptyList(),
    val selectedWords: List<String> = emptyList(),
    val mistakesLeft: Int = 4,
    val guessHistory: List<List<String>> = emptyList(),
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val hintsUsed: Int = 0,
    val hintedCategories: Set<Int> = emptySet()
)

@Serializable
enum class GameStatus {
    IN_PROGRESS,
    WON,
    LOST
}
