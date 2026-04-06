package com.dailyrap.app.domain.usecase

import com.dailyrap.app.data.local.entity.ConnectionsResultEntity
import com.dailyrap.app.data.local.entity.CrosswordResultEntity
import com.dailyrap.app.data.local.entity.LiteralnieResultEntity
import com.dailyrap.app.data.model.ConnectionsGameState
import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.LiteralnieGameState
import com.dailyrap.app.data.repository.StatsRepository
import javax.inject.Inject

class SaveGameResultUseCase @Inject constructor(
    private val statsRepository: StatsRepository
) {
    suspend fun saveConnectionsResult(
        state: ConnectionsGameState,
        timeSeconds: Int
    ) {
        val entity = ConnectionsResultEntity(
            date = state.puzzle.date,
            puzzleId = state.puzzle.id,
            won = state.status == GameStatus.WON,
            mistakes = 4 - state.mistakesLeft,
            timeSeconds = timeSeconds,
            guessHistory = state.guessHistory.joinToString(";") { it.joinToString(",") }
        )
        statsRepository.saveConnectionsResult(entity)
    }

    suspend fun saveLiteralnieResult(state: LiteralnieGameState) {
        val entity = LiteralnieResultEntity(
            date = state.dailyWord.date,
            wordId = state.dailyWord.id,
            won = state.status == GameStatus.WON,
            guessCount = state.guesses.size,
            variant = state.dailyWord.variant,
            guessHistory = state.guesses.joinToString(",")
        )
        statsRepository.saveLiteralnieResult(entity)
    }

    suspend fun saveCrosswordResult(state: CrosswordGameState) {
        val entity = CrosswordResultEntity(
            date = state.puzzle.date,
            puzzleId = state.puzzle.id,
            correctAnswers = state.correctRows.size,
            totalClues = state.puzzle.clues.size,
            secretWordFound = state.secretWordRevealed
        )
        statsRepository.saveCrosswordResult(entity)
    }
}
