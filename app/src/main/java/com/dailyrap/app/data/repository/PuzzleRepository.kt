package com.dailyrap.app.data.repository

import android.content.Context
import com.dailyrap.app.data.model.Puzzle
import com.dailyrap.app.data.model.PuzzleList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PuzzleRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var puzzles: List<Puzzle>? = null

    private fun loadPuzzles(): List<Puzzle> {
        puzzles?.let { return it }
        val jsonString = context.assets.open("puzzles.json")
            .bufferedReader()
            .use { it.readText() }
        val puzzleList = json.decodeFromString<PuzzleList>(jsonString)
        puzzles = puzzleList.puzzles
        return puzzleList.puzzles
    }

    fun getPuzzleByDate(date: String): Puzzle? {
        return loadPuzzles().find { it.date == date }
    }

    fun getPuzzleById(id: Int): Puzzle? {
        return loadPuzzles().find { it.id == id }
    }

    fun getAllPuzzles(): List<Puzzle> {
        return loadPuzzles()
    }
}
