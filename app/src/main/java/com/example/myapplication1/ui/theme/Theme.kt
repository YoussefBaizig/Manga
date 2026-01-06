package com.example.myapplication1.ui.theme

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

/**
 * Dark color scheme - Primary theme for manga reading
 */
private val MangaDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonDark,
    onPrimaryContainer = Color.White,
    
    // Secondary colors
    secondary = SakuraPink,
    onSecondary = InkBlack,
    secondaryContainer = SakuraPinkDark,
    onSecondaryContainer = Color.White,
    
    // Tertiary colors (accent)
    tertiary = CyberGold,
    onTertiary = InkBlack,
    tertiaryContainer = CyberGoldDark,
    onTertiaryContainer = InkBlack,
    
    // Background & Surface
    background = InkBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    
    // Other
    outline = TextMuted,
    outlineVariant = SurfaceElevated,
    inverseSurface = TextPrimary,
    inverseOnSurface = InkBlack,
    inversePrimary = CrimsonDark,
    
    // Error
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color.White,
    
    // Scrim
    scrim = Color.Black.copy(alpha = 0.6f)
)

/**
 * Light color scheme - Alternative for daytime reading
 */
private val MangaLightColorScheme = lightColorScheme(
    // Primary colors
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonLight,
    onPrimaryContainer = CrimsonDark,
    
    // Secondary colors
    secondary = SakuraPinkDark,
    onSecondary = Color.White,
    secondaryContainer = SakuraPink,
    onSecondaryContainer = InkBlack,
    
    // Tertiary colors
    tertiary = CyberGoldDark,
    onTertiary = InkBlack,
    tertiaryContainer = CyberGold,
    onTertiaryContainer = InkBlack,
    
    // Background & Surface
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    
    // Other
    outline = LightTextSecondary,
    outlineVariant = LightSurfaceVariant,
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightSurface,
    inversePrimary = CrimsonLight,
    
    // Error
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun MangaAppTheme(
    darkTheme: Boolean = true, // Default to dark theme for manga reading
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MangaDarkColorScheme else MangaLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MangaTypography,
        content = content
    )
}

// Keep the old theme name for compatibility during migration
@Composable
fun MyApplication1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MangaAppTheme(darkTheme = true, content = content)
}
