package com.dailyrap.app.ui.screen.literalnie.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.ui.theme.LetterAbsent
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

private val ROW1 = listOf('Q','W','E','R','T','Y','U','I','O','P')
private val ROW2 = listOf('A','S','D','F','G','H','J','K','L')
private val ROW3 = listOf('Z','X','C','V','B','N','M')
private val POLISH = listOf('Ą','Ć','Ę','Ł','Ń','Ó','Ś','Ź','Ż')

private fun keyColor(state: LetterResult?) = when (state) {
    LetterResult.CORRECT -> LetterCorrect
    LetterResult.PRESENT -> LetterPresent
    LetterResult.ABSENT -> LetterAbsent
    null -> TileDefault
}

@Composable
fun Keyboard(
    keyboardState: Map<Char, LetterResult>,
    onKey: (Char) -> Unit,
    onEnter: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: Polish characters (9 keys)
        KeyRow(keys = POLISH, keyboardState = keyboardState, onKey = onKey)

        // Row 2: QWERTYUIOP (10 keys)
        KeyRow(keys = ROW1, keyboardState = keyboardState, onKey = onKey)

        // Row 3: ASDFGHJKL (9 keys)
        KeyRow(keys = ROW2, keyboardState = keyboardState, onKey = onKey)

        // Row 4: BACKSPACE + ZXCVBNM + OK (9 elements, all weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionKey(text = "⌫", onClick = onDelete)

            ROW3.forEach { letter ->
                LetterKey(
                    letter = letter,
                    state = keyboardState[letter],
                    onClick = { onKey(letter) }
                )
            }

            ActionKey(text = "OK", onClick = onEnter)
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<Char>,
    keyboardState: Map<Char, LetterResult>,
    onKey: (Char) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { letter ->
            LetterKey(
                letter = letter,
                state = keyboardState[letter],
                onClick = { onKey(letter) }
            )
        }
    }
}

@Composable
private fun RowScope.LetterKey(
    letter: Char,
    state: LetterResult?,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = keyColor(state),
        animationSpec = tween(300),
        label = "key_$letter"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            color = TileText,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun RowScope.ActionKey(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TileDefault)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TileText,
            fontWeight = FontWeight.Bold,
            fontSize = if (text == "⌫") 18.sp else 12.sp
        )
    }
}
