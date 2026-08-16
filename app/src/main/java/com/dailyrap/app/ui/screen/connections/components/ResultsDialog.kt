package com.dailyrap.app.ui.screen.connections.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dailyrap.app.data.model.ConnectionsGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.theme.AccentPurpleEnd
import com.dailyrap.app.ui.theme.AccentPurpleStart
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileText

private fun difficultyEmoji(difficulty: Int): String = when (difficulty) {
    0 -> "\uD83D\uDFE8"  // yellow
    1 -> "\uD83D\uDFE9"  // green
    2 -> "\uD83D\uDFE6"  // blue
    3 -> "\uD83D\uDFEA"  // purple
    else -> "\u2B1C"      // white
}

fun buildEmojiGrid(state: ConnectionsGameState): String {
    val sb = StringBuilder()
    sb.appendLine("RapConnect \uD83C\uDFA4 #${state.puzzle.id}")
    sb.appendLine()

    for (guess in state.guessHistory) {
        val row = guess.map { word ->
            val category = state.puzzle.categories.find { word in it.words }
            difficultyEmoji(category?.difficulty ?: -1)
        }.joinToString("")
        sb.appendLine(row)
    }

    val mistakes = 4 - state.mistakesLeft
    sb.appendLine()
    sb.append("Błędy: $mistakes/4")
    return sb.toString()
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%d:%02d", m, s)
}

@Composable
fun ResultsDialog(
    gameState: ConnectionsGameState,
    elapsedSeconds: Int,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val won = gameState.status == GameStatus.WON
    val mistakes = 4 - gameState.mistakesLeft

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDark, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon — no game ID
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (won) Icons.Default.Celebration else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = if (won) LetterCorrect else Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (won) "Brawo!" else "Przegrana",
                    color = if (won) LetterCorrect else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji grid — larger squares
            Text(
                text = buildEmojiGrid(gameState).lines()
                    .drop(2) // skip title and empty line
                    .takeWhile { it.isNotBlank() }
                    .joinToString("\n"),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Błędy: $mistakes/4  •  Czas: ${formatTime(elapsedSeconds)}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = TextSecondary.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // Show all 4 categories with colored backgrounds
            Text(
                text = "Rozwiązanie",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            gameState.puzzle.categories.sortedBy { it.difficulty }.forEach { category ->
                val color = categoryColor(category.difficulty)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = category.name.uppercase(),
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = category.words.joinToString(", "),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share button — gradient purple
            val gradientBrush = Brush.linearGradient(
                colors = listOf(AccentPurpleStart, AccentPurpleEnd),
                start = Offset(0f, 0f),
                end = Offset(400f, 0f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradientBrush)
                    .then(
                        Modifier.padding(0.dp) // clickable wrapper
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val text = buildEmojiGrid(gameState) +
                                "  |  Czas: ${formatTime(elapsedSeconds)}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Udostępnij wynik"))
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "UDOSTĘPNIJ",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TileText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Go home — outline with white border
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(
                    text = "WRÓĆ DO MENU",
                    color = TileText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
