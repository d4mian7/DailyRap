package com.dailyrap.app.data.model

data class PlayerStats(
    val connectionsPlayed: Int = 0,
    val connectionsWon: Int = 0,
    val connectionsStreak: Int = 0,
    val connectionsBestStreak: Int = 0,
    val connectionsMistakeHistogram: Map<Int, Int> = emptyMap(),
    val literalniePlayed: Int = 0,
    val literalnieWon: Int = 0,
    val literalnieStreak: Int = 0,
    val literalnieBestStreak: Int = 0,
    val literalnieGuessHistogram: Map<Int, Int> = emptyMap(),
    val crosswordPlayed: Int = 0,
    val crosswordWon: Int = 0,
    val crosswordStreak: Int = 0,
    val crosswordBestStreak: Int = 0,
    val crosswordCorrectHistogram: Map<Int, Int> = emptyMap()
)
