package com.dailyrap.app.ui.screen.connections.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.ui.theme.BorderSubtle
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText
import java.util.Locale

@Composable
fun WordTile(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) TileSelected else TileDefault,
        animationSpec = tween(200),
        label = "tileBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(150),
        label = "tileScale"
    )

    val displayText = word.uppercase(Locale("pl"))
    // Detect multi-word: space or any whitespace
    val hasSpace = displayText.any { it.isWhitespace() }

    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, if (isSelected) TileSelected else BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 3.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasSpace) {
            // Multi-word: autosize to fit 2 lines
            var fontSize by remember(displayText) { mutableFloatStateOf(11f) }
            var readyToDraw by remember(displayText) { mutableStateOf(false) }

            Text(
                text = displayText,
                color = TileText,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                lineHeight = (fontSize + 2).sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                onTextLayout = { result ->
                    if (result.hasVisualOverflow && fontSize > 5f) {
                        fontSize *= 0.9f
                    } else if (!readyToDraw) {
                        readyToDraw = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp)
                    .drawWithContent { if (readyToDraw) drawContent() }
            )
        } else {
            // Single word: autosize to fit 1 line
            var fontSize by remember(displayText) { mutableFloatStateOf(14f) }
            var readyToDraw by remember(displayText) { mutableStateOf(false) }

            Text(
                text = displayText,
                color = TileText,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                onTextLayout = { result ->
                    if (result.hasVisualOverflow && fontSize > 5f) {
                        fontSize *= 0.9f
                    } else if (!readyToDraw) {
                        readyToDraw = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .drawWithContent { if (readyToDraw) drawContent() }
            )
        }
    }
}
