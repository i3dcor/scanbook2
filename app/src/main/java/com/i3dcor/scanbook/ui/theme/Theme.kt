package com.i3dcor.scanbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

enum class ThemeOption { DarkBlue, WarmEarthy }

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

val WarmEarthyColorScheme = darkColorScheme(
    primary          = WarmEarthyPrimary,
    secondary        = WarmEarthySecondary,
    tertiary         = WarmEarthyTertiary,
    error            = WarmEarthyError,
    background       = WarmEarthyBackground,
    surface          = WarmEarthySurface,
    surfaceVariant   = WarmEarthySurfaceVariant,
    outline          = WarmEarthyOutline,
    onPrimary        = WarmEarthyOnBackground,
    onBackground     = WarmEarthyOnBackground,
    onSurface        = WarmEarthyOnSurface,
    onSurfaceVariant = WarmEarthyOnSurfaceVariant,
)

@Composable
fun ScanBookTheme(
    theme: ThemeOption = ThemeOption.DarkBlue,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        ThemeOption.DarkBlue   -> DarkBlueColorScheme
        ThemeOption.WarmEarthy -> WarmEarthyColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
