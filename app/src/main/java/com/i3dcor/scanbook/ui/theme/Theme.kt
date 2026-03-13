package com.i3dcor.scanbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val DarkBlueColorScheme = darkColorScheme(
    primary          = DarkBluePrimary,
    secondary        = DarkBlueSecondary,
    tertiary         = DarkBlueTertiary,
    error            = DarkBlueError,
    background       = DarkBlueBackground,
    surface          = DarkBlueSurface,
    surfaceVariant   = DarkBlueSurfaceVariant,
    outline          = DarkBlueOutline,
    onPrimary        = DarkBlueOnBackground,
    onBackground     = DarkBlueOnBackground,
    onSurface        = DarkBlueOnSurface,
    onSurfaceVariant = DarkBlueOnSurfaceVariant,
    surfaceTint      = DarkBlueSurfaceTint,
)

@Composable
fun ScanBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkBlueColorScheme,
        typography = Typography,
        content = content
    )
}
