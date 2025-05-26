package com.giacomo.plantwateringtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreen, // Deep matte green as the primary color
    secondary = LightGreenGrey, // Light green-grey for accents
    tertiary = SoftBlueGrey, // Muted blue-grey for tertiary accents
    background = DarkGreen, // Dark green for background consistency
    surface = LightGreenGrey, // Softer contrast for surfaces
    onSurface = LightOffWhite, // Off-white text for readability
    onBackground = LightOffWhite // Ensuring text stands out on dark background
)

// Light color scheme using matte green and more neutral tones
private val LightColorScheme = lightColorScheme(
    primary = LightGreenGrey,
    secondary = LightOffWhite,
    tertiary = DarkGreen,
    background = LightGreenGrey, // Set background for light theme
    surface = Color.White, // Set surface for light theme
    onSurface = Color.Black,  // Black text color on surface
    onBackground = Color.Black // Black text color on background
)


@Composable
fun PlantWateringTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}