package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrosswordClue(
    val question: String,
    val answer: String,
    val highlightIndex: Int
)

@Serializable
data class CrosswordPuzzle(
    val id: Int,
    val date: String,
    val secretWord: String,
    val clues: List<CrosswordClue>
)

@Serializable
data class CrosswordPuzzleList(
    val crosswords: List<CrosswordPuzzle>
)
