package com.example.ui.theme

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
    primary = CyberCyan,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = CyanLight,
    secondary = NeonBlue,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D63),
    onSecondaryContainer = Color(0xFFBBE9FF),
    tertiary = ElectricIndigo,
    onTertiary = Color.White,
    background = TechDarkBackground,
    onBackground = TextPrimary,
    surface = TechDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = TechDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TechDarkBorder,
    error = SignalRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF5E35B1),
    onTertiary = Color.White,
    background = TechLightBackground,
    onBackground = TextLightPrimary,
    surface = TechLightSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = TechLightSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = TechLightBorder,
    error = SignalRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek tech dark
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
