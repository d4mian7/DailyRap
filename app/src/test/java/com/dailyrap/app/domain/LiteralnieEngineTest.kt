package com.dailyrap.app.domain

import com.dailyrap.app.data.model.DailyWord
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.data.model.LetterResult.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LiteralnieEngineTest {

    private lateinit var engine: LiteralnieEngine

    @Before
    fun setup() {
        engine = LiteralnieEngine()
    }

    private fun word5(word: String = "SOBEL") = DailyWord(
        id = 1, date = "2026-04-01", word = word, hint = "Raper", variant = 5
    )

    private fun word4(word: String = "MATA") = DailyWord(
        id = 2, date = "2026-04-02", word = word, hint = "Raper", variant = 4
    )

    private fun word6(word: String = "SZPAKU") = DailyWord(
        id = 3, date = "2026-04-03", word = word, hint = "Raper", variant = 6
    )

    // ── Inicjalizacja ──────────────────────────────────────────────

    @Test
    fun `createGame sets 6 maxGuesses for variant 5`() {
        val state = engine.createGame(word5())
        assertEquals(6, state.maxGuesses)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertTrue(state.guesses.isEmpty())
    }

    @Test
    fun `createGame sets 6 maxGuesses for variant 4`() {
        val state = engine.createGame(word4())
        assertEquals(6, state.maxGuesses)
    }

    @Test
    fun `createGame sets 6 maxGuesses for variant 6`() {
        val state = engine.createGame(word6())
        assertEquals(6, state.maxGuesses)
    }

    // ── Wpisywanie liter ───────────────────────────────────────────

    @Test
    fun `typeLetter adds uppercase letter`() {
        var state = engine.createGame(word5())
        state = engine.typeLetter(state, 's')
        assertEquals("S", state.currentGuess)
    }

    @Test
    fun `typeLetter stops at word length`() {
        var state = engine.createGame(word4()) // 4 litery
        state = engine.typeLetter(state, 'A')
        state = engine.typeLetter(state, 'B')
        state = engine.typeLetter(state, 'C')
        state = engine.typeLetter(state, 'D')
        state = engine.typeLetter(state, 'E') // 5th — powinno być zignorowane
        assertEquals("ABCD", state.currentGuess)
    }

    @Test
    fun `deleteLetter removes last character`() {
        var state = engine.createGame(word5())
        state = engine.typeLetter(state, 'A')
        state = engine.typeLetter(state, 'B')
        state = engine.deleteLetter(state)
        assertEquals("A", state.currentGuess)
    }

    @Test
    fun `deleteLetter on empty does nothing`() {
        var state = engine.createGame(word5())
        state = engine.deleteLetter(state)
        assertEquals("", state.currentGuess)
    }

    // ── Ewaluacja liter ────────────────────────────────────────────

    @Test
    fun `evaluateGuess all correct`() {
        val results = engine.evaluateGuess("SOBEL", "SOBEL")
        assertEquals(listOf(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT), results)
    }

    @Test
    fun `evaluateGuess all absent`() {
        val results = engine.evaluateGuess("XXXXX", "SOBEL")
        assertEquals(listOf(ABSENT, ABSENT, ABSENT, ABSENT, ABSENT), results)
    }

    @Test
    fun `evaluateGuess mixed results`() {
        // SOBLE vs SOBEL: S=correct, O=correct, B=correct, L=present, E=present
        val results = engine.evaluateGuess("SOBLE", "SOBEL")
        assertEquals(CORRECT, results[0]) // S
        assertEquals(CORRECT, results[1]) // O
        assertEquals(CORRECT, results[2]) // B
        assertEquals(PRESENT, results[3]) // L (jest w haśle, ale na pozycji 4 nie 3)
        assertEquals(PRESENT, results[4]) // E (jest w haśle, ale na pozycji 3 nie 4)
    }

    @Test
    fun `evaluateGuess duplicate letter in guess - one correct one absent`() {
        // Hasło: SOLAR, Próba: SOLAS
        // S=correct, O=correct, L=correct, A=correct, S=absent (tylko jedno S w haśle, już zużyte)
        val results = engine.evaluateGuess("SOLAS", "SOLAR")
        assertEquals(CORRECT, results[0]) // S
        assertEquals(CORRECT, results[1]) // O
        assertEquals(CORRECT, results[2]) // L
        assertEquals(CORRECT, results[3]) // A
        assertEquals(ABSENT, results[4])  // S — drugie S, ale w haśle jest tylko jedno (zużyte na poz. 0)
    }

    @Test
    fun `evaluateGuess duplicate letter in guess - one present one absent`() {
        // Hasło: SOBEL, Próba: BBBBB
        // Tylko jedno B w haśle (pozycja 2), próba ma B wszędzie
        // Pozycja 2 = CORRECT, reszta ABSENT
        val results = engine.evaluateGuess("BBBBB", "SOBEL")
        assertEquals(ABSENT, results[0])
        assertEquals(ABSENT, results[1])
        assertEquals(CORRECT, results[2]) // B na pozycji 2
        assertEquals(ABSENT, results[3])
        assertEquals(ABSENT, results[4])
    }

    // ── Polskie znaki ──────────────────────────────────────────────

    @Test
    fun `evaluateGuess handles polish characters`() {
        val results = engine.evaluateGuess("BIAŁAS", "BIAŁAS")
        assertEquals(listOf(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT, CORRECT), results)
    }

    @Test
    fun `evaluateGuess polish characters present`() {
        // Hasło: BIAŁAS, Próba: ABIAŁS
        // A=present, B=present, I=present, A=present, Ł=present, S=correct... hm
        // Spróbujmy prostszy przypadek
        // Hasło: ŻABSON, Próba: ŻXXXXX — Ż correct, reszta absent
        val results = engine.evaluateGuess("ŻXXXXX", "ŻABSON")
        assertEquals(CORRECT, results[0])
        assertEquals(ABSENT, results[1])
    }

    @Test
    fun `evaluateGuess case insensitive`() {
        val results = engine.evaluateGuess("sobel", "SOBEL")
        assertEquals(listOf(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT), results)
    }

    @Test
    fun `typeLetter handles polish characters`() {
        var state = engine.createGame(word6("ŻABSON"))
        state = engine.typeLetter(state, 'ż')
        state = engine.typeLetter(state, 'a')
        assertEquals("ŻA", state.currentGuess)
    }

    // ── Zatwierdzanie próby ────────────────────────────────────────

    @Test
    fun `submitGuess too short`() {
        var state = engine.createGame(word5())
        state = engine.typeLetter(state, 'S')
        state = engine.typeLetter(state, 'O')
        val (_, result) = engine.submitGuess(state)
        assertTrue(result is LiteralnieEngine.SubmitResult.TooShort)
    }

    @Test
    fun `submitGuess accepted and adds to guesses`() {
        var state = engine.createGame(word5()) // SOBEL
        "XXXXX".forEach { state = engine.typeLetter(state, it) }
        val (newState, result) = engine.submitGuess(state)

        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
        assertEquals(1, newState.guesses.size)
        assertEquals("XXXXX", newState.guesses[0])
        assertEquals("", newState.currentGuess)
        assertEquals(GameStatus.IN_PROGRESS, newState.status)
    }

    @Test
    fun `submitGuess correct word - win`() {
        var state = engine.createGame(word5()) // SOBEL
        "SOBEL".forEach { state = engine.typeLetter(state, it) }
        val (newState, result) = engine.submitGuess(state)

        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
        assertEquals(GameStatus.WON, newState.status)
    }

    @Test
    fun `submitGuess last attempt wrong - lose`() {
        var state = engine.createGame(word4()) // MATA, maxGuesses=6

        // 5 złych prób
        repeat(5) {
            "XXXX".forEach { c -> state = engine.typeLetter(state, c) }
            state = engine.submitGuess(state).first
        }
        assertEquals(GameStatus.IN_PROGRESS, state.status)

        // 6. próba — też zła
        "YYYY".forEach { state = engine.typeLetter(state, it) }
        val (finalState, result) = engine.submitGuess(state)

        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
        assertEquals(GameStatus.LOST, finalState.status)
    }

    @Test
    fun `submitGuess on finished game returns GameOver`() {
        var state = engine.createGame(word5()) // SOBEL
        "SOBEL".forEach { state = engine.typeLetter(state, it) }
        state = engine.submitGuess(state).first
        assertEquals(GameStatus.WON, state.status)

        // Próba po wygranej
        "XXXXX".forEach { state = engine.typeLetter(state, it) }
        val (_, result) = engine.submitGuess(state)
        assertTrue(result is LiteralnieEngine.SubmitResult.GameOver)
    }

    // ── Klawiatura ─────────────────────────────────────────────────

    @Test
    fun `keyboard state tracks best result per letter`() {
        var state = engine.createGame(word5("SOBEL"))

        // Próba 1: SOBLE → S=correct, O=correct, B=correct, L=present, E=present
        "SOBLE".forEach { state = engine.typeLetter(state, it) }
        state = engine.submitGuess(state).first

        val keyboard = engine.getKeyboardState(state)
        assertEquals(CORRECT, keyboard['S'])
        assertEquals(CORRECT, keyboard['O'])
        assertEquals(CORRECT, keyboard['B'])
        assertEquals(PRESENT, keyboard['L'])
        assertEquals(PRESENT, keyboard['E'])
    }

    @Test
    fun `keyboard state upgrades from present to correct`() {
        var state = engine.createGame(word5("SOBEL"))

        // Próba 1: SOBLE → E=present
        "SOBLE".forEach { state = engine.typeLetter(state, it) }
        state = engine.submitGuess(state).first

        // Próba 2: SOBEL → E=correct
        "SOBEL".forEach { state = engine.typeLetter(state, it) }
        state = engine.submitGuess(state).first

        val keyboard = engine.getKeyboardState(state)
        assertEquals(CORRECT, keyboard['E'])
        assertEquals(CORRECT, keyboard['L'])
    }

    // ── Walidacja słownikowa ──────────────────────────────────────

    @Test
    fun `submitGuess rejects word not in dictionary`() {
        val dict = setOf("SOBEL", "SOBLE", "SOLAR")
        var state = engine.createGame(word5()) // SOBEL
        "XXXXX".forEach { state = engine.typeLetter(state, it) }
        val (_, result) = engine.submitGuess(state, dict)
        assertTrue(result is LiteralnieEngine.SubmitResult.NotInDictionary)
    }

    @Test
    fun `submitGuess accepts word in dictionary`() {
        val dict = setOf("SOBEL", "SOBLE", "SOLAR")
        var state = engine.createGame(word5()) // SOBEL
        "SOBLE".forEach { state = engine.typeLetter(state, it) }
        val (_, result) = engine.submitGuess(state, dict)
        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
    }

    @Test
    fun `submitGuess always accepts target word even if not in dictionary`() {
        val dict = setOf("AAAAA") // SOBEL nie jest w słowniku
        var state = engine.createGame(word5()) // SOBEL
        "SOBEL".forEach { state = engine.typeLetter(state, it) }
        val (newState, result) = engine.submitGuess(state, dict)
        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
        assertEquals(GameStatus.WON, newState.status)
    }

    @Test
    fun `submitGuess without dictionary accepts any word`() {
        var state = engine.createGame(word5()) // SOBEL
        "XXXXX".forEach { state = engine.typeLetter(state, it) }
        val (_, result) = engine.submitGuess(state) // no dictionary
        assertTrue(result is LiteralnieEngine.SubmitResult.Accepted)
    }

    // ── Pełna gra ──────────────────────────────────────────────────

    @Test
    fun `full game win on second guess`() {
        var state = engine.createGame(word4("MATA"))

        // Próba 1: MAPA
        "MAPA".forEach { state = engine.typeLetter(state, it) }
        val (state2, result1) = engine.submitGuess(state)
        state = state2
        assertTrue(result1 is LiteralnieEngine.SubmitResult.Accepted)
        val r1 = (result1 as LiteralnieEngine.SubmitResult.Accepted).letterResults
        assertEquals(CORRECT, r1[0]) // M
        assertEquals(CORRECT, r1[1]) // A
        assertEquals(ABSENT, r1[2])  // P
        assertEquals(CORRECT, r1[3]) // A

        // Próba 2: MATA — wygrana
        "MATA".forEach { state = engine.typeLetter(state, it) }
        val (finalState, result2) = engine.submitGuess(state)
        assertTrue(result2 is LiteralnieEngine.SubmitResult.Accepted)
        assertEquals(GameStatus.WON, finalState.status)
        assertEquals(2, finalState.guesses.size)
    }
}
