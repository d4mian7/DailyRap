package com.dailyrap.app.domain

import com.dailyrap.app.data.model.ArtistPuzzle
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.GuessArtistGameState
import java.text.Normalizer

/**
 * Silnik gry „Kim jestem?”.
 * Gracz zgaduje artystę po tropach; pudło albo „następny trop” odsłania kolejny.
 * Stan gry jest immutable; każda operacja zwraca nowy [GuessArtistGameState].
 */
class GuessArtistEngine {

    sealed class GuessResult {
        data object Correct : GuessResult()
        data object Wrong : GuessResult()
        data object AlreadyGuessed : GuessResult()
        data object GameOver : GuessResult()
    }

    fun createGame(puzzle: ArtistPuzzle): GuessArtistGameState = GuessArtistGameState(puzzle = puzzle)

    fun submitGuess(state: GuessArtistGameState, guess: String): Pair<GuessArtistGameState, GuessResult> {
        if (state.status != GameStatus.IN_PROGRESS) return state to GuessResult.GameOver
        val normalized = normalize(guess)
        if (state.guesses.any { normalize(it) == normalized }) return state to GuessResult.AlreadyGuessed

        val guesses = state.guesses + guess
        if (normalized == normalize(state.puzzle.artist)) {
            return state.copy(guesses = guesses, status = GameStatus.WON) to GuessResult.Correct
        }
        val newState = if (hasNextClue(state)) {
            state.copy(guesses = guesses, revealedClues = state.revealedClues + 1)
        } else {
            state.copy(guesses = guesses, status = GameStatus.LOST)
        }
        return newState to GuessResult.Wrong
    }

    fun nextClue(state: GuessArtistGameState): GuessArtistGameState {
        if (state.status != GameStatus.IN_PROGRESS || !hasNextClue(state)) return state
        return state.copy(revealedClues = state.revealedClues + 1)
    }

    fun hasNextClue(state: GuessArtistGameState): Boolean =
        state.revealedClues < state.puzzle.clues.size

    /** Reklama: po przegranej jedna dodatkowa próba, bez nowego tropu. */
    fun grantExtraChance(state: GuessArtistGameState): GuessArtistGameState {
        if (state.status != GameStatus.LOST || state.extraChanceUsed) return state
        return state.copy(status = GameStatus.IN_PROGRESS, extraChanceUsed = true)
    }

    /** Lista pojawia się dopiero po pierwszej literze; pasuje początek dowolnego członu ksywy. */
    fun suggestions(query: String, pool: List<String>, guesses: List<String>): List<String> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()
        val guessed = guesses.map(::normalize).toSet()
        return pool
            .filter { normalize(it) !in guessed }
            .filter { artist ->
                val name = normalize(artist)
                name.startsWith(normalizedQuery) ||
                    name.split(' ', '-').any { it.startsWith(normalizedQuery) }
            }
            .sortedBy(::normalize)
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace('ł', 'l')
}
