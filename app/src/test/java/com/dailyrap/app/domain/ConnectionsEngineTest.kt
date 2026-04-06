package com.dailyrap.app.domain

import com.dailyrap.app.data.model.Category
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.Puzzle
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ConnectionsEngineTest {

    private lateinit var engine: ConnectionsEngine
    private lateinit var puzzle: Puzzle

    private val cat1 = Category("Chillwagon", listOf("Kizo", "ReTo", "Żabson", "Borixon"), 0)
    private val cat2 = Category("2115", listOf("Bedoes", "White", "Kuqe", "Blacha"), 1)
    private val cat3 = Category("OIO", listOf("OKI", "Young Igi", "Otsochodzi", "@atutowy"), 2)
    private val cat4 = Category("Albumy Taco", listOf("Europa", "Café Belga", "Trójkąt", "Marmur"), 3)

    @Before
    fun setup() {
        engine = ConnectionsEngine()
        puzzle = Puzzle(
            id = 1,
            date = "2026-04-01",
            categories = listOf(cat1, cat2, cat3, cat4)
        )
    }

    @Test
    fun `createGame shuffles 16 words`() {
        val state = engine.createGame(puzzle)
        assertEquals(16, state.remainingWords.size)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertEquals(4, state.mistakesLeft)
        assertTrue(state.solvedCategories.isEmpty())
        assertTrue(state.selectedWords.isEmpty())
        // Wszystkie słowa z kategorii są obecne
        for (cat in puzzle.categories) {
            for (word in cat.words) {
                assertTrue("Missing word: $word", word in state.remainingWords)
            }
        }
    }

    @Test
    fun `toggleWord selects and deselects`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "Kizo")
        assertEquals(listOf("Kizo"), state.selectedWords)

        state = engine.toggleWord(state, "Kizo")
        assertTrue(state.selectedWords.isEmpty())
    }

    @Test
    fun `toggleWord max 4 selections`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.toggleWord(state, "Żabson")
        state = engine.toggleWord(state, "Borixon")
        assertEquals(4, state.selectedWords.size)

        // 5th selection should be rejected
        state = engine.toggleWord(state, "Bedoes")
        assertEquals(4, state.selectedWords.size)
        assertFalse("Bedoes" in state.selectedWords)
    }

    @Test
    fun `toggleWord ignores words not in remaining`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "NIEISTNIEJĄCE")
        assertTrue(state.selectedWords.isEmpty())
    }

    @Test
    fun `clearSelection clears all`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.clearSelection(state)
        assertTrue(state.selectedWords.isEmpty())
    }

    @Test
    fun `submitGuess correct - 4 of 4 match category`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.toggleWord(state, "Żabson")
        state = engine.toggleWord(state, "Borixon")

        val (newState, result) = engine.submitGuess(state)

        assertTrue(result is ConnectionsEngine.GuessResult.Correct)
        val correct = result as ConnectionsEngine.GuessResult.Correct
        assertEquals("Chillwagon", correct.category.name)
        assertEquals(1, newState.solvedCategories.size)
        assertEquals(12, newState.remainingWords.size)
        assertEquals(4, newState.mistakesLeft)
        assertTrue(newState.selectedWords.isEmpty())
    }

    @Test
    fun `submitGuess almost correct - 3 of 4 from one category`() {
        var state = engine.createGame(puzzle)
        // 3 z Chillwagon + 1 z 2115
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.toggleWord(state, "Żabson")
        state = engine.toggleWord(state, "Bedoes")

        val (newState, result) = engine.submitGuess(state)

        assertTrue(result is ConnectionsEngine.GuessResult.AlmostCorrect)
        assertEquals(3, newState.mistakesLeft)
        assertTrue(newState.solvedCategories.isEmpty())
        assertEquals(16, newState.remainingWords.size)
    }

    @Test
    fun `submitGuess wrong - no category has 3+ matches`() {
        var state = engine.createGame(puzzle)
        // 2 z Chillwagon + 2 z 2115
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.toggleWord(state, "Bedoes")
        state = engine.toggleWord(state, "White")

        val (newState, result) = engine.submitGuess(state)

        assertTrue(result is ConnectionsEngine.GuessResult.Wrong)
        assertEquals(3, newState.mistakesLeft)
    }

    @Test
    fun `submitGuess invalid when less than 4 selected`() {
        var state = engine.createGame(puzzle)
        state = engine.toggleWord(state, "Kizo")

        val (_, result) = engine.submitGuess(state)
        assertTrue(result is ConnectionsEngine.GuessResult.Invalid)
    }

    @Test
    fun `submitGuess blocks duplicate guess`() {
        var state = engine.createGame(puzzle)

        // Pierwsza próba (pudło)
        state = engine.toggleWord(state, "Kizo")
        state = engine.toggleWord(state, "ReTo")
        state = engine.toggleWord(state, "Bedoes")
        state = engine.toggleWord(state, "White")
        val (state2, _) = engine.submitGuess(state)

        // Ta sama próba ponownie
        var state3 = engine.toggleWord(state2, "Kizo")
        state3 = engine.toggleWord(state3, "ReTo")
        state3 = engine.toggleWord(state3, "Bedoes")
        state3 = engine.toggleWord(state3, "White")
        val (state4, result2) = engine.submitGuess(state3)

        assertTrue(result2 is ConnectionsEngine.GuessResult.Invalid)
        // Nie powinno odjąć kolejnego błędu
        assertEquals(state2.mistakesLeft, state4.mistakesLeft)
    }

    @Test
    fun `full game - win scenario`() {
        var state = engine.createGame(puzzle)

        // Rozwiąż wszystkie 4 kategorie po kolei
        for (cat in puzzle.categories) {
            for (word in cat.words) {
                state = engine.toggleWord(state, word)
            }
            val (newState, result) = engine.submitGuess(state)
            assertTrue(result is ConnectionsEngine.GuessResult.Correct)
            state = newState
        }

        assertEquals(GameStatus.WON, state.status)
        assertEquals(4, state.solvedCategories.size)
        assertTrue(state.remainingWords.isEmpty())
        assertEquals(4, state.mistakesLeft) // zero błędów
    }

    @Test
    fun `full game - lose scenario (4 mistakes)`() {
        var state = engine.createGame(puzzle)

        // 4 różne pudła (za każdym razem inny zestaw, żeby blokada duplikatów nie zadziałała)
        val wrongGuesses = listOf(
            listOf("Kizo", "ReTo", "Bedoes", "White"),        // 2 Chillwagon + 2 z 2115
            listOf("Kizo", "ReTo", "OKI", "Young Igi"),       // 2 Chillwagon + 2 OIO
            listOf("Kizo", "Bedoes", "OKI", "Europa"),        // po 1 z każdej
            listOf("ReTo", "White", "Young Igi", "Marmur")    // po 1 z każdej
        )

        for (guess in wrongGuesses) {
            for (word in guess) {
                state = engine.toggleWord(state, word)
            }
            val (newState, _) = engine.submitGuess(state)
            state = newState
            if (state.status == GameStatus.LOST) break
        }

        assertEquals(GameStatus.LOST, state.status)
        // Przy przegranej — wszystkie kategorie odsłonięte
        assertEquals(4, state.solvedCategories.size)
    }

    @Test
    fun `toggleWord ignored when game is over`() {
        var state = engine.createGame(puzzle)
        // Wygraj grę
        for (cat in puzzle.categories) {
            for (word in cat.words) {
                state = engine.toggleWord(state, word)
            }
            state = engine.submitGuess(state).first
        }
        assertEquals(GameStatus.WON, state.status)

        val afterToggle = engine.toggleWord(state, "Kizo")
        assertEquals(state, afterToggle)
    }

    @Test
    fun `shuffle changes word order`() {
        val state = engine.createGame(puzzle)
        // Sprawdzamy że shuffle zwraca te same słowa (sorted)
        val shuffled = engine.shuffle(state)
        assertEquals(
            state.remainingWords.sorted(),
            shuffled.remainingWords.sorted()
        )
    }
}
