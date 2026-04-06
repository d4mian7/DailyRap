package com.dailyrap.app.data.repository

import com.dailyrap.app.data.local.DailyResultDao
import com.dailyrap.app.data.local.entity.ConnectionsResultEntity
import com.dailyrap.app.data.local.entity.CrosswordResultEntity
import com.dailyrap.app.data.local.entity.LiteralnieResultEntity
import com.dailyrap.app.data.model.PlayerStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dao: DailyResultDao
) {
    suspend fun saveConnectionsResult(result: ConnectionsResultEntity) {
        dao.insertConnectionsResult(result)
    }

    suspend fun saveLiteralnieResult(result: LiteralnieResultEntity) {
        dao.insertLiteralnieResult(result)
    }

    suspend fun saveCrosswordResult(result: CrosswordResultEntity) {
        dao.insertCrosswordResult(result)
    }

    suspend fun getConnectionsResult(date: String): ConnectionsResultEntity? {
        return dao.getConnectionsResult(date)
    }

    suspend fun getLiteralnieResult(date: String): LiteralnieResultEntity? {
        return dao.getLiteralnieResult(date)
    }

    suspend fun getCrosswordResult(date: String): CrosswordResultEntity? {
        return dao.getCrosswordResult(date)
    }

    suspend fun resetAllStats() {
        dao.deleteAllConnectionsResults()
        dao.deleteAllLiteralnieResults()
        dao.deleteAllCrosswordResults()
    }

    suspend fun getPlayerStats(): PlayerStats {
        val connectionsResults = dao.getAllConnectionsResults()
        val literalnieResults = dao.getAllLiteralnieResults()
        val crosswordResults = dao.getAllCrosswordResults()

        val connectionsMistakeHistogram = connectionsResults
            .filter { it.won }
            .groupingBy { it.mistakes }
            .eachCount()

        val literalnieGuessHistogram = literalnieResults
            .filter { it.won }
            .groupingBy { it.guessCount }
            .eachCount()

        val crosswordCorrectHistogram = crosswordResults
            .groupingBy { it.correctAnswers }
            .eachCount()

        return PlayerStats(
            connectionsPlayed = connectionsResults.size,
            connectionsWon = connectionsResults.count { it.won },
            connectionsStreak = calculateStreak(connectionsResults.map { it.date to it.won }),
            connectionsBestStreak = calculateBestStreak(connectionsResults.map { it.date to it.won }),
            connectionsMistakeHistogram = connectionsMistakeHistogram,
            literalniePlayed = literalnieResults.size,
            literalnieWon = literalnieResults.count { it.won },
            literalnieStreak = calculateStreak(literalnieResults.map { it.date to it.won }),
            literalnieBestStreak = calculateBestStreak(literalnieResults.map { it.date to it.won }),
            literalnieGuessHistogram = literalnieGuessHistogram,
            crosswordPlayed = crosswordResults.size,
            crosswordWon = crosswordResults.count { it.correctAnswers >= 5 },
            crosswordStreak = calculateStreak(crosswordResults.map { it.date to (it.correctAnswers >= 5) }),
            crosswordBestStreak = calculateBestStreak(crosswordResults.map { it.date to (it.correctAnswers >= 5) }),
            crosswordCorrectHistogram = crosswordCorrectHistogram
        )
    }

    private fun calculateStreak(results: List<Pair<String, Boolean>>): Int {
        var streak = 0
        for ((_, won) in results) {
            if (won) streak++ else break
        }
        return streak
    }

    private fun calculateBestStreak(results: List<Pair<String, Boolean>>): Int {
        var best = 0
        var current = 0
        for ((_, won) in results) {
            if (won) {
                current++
                if (current > best) best = current
            } else {
                current = 0
            }
        }
        return best
    }
}
