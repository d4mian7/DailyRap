package com.dailyrap.app.domain

import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.data.model.CrosswordPuzzle
import com.dailyrap.app.data.model.GameStatus

/**
 * Silnik gry RapCrossword.
 *
 * Czysta logika — bez zależności od Androida, DI ani coroutines.
 */
class CrosswordEngine {

    fun createGame(puzzle: CrosswordPuzzle): CrosswordGameState {
        return CrosswordGameState(puzzle = puzzle)
    }

    fun updateAnswer(state: CrosswordGameState, rowIndex: Int, answer: String): CrosswordGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (rowIndex in state.correctRows) return state
        val newAnswers = state.answers.toMutableMap()
        newAnswers[rowIndex] = answer.uppercase()
        return state.copy(
            answers = newAnswers,
            checkedRows = state.checkedRows - rowIndex
        )
    }

    sealed class CheckResult {
        data object Correct : CheckResult()
        data object Wrong : CheckResult()
        data object Empty : CheckResult()
    }

    fun checkAnswer(state: CrosswordGameState, rowIndex: Int): Pair<CrosswordGameState, CheckResult> {
        if (state.status != GameStatus.IN_PROGRESS) return state to CheckResult.Empty
        val answer = state.answers[rowIndex]?.trim()?.uppercase()
        if (answer.isNullOrEmpty()) return state to CheckResult.Empty

        val expected = state.puzzle.clues[rowIndex].answer.uppercase()
        val isCorrect = answer == expected

        val newChecked = state.checkedRows + rowIndex
        val newCorrect = if (isCorrect) state.correctRows + rowIndex else state.correctRows

        val allCluesCorrect = newCorrect.size == state.puzzle.clues.size
        val secretRevealed = allCluesCorrect

        // If all clues are correct, auto-complete the secret word (its letters come from the clues)
        val secretChecked = if (allCluesCorrect) true else state.secretWordChecked
        val secretCorrect = if (allCluesCorrect) true else state.secretWordCorrect

        val newState = state.copy(
            checkedRows = newChecked,
            correctRows = newCorrect,
            secretWordRevealed = secretRevealed,
            secretWordChecked = secretChecked,
            secretWordCorrect = secretCorrect,
            status = resolveStatus(newChecked.size, state.puzzle.clues.size, newCorrect.size, secretChecked, secretCorrect)
        )

        return newState to if (isCorrect) CheckResult.Correct else CheckResult.Wrong
    }

    fun updateSecretWordAnswer(state: CrosswordGameState, answer: String): CrosswordGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.secretWordCorrect) return state
        return state.copy(
            secretWordAnswer = answer.uppercase(),
            secretWordChecked = false
        )
    }

    fun checkSecretWord(state: CrosswordGameState): Pair<CrosswordGameState, CheckResult> {
        if (state.status != GameStatus.IN_PROGRESS) return state to CheckResult.Empty
        val answer = state.secretWordAnswer.trim().uppercase()
        if (answer.isEmpty()) return state to CheckResult.Empty

        val expected = state.puzzle.secretWord.uppercase()
        val isCorrect = answer == expected

        val newState = state.copy(
            secretWordChecked = true,
            secretWordCorrect = isCorrect,
            secretWordRevealed = state.secretWordRevealed || isCorrect,
            status = resolveStatus(state.checkedRows.size, state.puzzle.clues.size, state.correctRows.size, true, isCorrect)
        )

        return newState to if (isCorrect) CheckResult.Correct else CheckResult.Wrong
    }

    private fun resolveStatus(
        checkedClues: Int,
        totalClues: Int,
        correctClues: Int,
        secretChecked: Boolean,
        secretCorrect: Boolean
    ): GameStatus {
        val allCluesChecked = checkedClues == totalClues
        val allDone = allCluesChecked && secretChecked
        return if (allDone) {
            if (correctClues >= 5 && secretCorrect) GameStatus.WON else GameStatus.LOST
        } else {
            GameStatus.IN_PROGRESS
        }
    }

    fun getRevealedSecretLetters(state: CrosswordGameState): List<Char?> {
        return state.puzzle.clues.mapIndexed { index, clue ->
            if (index in state.correctRows) {
                clue.answer.uppercase().getOrNull(clue.highlightIndex)
            } else {
                null
            }
        }
    }

    fun finishGame(state: CrosswordGameState): CrosswordGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        val secretCorrect = state.secretWordCorrect ||
                state.secretWordAnswer.trim().uppercase() == state.puzzle.secretWord.uppercase()
        val correctCount = state.correctRows.size
        val newStatus = if (correctCount >= 5 && secretCorrect) GameStatus.WON else GameStatus.LOST
        return state.copy(
            checkedRows = (0 until state.puzzle.clues.size).toSet(),
            secretWordChecked = true,
            secretWordCorrect = secretCorrect,
            secretWordRevealed = state.secretWordRevealed || secretCorrect,
            status = newStatus
        )
    }
}
