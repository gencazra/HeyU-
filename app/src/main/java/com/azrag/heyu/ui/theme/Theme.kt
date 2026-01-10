package com.azrag.heyu.ui.theme

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
    primary = HeyU_Dark_Primary,
    onPrimary = HeyU_Dark_Background,
    background = HeyU_Dark_Background,
    surface = HeyU_Dark_Surface,
    onBackground = HeyU_Dark_OnBackground,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = HeyU_Light_Primary,
    onPrimary = Color.White,
    background = HeyU_Light_Background,
    surface = HeyU_Light_Surface,
    onBackground = HeyU_Light_OnBackground,
    onSurface = HeyU_Light_Primary
)

@Composable
fun HeyUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
