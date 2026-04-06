package com.dailyrap.app.ui.screen.literalnie.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.LetterResult

@Composable
fun LetterGrid(
    guesses: List<String>,
    currentGuess: String,
    maxGuesses: Int,
    wordLength: Int,
    guessResults: List<List<LetterResult>>,
    lastRevealedRow: Int,
    modifier: Modifier = Modifier
) {
    // Base sizes for 6 rows, scaled down for extra rows (7, 8, 9)
    val tileSize = when {
        maxGuesses <= 6 -> when (wordLength) { 4 -> 56.dp; 5 -> 52.dp; else -> 50.dp }
        maxGuesses == 7 -> when (wordLength) { 4 -> 48.dp; 5 -> 44.dp; else -> 42.dp }
        maxGuesses == 8 -> when (wordLength) { 4 -> 42.dp; 5 -> 39.dp; else -> 37.dp }
        else ->            when (wordLength) { 4 -> 38.dp; 5 -> 35.dp; else -> 33.dp }
    }
    val tileFontSize = when {
        maxGuesses <= 6 -> when (wordLength) { 4 -> 26.sp; 5 -> 24.sp; else -> 22.sp }
        maxGuesses == 7 -> when (wordLength) { 4 -> 22.sp; 5 -> 20.sp; else -> 18.sp }
        maxGuesses == 8 -> when (wordLength) { 4 -> 20.sp; 5 -> 18.sp; else -> 16.sp }
        else ->            when (wordLength) { 4 -> 18.sp; 5 -> 16.sp; else -> 15.sp }
    }
    val tileSpacing = when {
        maxGuesses > 7 -> 4.dp
        wordLength >= 6 -> 5.dp
        else -> 6.dp
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tileSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (rowIndex in 0 until maxGuesses) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(tileSpacing)
            ) {
                for (colIndex in 0 until wordLength) {
                    val letter: Char?
                    val result: LetterResult?
                    val isNewReveal: Boolean

                    when {
                        // Completed guess row
                        rowIndex < guesses.size -> {
                            letter = guesses[rowIndex].getOrNull(colIndex)
                            result = guessResults.getOrNull(rowIndex)?.getOrNull(colIndex)
                            isNewReveal = rowIndex == lastRevealedRow
                        }
                        // Current guess row
                        rowIndex == guesses.size -> {
                            letter = currentGuess.getOrNull(colIndex)
                            result = null
                            isNewReveal = false
                        }
                        // Future empty row
                        else -> {
                            letter = null
                            result = null
                            isNewReveal = false
                        }
                    }

                    LetterTile(
                        letter = letter,
                        result = result,
                        tileSize = tileSize,
                        fontSize = tileFontSize,
                        flipDelay = if (isNewReveal) colIndex * 150 else 0,
                        bounce = rowIndex == guesses.size && letter != null
                    )
                }
            }
        }
    }
}
