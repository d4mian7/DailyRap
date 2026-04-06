package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrosswordGameState(
    val puzzle: CrosswordPuzzle,
    val answers: Map<Int, String> = emptyMap(),
    val checkedRows: Set<Int> = emptySet(),
    val correctRows: Set<Int> = emptySet(),
    val secretWordRevealed: Boolean = false,
    val secretWordAnswer: String = "",
    val secretWordCorrect: Boolean = false,
    val secretWordChecked: Boolean = false,
    val status: GameStatus = GameStatus.IN_PROGRESS
)
