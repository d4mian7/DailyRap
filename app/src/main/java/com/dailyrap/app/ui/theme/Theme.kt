package com.dailyrap.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    secondary = TileSelected,
    tertiary = LetterCorrect,
    background = BackgroundDark,
    surface = TileDefault,
    onPrimary = TileText,
    onSecondary = TileText,
    onTertiary = TileText,
    onBackground = TileText,
    onSurface = TileText
)

@Composable
fun DailyRapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
