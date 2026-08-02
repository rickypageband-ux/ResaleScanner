package com.resalescanner.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF2CE),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF4C6359),
    background = Color(0xFFF7FBF7),
    surface = Color(0xFFF7FBF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D5B3),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF00513B),
    secondary = Color(0xFFB3CCC0),
)

@Composable
fun ResaleScannerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}

