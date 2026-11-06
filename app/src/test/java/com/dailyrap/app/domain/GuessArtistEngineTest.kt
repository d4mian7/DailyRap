package com.dailyrap.app.domain

import com.dailyrap.app.data.model.ArtistPuzzle
import com.dailyrap.app.data.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuessArtistEngineTest {

    private val engine = GuessArtistEngine()
    private val puzzle = ArtistPuzzle(
        id = 1,
        date = "2026-11-01",
        artist = "Taco Hemingway",
        clues = listOf("t1", "t2", "t3", "t4", "t5")
    )
    private val pool = listOf("Taco Hemingway", "Taconafide", "Hubert.", "Żabson", "Białas", "Jan-rapowanie")

    @Test
    fun `game starts with only first clue revealed`() {
        val state = engine.createGame(puzzle)
        assertEquals(1, state.revealedClues)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
    }

    @Test
    fun `correct guess wins and keeps clue count`() {
        val start = engine.nextClue(engine.createGame(puzzle))
        val (state, result) = engine.submitGuess(start, "Taco Hemingway")
        assertEquals(GuessArtistEngine.GuessResult.Correct, result)
        assertEquals(GameStatus.WON, state.status)
        assertEquals(2, state.revealedClues)
    }

    @Test
    fun `wrong guess reveals next clue`() {
        val (state, result) = engine.submitGuess(engine.createGame(puzzle), "Żabson")
        assertEquals(GuessArtistEngine.GuessResult.Wrong, result)
        assertEquals(2, state.revealedClues)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
    }

    @Test
    fun `repeated guess costs nothing`() {
        val (first, _) = engine.submitGuess(engine.createGame(puzzle), "Żabson")
        val (second, result) = engine.submitGuess(first, "żabson")
        assertEquals(GuessArtistEngine.GuessResult.AlreadyGuessed, result)
        assertEquals(first, second)
    }

    @Test
    fun `wrong guess on last clue loses`() {
        var state = engine.createGame(puzzle)
        repeat(4) { state = engine.nextClue(state) }
        assertFalse(engine.hasNextClue(state))
        val (lost, _) = engine.submitGuess(state, "Żabson")
        assertEquals(GameStatus.LOST, lost.status)
    }

    @Test
    fun `next clue stops at last clue`() {
        var state = engine.createGame(puzzle)
        repeat(10) { state = engine.nextClue(state) }
        assertEquals(5, state.revealedClues)
    }

    @Test
    fun `extra chance gives one more guess only once`() {
        var state = engine.createGame(puzzle)
        repeat(4) { state = engine.nextClue(state) }
        val (lost, _) = engine.submitGuess(state, "Żabson")
        val extra = engine.grantExtraChance(lost)
        assertEquals(GameStatus.IN_PROGRESS, extra.status)
        assertEquals(5, extra.revealedClues)

        val (lostAgain, _) = engine.submitGuess(extra, "Białas")
        assertEquals(GameStatus.LOST, lostAgain.status)
        assertEquals(GameStatus.LOST, engine.grantExtraChance(lostAgain).status)
    }

    @Test
    fun `extra chance can win`() {
        var state = engine.createGame(puzzle)
        repeat(4) { state = engine.nextClue(state) }
        val (lost, _) = engine.submitGuess(state, "Żabson")
        val (won, result) = engine.submitGuess(engine.grantExtraChance(lost), "Taco Hemingway")
        assertEquals(GuessArtistEngine.GuessResult.Correct, result)
        assertEquals(GameStatus.WON, won.status)
    }

    @Test
    fun `no guesses after game over`() {
        val (won, _) = engine.submitGuess(engine.createGame(puzzle), "Taco Hemingway")
        assertEquals(GuessArtistEngine.GuessResult.GameOver, engine.submitGuess(won, "Żabson").second)
    }

    @Test
    fun `no suggestions before first letter`() {
        assertTrue(engine.suggestions("", pool, emptyList()).isEmpty())
        assertTrue(engine.suggestions("  ", pool, emptyList()).isEmpty())
    }

    @Test
    fun `suggestions match any part of name and ignore polish letters`() {
        assertEquals(listOf("Taco Hemingway", "Taconafide"), engine.suggestions("taco", pool, emptyList()))
        assertEquals(listOf("Hubert.", "Taco Hemingway"), engine.suggestions("H", pool, emptyList()))
        assertEquals(listOf("Żabson"), engine.suggestions("zab", pool, emptyList()))
        assertEquals(listOf("Białas"), engine.suggestions("bial", pool, emptyList()))
        assertEquals(listOf("Jan-rapowanie"), engine.suggestions("rap", pool, emptyList()))
    }

    @Test
    fun `suggestions hide already guessed artists`() {
        assertEquals(listOf("Taconafide"), engine.suggestions("taco", pool, listOf("Taco Hemingway")))
    }
}
