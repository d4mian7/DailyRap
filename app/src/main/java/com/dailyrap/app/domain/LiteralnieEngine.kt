package com.dailyrap.app.domain

import com.dailyrap.app.data.model.DailyWord
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.data.model.LiteralnieGameState

class LiteralnieEngine {

    fun createGame(dailyWord: DailyWord): LiteralnieGameState {
        return LiteralnieGameState(
            dailyWord = dailyWord,
            maxGuesses = 6
        )
    }

    fun typeLetter(state: LiteralnieGameState, letter: Char): LiteralnieGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.currentGuess.length >= state.dailyWord.variant) return state
        return state.copy(currentGuess = state.currentGuess + letter.uppercaseChar())
    }

    fun deleteLetter(state: LiteralnieGameState): LiteralnieGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (state.currentGuess.isEmpty()) return state
        return state.copy(currentGuess = state.currentGuess.dropLast(1))
    }

    sealed class SubmitResult {
        data class Accepted(val letterResults: List<LetterResult>) : SubmitResult()
        data object TooShort : SubmitResult()
        data object NotInDictionary : SubmitResult()
        data object GameOver : SubmitResult()
    }

    fun submitGuess(
        state: LiteralnieGameState,
        dictionary: Set<String>? = null
    ): Pair<LiteralnieGameState, SubmitResult> {
        if (state.status != GameStatus.IN_PROGRESS) return state to SubmitResult.GameOver

        val guess = state.currentGuess.uppercase()
        val target = state.dailyWord.word.uppercase()

        if (guess.length != target.length) return state to SubmitResult.TooShort

        // Walidacja słownikowa — jeśli słownik jest dostępny
        if (dictionary != null && guess != target && guess !in dictionary) {
            return state to SubmitResult.NotInDictionary
        }

        val letterResults = evaluateGuess(guess, target)
        val newGuesses = state.guesses + guess
        val won = guess == target
        val totalAllowed = state.maxGuesses + state.extraAttemptsUsed
        val lost = !won && newGuesses.size >= totalAllowed

        val newStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        val newState = state.copy(
            guesses = newGuesses,
            currentGuess = "",
            status = newStatus
        )
        return newState to SubmitResult.Accepted(letterResults)
    }

    fun evaluateGuess(guess: String, target: String): List<LetterResult> {
        val g = guess.uppercase()
        val t = target.uppercase()

        val results = Array(g.length) { LetterResult.ABSENT }
        val targetCharsRemaining = t.toCharArray().toMutableList()

        for (i in g.indices) {
            if (g[i] == t[i]) {
                results[i] = LetterResult.CORRECT
                targetCharsRemaining[i] = '\u0000'
            }
        }

        for (i in g.indices) {
            if (results[i] == LetterResult.CORRECT) continue
            val idx = targetCharsRemaining.indexOf(g[i])
            if (idx != -1) {
                results[i] = LetterResult.PRESENT
                targetCharsRemaining[idx] = '\u0000'
            }
        }

        return results.toList()
    }

    fun getKeyboardState(state: LiteralnieGameState): Map<Char, LetterResult> {
        val keyboard = mutableMapOf<Char, LetterResult>()
        val target = state.dailyWord.word.uppercase()

        for (guess in state.guesses) {
            val results = evaluateGuess(guess, target)
            for ((i, letter) in guess.uppercase().withIndex()) {
                val current = keyboard[letter]
                val new = results[i]
                if (current == null || new.priority > current.priority) {
                    keyboard[letter] = new
                }
            }
        }
        return keyboard
    }
}

private val LetterResult.priority: Int
    get() = when (this) {
        LetterResult.CORRECT -> 2
        LetterResult.PRESENT -> 1
        LetterResult.ABSENT -> 0
    }
