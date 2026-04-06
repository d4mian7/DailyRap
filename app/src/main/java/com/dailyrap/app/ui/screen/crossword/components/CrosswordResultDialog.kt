package com.dailyrap.app.ui.screen.crossword.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.theme.AccentPurpleEnd
import com.dailyrap.app.ui.theme.AccentPurpleStart
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileText

fun buildCrosswordShareText(state: CrosswordGameState): String {
    val sb = StringBuilder()
    sb.appendLine("RapCrossword \uD83C\uDFA4 #${state.puzzle.id}")
    val emojiRow = state.puzzle.clues.indices.joinToString("") { i ->
        if (i in state.correctRows) "\u2705" else "\u274C"
    }
    sb.appendLine(emojiRow)
    sb.appendLine("Wynik: ${state.correctRows.size}/${state.puzzle.clues.size}")
    if (state.secretWordRevealed) {
        sb.append("Hasło główne: ${state.puzzle.secretWord} \uD83D\uDD13")
    } else {
        sb.append("Hasło nieodkryte \uD83D\uDD12")
    }
    return sb.toString()
}

@Composable
fun CrosswordResultDialog(
    gameState: CrosswordGameState,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val won = gameState.status == GameStatus.WON
    val correct = gameState.correctRows.size
    val total = gameState.puzzle.clues.size

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDark, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header — no game ID
            Text(
                text = if (won) "\uD83C\uDF89 Brawo!" else "\uD83D\uDE14 Przegrana",
                color = if (won) LetterCorrect else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emoji row
            val emojiRow = gameState.puzzle.clues.indices.joinToString("") { i ->
                if (i in gameState.correctRows) "\u2705" else "\u274C"
            }
            Text(
                text = emojiRow,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Wynik: $correct/$total",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Secret word status
            if (gameState.secretWordRevealed) {
                Text(
                    text = "Hasło: ${gameState.puzzle.secretWord} \uD83D\uDD13",
                    color = LetterPresent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            } else {
                Text(
                    text = "Hasło nieodkryte \uD83D\uDD12",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = TextSecondary.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // Show all answers
            Text(
                text = "Rozwiązanie",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            gameState.puzzle.clues.forEachIndexed { i, clue ->
                val isCorrectRow = i in gameState.correctRows
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${i + 1}. ${clue.question}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Answer as chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isCorrectRow) LetterCorrect.copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = clue.answer.uppercase(),
                            color = if (isCorrectRow) LetterCorrect else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
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
                    .background(gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val text = buildCrosswordShareText(gameState)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RapCrossword", text))
                        Toast.makeText(context, "Skopiowano do schowka!", Toast.LENGTH_SHORT).show()
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
