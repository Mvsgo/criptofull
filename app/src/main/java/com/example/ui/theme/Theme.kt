package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = CryptoDarkBackground,
    primaryContainer = CryptoDarkSurfaceVariant,
    onPrimaryContainer = CyanNeon,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    tertiary = BitcoinGold,
    background = CryptoDarkBackground,
    surface = CryptoDarkSurface,
    surfaceVariant = CryptoDarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = CryptoCardBorder
)

private val LightColorScheme = darkColorScheme(
    // We default to a sleek dark crypto palette because crypto trading apps excel in dark mode
    primary = CyanNeon,
    onPrimary = CryptoDarkBackground,
    primaryContainer = CryptoDarkSurfaceVariant,
    onPrimaryContainer = CyanNeon,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    tertiary = BitcoinGold,
    background = CryptoDarkBackground,
    surface = CryptoDarkSurface,
    surfaceVariant = CryptoDarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = CryptoCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = CryptoDarkBackground.toArgb()
                window.navigationBarColor = CryptoDarkBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
