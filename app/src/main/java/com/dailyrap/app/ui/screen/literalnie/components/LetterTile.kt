package com.dailyrap.app.ui.screen.literalnie.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.ui.theme.LetterAbsent
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

fun letterResultColor(result: LetterResult?): Color = when (result) {
    LetterResult.CORRECT -> LetterCorrect
    LetterResult.PRESENT -> LetterPresent
    LetterResult.ABSENT -> LetterAbsent
    null -> TileDefault
}

@Composable
fun LetterTile(
    letter: Char?,
    result: LetterResult?,
    tileSize: Dp = 52.dp,
    fontSize: TextUnit = 22.sp,
    flipDelay: Int = 0,
    bounce: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = letterResultColor(result),
        animationSpec = tween(durationMillis = 300, delayMillis = flipDelay),
        label = "letterBg"
    )

    // Flip animation
    val flipRotation = remember { Animatable(0f) }
    LaunchedEffect(result) {
        if (result != null) {
            flipRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(500, delayMillis = flipDelay)
            )
        } else {
            flipRotation.snapTo(0f)
        }
    }

    // Bounce animation
    val bounceScale = remember { Animatable(1f) }
    LaunchedEffect(bounce, letter) {
        if (bounce && letter != null) {
            bounceScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 150
                    1.15f at 50
                    1f at 150
                }
            )
        }
    }

    val borderColor = when {
        letter != null && result == null -> Color(0xFF4A4858)
        letter == null && result == null -> Color(0xFF3F3F46)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(tileSize)
            .scale(bounceScale.value)
            .graphicsLayer { rotationX = flipRotation.value }
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (letter != null) {
            Text(
                text = letter.uppercase(),
                color = TileText,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}
