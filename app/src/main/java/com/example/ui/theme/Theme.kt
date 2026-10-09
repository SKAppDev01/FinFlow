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
    primary = PrimaryEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = PrimaryEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = PrimaryEmerald,
    onSecondary = Color.White,
    tertiary = ExpenseCoral,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = PrimaryEmeraldDark,
    secondary = PrimaryEmeraldLight,
    onSecondary = Color.Black,
    tertiary = ExpenseCoral,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder
)

@Composable
fun FinFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our intentional custom fintech branding
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
