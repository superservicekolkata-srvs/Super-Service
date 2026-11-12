package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SuperBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SuperBlueLight,
    onPrimaryContainer = SuperBlueDark,
    secondary = SuperIndigoSecondary,
    onSecondary = Color.White,
    tertiary = SuperAzureTertiary,
    onTertiary = Color.White,
    background = SuperBackgroundLight,
    onBackground = SuperTextPrimary,
    surface = SuperSurfaceLight,
    onSurface = SuperTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SuperTextSecondary,
    outline = SuperCardStroke
)

private val DarkColorScheme = darkColorScheme(
    primary = SuperBluePrimaryDark,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0F172A),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF0F172A),
    background = SuperBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = SuperSurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

@Composable
fun SuperServiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
