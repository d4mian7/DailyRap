package com.dailyrap.app.ui.screen.connections.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WordGrid(
    words: List<String>,
    selectedWords: List<String>,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = words.chunked(4)
        rows.forEach { rowWords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEach { word ->
                    key(word) {
                        var appeared by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { appeared = true }
                        val offsetY by animateDpAsState(
                            targetValue = if (appeared) 0.dp else 20.dp,
                            animationSpec = tween(300),
                            label = "tileOffset"
                        )

                        WordTile(
                            word = word,
                            isSelected = word in selectedWords,
                            onClick = { onWordClick(word) },
                            modifier = Modifier
                                .weight(1f)
                                .offset(y = offsetY)
                        )
                    }
                }
                repeat(4 - rowWords.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
