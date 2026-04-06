package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Puzzle(
    val id: Int,
    val date: String,
    val categories: List<Category>
)

@Serializable
data class PuzzleList(
    val puzzles: List<Puzzle>
)
