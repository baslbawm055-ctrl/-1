package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = TextWhite,
    primaryContainer = PrimaryColor.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryColor,
    secondary = AccentColor,
    onSecondary = TextWhite,
    secondaryContainer = AccentColor.copy(alpha = 0.1f),
    onSecondaryContainer = AccentColor,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = CardLight,
    onSurface = TextDark,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextGrayLight,
    outline = TextGrayLight.copy(alpha = 0.5f),
    outlineVariant = TextGrayLight.copy(alpha = 0.2f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = TextWhite,
    primaryContainer = PrimaryColor.copy(alpha = 0.2f),
    onPrimaryContainer = TextWhite,
    secondary = AccentColor,
    onSecondary = BackgroundDark,
    secondaryContainer = AccentColor.copy(alpha = 0.2f),
    onSecondaryContainer = AccentColor,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = CardDark,
    onSurface = TextWhite,
    surfaceVariant = BackgroundDark,
    onSurfaceVariant = TextGray,
    outline = TextGray.copy(alpha = 0.5f),
    outlineVariant = TextGray.copy(alpha = 0.2f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
