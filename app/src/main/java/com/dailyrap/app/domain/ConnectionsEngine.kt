package com.dailyrap.app.domain

import com.dailyrap.app.data.model.Category
import com.dailyrap.app.data.model.ConnectionsGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.Puzzle

/**
 * Silnik gry RapConnect.
 *
 * Czysta logika — bez zależności od Androida, DI ani coroutines.
 * Stan gry jest immutable; każda operacja zwraca nowy [ConnectionsGameState].
 */
class ConnectionsEngine {

    // ── Inicjalizacja ──────────────────────────────────────────────

    fun createGame(puzzle: Puzzle): ConnectionsGameState {
        val allWords = puzzle.categories.flatMap { it.words }.shuffled()
        return ConnectionsGameState(
            puzzle = puzzle,
            remainingWords = allWords
        )
    }

    // ── Tasowanie ──────────────────────────────────────────────────

    fun shuffle(state: ConnectionsGameState): ConnectionsGameState {
        return state.copy(remainingWords = state.remainingWords.shuffled())
    }

    // ── Zaznaczanie / odznaczanie słów ─────────────────────────────

    fun toggleWord(state: ConnectionsGameState, word: String): ConnectionsGameState {
        if (state.status != GameStatus.IN_PROGRESS) return state
        if (word !in state.remainingWords) return state

        val selected = state.selectedWords.toMutableList()
        if (word in selected) {
            selected.remove(word)
        } else {
            if (selected.size >= 4) return state
            selected.add(word)
        }
        return state.copy(selectedWords = selected)
    }

    fun clearSelection(state: ConnectionsGameState): ConnectionsGameState {
        return state.copy(selectedWords = emptyList())
    }

    // ── Sprawdzanie zgadywania ─────────────────────────────────────

    /**
     * Wynik sprawdzenia 4 zaznaczonych słów.
     */
    sealed class GuessResult {
        /** Trafienie — 4/4 słowa pasują do jednej kategorii. */
        data class Correct(val category: Category) : GuessResult()
        /** Prawie — 3 z 4 słów pasują do jednej kategorii. */
        data object AlmostCorrect : GuessResult()
        /** Pudło — żadna kategoria nie ma 3+ trafień. */
        data object Wrong : GuessResult()
        /** Nie można sprawdzić (np. < 4 słowa, gra skończona, duplikat). */
        data object Invalid : GuessResult()
    }

    fun submitGuess(state: ConnectionsGameState): Pair<ConnectionsGameState, GuessResult> {
        if (state.status != GameStatus.IN_PROGRESS) return state to GuessResult.Invalid
        if (state.selectedWords.size != 4) return state to GuessResult.Invalid

        // Blokada powtórnego sprawdzania tego samego zestawu
        val sorted = state.selectedWords.sorted()
        if (state.guessHistory.any { it.sorted() == sorted }) {
            return state to GuessResult.Invalid
        }

        val newHistory = state.guessHistory + listOf(state.selectedWords.toList())

        // Szukamy kategorii, do której pasują wszystkie 4 słowa
        val matchedCategory = state.puzzle.categories.find { category ->
            category !in state.solvedCategories &&
                state.selectedWords.all { it in category.words }
        }

        return if (matchedCategory != null) {
            // Trafienie 4/4
            val newRemaining = state.remainingWords.filter { it !in state.selectedWords }
            val newSolved = state.solvedCategories + matchedCategory
            val newStatus = if (newSolved.size == state.puzzle.categories.size) {
                GameStatus.WON
            } else {
                GameStatus.IN_PROGRESS
            }
            val newState = state.copy(
                remainingWords = newRemaining,
                solvedCategories = newSolved,
                selectedWords = emptyList(),
                guessHistory = newHistory,
                status = newStatus
            )
            newState to GuessResult.Correct(matchedCategory)
        } else {
            // Sprawdzamy "prawie" — 3/4 z jednej kategorii
            val almostCategory = state.puzzle.categories.find { category ->
                category !in state.solvedCategories &&
                    state.selectedWords.count { it in category.words } == 3
            }

            val newMistakes = state.mistakesLeft - 1
            val newStatus = if (newMistakes <= 0) GameStatus.LOST else GameStatus.IN_PROGRESS

            // Przy przegranej odsłaniamy wszystkie pozostałe kategorie
            val newSolved = if (newStatus == GameStatus.LOST) {
                state.puzzle.categories
            } else {
                state.solvedCategories
            }

            val newRemaining = if (newStatus == GameStatus.LOST) {
                emptyList()
            } else {
                state.remainingWords
            }

            val newState = state.copy(
                remainingWords = newRemaining,
                solvedCategories = newSolved,
                selectedWords = emptyList(),
                mistakesLeft = newMistakes,
                guessHistory = newHistory,
                status = newStatus
            )

            val result = if (almostCategory != null) {
                GuessResult.AlmostCorrect
            } else {
                GuessResult.Wrong
            }
            newState to result
        }
    }
}
