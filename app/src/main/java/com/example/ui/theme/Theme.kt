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
    primary = NeonGold,
    onPrimary = Color(0xFF281900),
    primaryContainer = Color(0xFF5A3D00),
    onPrimaryContainer = Color(0xFFFFDFA0),
    secondary = CyberPurpleLight,
    onSecondary = Color(0xFF24005A),
    secondaryContainer = Color(0xFF452277),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = NeonCyan,
    onTertiary = Color(0xFF003828),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7A4F00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDFA0),
    onPrimaryContainer = Color(0xFF281900),
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF22005D),
    tertiary = Color(0xFF006C4C),
    onTertiary = Color.White,
    background = Color(0xFFFAF8FD),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our dedicated gaming theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // MiniWar dark gamer UI is best experience in both modes!
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
