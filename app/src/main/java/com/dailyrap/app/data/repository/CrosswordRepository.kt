package com.dailyrap.app.data.repository

import android.content.Context
import com.dailyrap.app.data.model.CrosswordPuzzle
import com.dailyrap.app.data.model.CrosswordPuzzleList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrosswordRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var crosswords: List<CrosswordPuzzle>? = null

    private fun loadCrosswords(): List<CrosswordPuzzle> {
        crosswords?.let { return it }
        val jsonString = context.assets.open("crosswords.json")
            .bufferedReader()
            .use { it.readText() }
        val list = json.decodeFromString<CrosswordPuzzleList>(jsonString)
        crosswords = list.crosswords
        return list.crosswords
    }

    fun getCrosswordByDate(date: String): CrosswordPuzzle? {
        return loadCrosswords().find { it.date == date }
    }

    fun getAllCrosswords(): List<CrosswordPuzzle> {
        return loadCrosswords()
    }
}
