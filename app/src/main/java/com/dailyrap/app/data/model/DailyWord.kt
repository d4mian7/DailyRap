package com.dailyrap.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyWord(
    val id: Int,
    val date: String,
    val word: String,
    val hint: String,
    val variant: Int
)

@Serializable
data class DailyWordList(
    val words: List<DailyWord>
)
