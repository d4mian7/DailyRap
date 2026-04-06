package com.dailyrap.app.ui.screen.crossword.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText

@Composable
fun SecretWordReveal(
    revealedLetters: List<Char?>,
    isFullyRevealed: Boolean,
    modifier: Modifier = Modifier
) {
    val bounceScale = remember { Animatable(1f) }

    LaunchedEffect(isFullyRevealed) {
        if (isFullyRevealed) {
            bounceScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 400
                    1.2f at 100
                    0.95f at 200
                    1.1f at 300
                    1f at 400
                }
            )
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ukryte hasło",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = if (isFullyRevealed) Modifier.scale(bounceScale.value) else Modifier
        ) {
            revealedLetters.forEach { letter ->
                val bgColor = when {
                    isFullyRevealed -> LetterPresent
                    letter != null -> TileSelected
                    else -> TileDefault
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (letter != null) {
                        Text(
                            text = letter.toString(),
                            color = if (isFullyRevealed) Color.Black else TileText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
