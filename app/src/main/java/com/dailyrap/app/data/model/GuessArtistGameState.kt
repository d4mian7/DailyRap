package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistPuzzle(
    val id: Int,
    val date: String,
    val artist: String,
    val clues: List<String>
)

@Serializable
data class ArtistPuzzleList(
    val pool: List<String>,
    val artists: List<ArtistPuzzle>
)

@Serializable
data class GuessArtistGameState(
    val puzzle: ArtistPuzzle,
    val revealedClues: Int = 1,
    val guesses: List<String> = emptyList(),
    val extraChanceUsed: Boolean = false,
    val status: GameStatus = GameStatus.IN_PROGRESS
)
