package com.dailyrap.app.ui.screen.crossword.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.CrosswordClue
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText

private val ErrorRed = Color(0xFFEF4444)

@Composable
fun ClueRow(
    index: Int,
    clue: CrosswordClue,
    currentAnswer: String,
    isChecked: Boolean,
    isCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isCorrect -> LetterCorrect.copy(alpha = 0.15f)
            isChecked -> ErrorRed.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "clueBg"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        // Question
        Text(
            text = "${index + 1}. ${clue.question}",
            color = TileText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Answer letter boxes
            val answerLength = clue.answer.length
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f)
            ) {
                for (i in 0 until answerLength) {
                    val isHighlight = i == clue.highlightIndex
                    val letter = currentAnswer.uppercase().getOrNull(i)

                    val boxBg = when {
                        isCorrect && isHighlight -> TileSelected
                        isCorrect -> LetterCorrect
                        isChecked && !isCorrect -> ErrorRed.copy(alpha = 0.4f)
                        isHighlight -> TileSelected.copy(alpha = 0.3f)
                        else -> TileDefault
                    }

                    val boxSize = when {
                        answerLength > 8 -> 26.dp
                        answerLength > 6 -> 30.dp
                        else -> 34.dp
                    }
                    val fontSize = when {
                        answerLength > 8 -> 12.sp
                        answerLength > 6 -> 13.sp
                        else -> 15.sp
                    }

                    Box(
                        modifier = Modifier
                            .size(boxSize)
                            .clip(RoundedCornerShape(6.dp))
                            .background(boxBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter?.toString() ?: "",
                            color = TileText,
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Check button
            if (!isCorrect) {
                Button(
                    onClick = onCheck,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "\u2713",
                        color = TileText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Text field (hidden input)
        if (!isCorrect) {
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = currentAnswer,
                onValueChange = { onAnswerChange(it.take(clue.answer.length)) },
                textStyle = TextStyle(
                    color = TileText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(AccentPurple),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onCheck() }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TileDefault)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (currentAnswer.isEmpty()) {
                            Text(
                                text = "Wpisz odpowiedź...",
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
