package com.example.nush_hack_24.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

@Composable
fun YourAppTheme(content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = PrimaryColor,
        primaryVariant = PrimaryVariant,
        secondary = SecondaryColor,
        background = BackgroundColor,
        surface = SurfaceColor,
        error = ErrorColor,
        onPrimary = OnPrimary,
        onSecondary = OnSecondary,
        onBackground = OnBackground,
        onSurface = OnSurface,
        onError = OnError
    )

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}