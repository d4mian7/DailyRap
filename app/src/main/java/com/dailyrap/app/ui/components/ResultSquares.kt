package com.dailyrap.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dailyrap.app.ui.theme.TileText

/**
 * Kafelek wyniku w oknie końca gry — zamiast systemowych emotek.
 * Gradient (jaśniej u góry), jaśniejsza ramka i połysk w górnej części,
 * żeby pasował do kafelków z planszy.
 */
@Composable
fun ResultSquare(
    color: Color,
    size: Dp = 28.dp,
    icon: ImageVector? = null
) {
    val shape = RoundedCornerShape(size * 0.26f)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(color, Color.White, 0.18f),
                        color,
                        lerp(color, Color.Black, 0.15f)
                    )
                )
            )
            .border(1.dp, lerp(color, Color.White, 0.4f).copy(alpha = 0.7f), shape),
        contentAlignment = Alignment.Center
    ) {
        // Połysk w górnej części kafelka
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TileText,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}

/** Siatka kafelków: każdy wiersz to lista kolorów. */
@Composable
fun ResultSquaresGrid(
    rows: List<List<Color>>,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { color -> ResultSquare(color = color, size = size) }
            }
        }
    }
}
