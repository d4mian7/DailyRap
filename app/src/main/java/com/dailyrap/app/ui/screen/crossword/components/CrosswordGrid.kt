package com.dailyrap.app.ui.screen.crossword.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.CrosswordClue
import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

private val ErrorRed = Color(0xFFEF4444)
private val SecretRowPurple = Color(0xFF8B5CF6)
private val NumberGold = Color(0xFFEAB308)
private val CellSpacing = 3.dp
private val NumberRowHeight = 22.dp

/**
 * Transposed crossword layout:
 * - Secret word runs HORIZONTALLY in one row (secretRow).
 * - Each answer runs VERTICALLY as a column.
 * - highlightIndex determines where the answer's letter sits in the secret row.
 *   Letters before highlightIndex go UP, letters after go DOWN.
 *
 * secretRow — the row index of the horizontal secret word.
 * totalRows — total vertical cells (from topmost letter to bottommost).
 * columnStarts — for each clue, the row at which its first letter appears.
 */
data class GridLayout(
    val secretRow: Int,
    val totalRows: Int,
    val columnStarts: List<Int>
)

fun calculateGridLayout(clues: List<CrosswordClue>): GridLayout {
    val maxHighlight = clues.maxOf { it.highlightIndex }
    val secretRow = maxHighlight
    val columnStarts = clues.map { secretRow - it.highlightIndex }
    val totalRows = clues.indices.maxOf { i ->
        columnStarts[i] + clues[i].answer.length
    }
    return GridLayout(secretRow, totalRows, columnStarts)
}

@Composable
fun CrosswordGrid(
    gameState: CrosswordGameState,
    selectedClueIndex: Int?,
    onClueClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val clues = gameState.puzzle.clues
    val layout = remember(clues) { calculateGridLayout(clues) }
    val numColumns = clues.size

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val totalHSpacing = CellSpacing * (numColumns - 1)
        val cellFromWidth = (availableWidth - totalHSpacing) / numColumns

        // Calculate cell size from height: total grid height = NumberRowHeight + 2.dp + totalRows * cell + (totalRows-1) * spacing
        val totalVSpacing = CellSpacing * (layout.totalRows - 1)
        val heightForCells = availableHeight - NumberRowHeight - 2.dp - totalVSpacing
        val cellFromHeight = if (layout.totalRows > 0) heightForCells / layout.totalRows else cellFromWidth

        val cellSize = minOf(cellFromWidth, cellFromHeight).coerceIn(14.dp, 44.dp)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row: clue numbers above each column
            Row(
                horizontalArrangement = Arrangement.spacedBy(CellSpacing)
            ) {
                clues.forEachIndexed { colIndex, _ ->
                    val isSelected = colIndex == selectedClueIndex
                    Box(
                        modifier = Modifier
                            .size(width = cellSize, height = NumberRowHeight)
                            .clickable { onClueClick(colIndex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${colIndex + 1}",
                            color = if (isSelected) Color.White else NumberGold,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Grid rows (top to bottom)
            for (row in 0 until layout.totalRows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CellSpacing)
                ) {
                    clues.forEachIndexed { colIndex, clue ->
                        val letterIndex = row - layout.columnStarts[colIndex]
                        val isInWord = letterIndex in 0 until clue.answer.length
                        val isSecretRow = row == layout.secretRow

                        val isChecked = colIndex in gameState.checkedRows
                        val isCorrect = colIndex in gameState.correctRows
                        val isSelected = colIndex == selectedClueIndex
                        val currentAnswer = gameState.answers[colIndex] ?: ""

                        if (isInWord) {
                            val isHighlight = letterIndex == clue.highlightIndex
                            val letter = when {
                                // Secret word fully revealed → show actual secret letter
                                isHighlight && gameState.secretWordRevealed ->
                                    gameState.puzzle.secretWord.uppercase().getOrNull(colIndex)
                                // Highlight cell on uncorrected clue → show typed secret word letter live, fallback to vertical answer
                                isHighlight && !isCorrect -> {
                                    val secretLetter = gameState.secretWordAnswer.uppercase().getOrNull(colIndex)
                                    secretLetter ?: currentAnswer.uppercase().getOrNull(letterIndex)
                                }
                                // Normal cell → show vertical answer letter
                                else -> currentAnswer.uppercase().getOrNull(letterIndex)
                            }

                            val isSecretWrong = isHighlight &&
                                    gameState.secretWordChecked && !gameState.secretWordCorrect

                            GridCell(
                                letter = letter,
                                isHighlight = isHighlight,
                                isSecretRow = isSecretRow,
                                isChecked = isChecked,
                                isCorrect = isCorrect,
                                isSelected = isSelected,
                                isSecretRevealed = gameState.secretWordRevealed,
                                isSecretWordWrong = isSecretWrong,
                                cellSize = cellSize,
                                onClick = { onClueClick(colIndex) }
                            )
                        } else {
                            // Empty — transparent spacer
                            Spacer(modifier = Modifier.size(cellSize))
                        }
                    }
                }
                if (row < layout.totalRows - 1) {
                    Spacer(modifier = Modifier.height(CellSpacing))
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    letter: Char?,
    isHighlight: Boolean,
    isSecretRow: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    isSecretRevealed: Boolean,
    isSecretWordWrong: Boolean = false,
    cellSize: Dp,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            // Correct + highlight (secret word letter) + secret revealed → bright purple
            isCorrect && isHighlight && isSecretRevealed -> SecretRowPurple
            // Correct + highlight → muted purple
            isCorrect && isHighlight -> SecretRowPurple.copy(alpha = 0.7f)
            // Correct answer cell → green
            isCorrect -> LetterCorrect
            // Secret word checked wrong → red highlight cells
            isSecretWordWrong -> ErrorRed.copy(alpha = 0.5f)
            // Checked but wrong → red
            isChecked && !isCorrect -> ErrorRed.copy(alpha = 0.4f)
            // Secret word row (highlight position) — purple tint to show the secret row
            isHighlight -> SecretRowPurple.copy(alpha = 0.25f)
            // Normal cell
            else -> TileDefault
        },
        animationSpec = tween(300),
        label = "cellBg"
    )

    val borderColor = when {
        isSelected && !isChecked -> SecretRowPurple.copy(alpha = 0.6f)
        isHighlight && !isChecked -> SecretRowPurple.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = letter,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.7f))
                    .togetherWith(fadeOut(tween(100)))
            },
            contentAlignment = Alignment.Center,
            label = "cellLetter"
        ) { animatedLetter ->
            if (animatedLetter != null) {
                val size = when {
                    cellSize < 26.dp -> 10.sp
                    cellSize < 30.dp -> 12.sp
                    cellSize < 34.dp -> 14.sp
                    else -> 16.sp
                }
                Text(
                    text = animatedLetter.toString(),
                    color = when {
                        isCorrect && isHighlight && isSecretRevealed -> Color.White
                        else -> TileText
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = size,
                    lineHeight = size,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}
