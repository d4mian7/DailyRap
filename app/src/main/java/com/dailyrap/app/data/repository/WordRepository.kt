package com.dailyrap.app.data.repository

import android.content.Context
import com.dailyrap.app.data.model.DailyWord
import com.dailyrap.app.data.model.DailyWordList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var words: List<DailyWord>? = null
    private var dictionary: Set<String>? = null

    private fun loadWords(): List<DailyWord> {
        words?.let { return it }
        val jsonString = context.assets.open("words.json")
            .bufferedReader()
            .use { it.readText() }
        val wordList = json.decodeFromString<DailyWordList>(jsonString)
        words = wordList.words
        return wordList.words
    }

    fun getDictionary(): Set<String> {
        dictionary?.let { return it }
        val set = mutableSetOf<String>()
        context.assets.open("polish_words.txt").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val w = line.trim().uppercase()
                if (w.isNotEmpty()) set.add(w)
            }
        }
        // Dodaj też wszystkie hasła rapowe z words.json
        loadWords().forEach { set.add(it.word.uppercase()) }
        dictionary = set
        return set
    }

    fun getWordByDate(date: String): DailyWord? {
        return loadWords().find { it.date == date }
    }

    fun getWordById(id: Int): DailyWord? {
        return loadWords().find { it.id == id }
    }

    fun getAllWords(): List<DailyWord> {
        return loadWords()
    }
}
