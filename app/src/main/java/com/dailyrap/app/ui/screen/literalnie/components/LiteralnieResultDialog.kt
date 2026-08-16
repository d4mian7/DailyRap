package com.dailyrap.app.ui.screen.literalnie.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.data.model.LiteralnieGameState
import com.dailyrap.app.ui.theme.AccentPurpleEnd
import com.dailyrap.app.ui.theme.AccentPurpleStart
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileText

private fun resultEmoji(result: LetterResult): String = when (result) {
    LetterResult.CORRECT -> "\uD83D\uDFE9"
    LetterResult.PRESENT -> "\uD83D\uDFE8"
    LetterResult.ABSENT -> "\u2B1B"
}

fun buildLiteralnieShareText(
    state: LiteralnieGameState,
    guessResults: List<List<LetterResult>>
): String {
    val won = state.status == GameStatus.WON
    val totalAllowed = state.maxGuesses + state.extraAttemptsUsed
    val tries = if (won) "${state.guesses.size}/$totalAllowed" else "X/$totalAllowed"

    val sb = StringBuilder()
    sb.appendLine("RapWord \uD83C\uDFA4 #${state.dailyWord.id} (${state.dailyWord.variant} liter)")
    sb.appendLine()
    for (row in guessResults) {
        sb.appendLine(row.joinToString("") { resultEmoji(it) })
    }
    sb.appendLine()
    sb.append("Próby: $tries")
    return sb.toString()
}

@Composable
fun LiteralnieResultDialog(
    gameState: LiteralnieGameState,
    guessResults: List<List<LetterResult>>,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val won = gameState.status == GameStatus.WON

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDark, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header — no game ID
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

            Spacer(modifier = Modifier.height(12.dp))

            // Answer word — white
            Text(
                text = "Hasło: ${gameState.dailyWord.word}",
                color = TileText,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji grid — larger
            val emojiGrid = guessResults.joinToString("\n") { row ->
                row.joinToString("") { resultEmoji(it) }
            }
            Text(
                text = emojiGrid,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val totalAllowed = gameState.maxGuesses + gameState.extraAttemptsUsed
            val tries = if (won) "${gameState.guesses.size}/$totalAllowed" else "X/$totalAllowed"
            Text(
                text = "Próby: $tries",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        val text = buildLiteralnieShareText(gameState, guessResults)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("RapWord", text))
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
