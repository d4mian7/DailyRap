package com.dailyrap.app.data.repository

import android.content.Context
import com.dailyrap.app.data.model.ArtistPuzzleList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuessArtistRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var data: ArtistPuzzleList? = null

    fun load(): ArtistPuzzleList {
        data?.let { return it }
        val jsonString = context.assets.open("artists.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString<ArtistPuzzleList>(jsonString).also { data = it }
    }
}
