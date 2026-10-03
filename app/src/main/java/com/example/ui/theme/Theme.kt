package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppLightGreen,
    onPrimary = WhatsAppDark,
    primaryContainer = WhatsAppDarkGreen,
    onPrimaryContainer = WhatsAppLightGreen,
    secondary = WhatsAppTealGreen,
    onSecondary = WhatsAppDark,
    background = WhatsAppDark,
    surface = Color(0xFF1F2C34),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A3942),
    onSurfaceVariant = Color(0xFF8696A0),
    outline = WhatsAppSubtext
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppDarkGreen,
    onPrimary = WhatsAppSurface,
    primaryContainer = WhatsAppLightGreen,
    onPrimaryContainer = WhatsAppDark,
    secondary = WhatsAppTealGreen,
    onSecondary = WhatsAppSurface,
    background = WhatsAppBackground,
    surface = WhatsAppSurface,
    onBackground = WhatsAppDark,
    onSurface = WhatsAppDark,
    outline = WhatsAppBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded WhatsApp green theme consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = WhatsAppDarkGreen.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
