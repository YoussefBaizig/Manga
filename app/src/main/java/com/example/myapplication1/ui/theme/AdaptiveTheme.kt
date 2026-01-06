package com.example.myapplication1.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.example.myapplication1.sensors.LightCategory

/**
 * Thème adaptatif pour la lecture de manga
 * S'adapte automatiquement à la lumière ambiante
 */
enum class AdaptiveReadingTheme {
    /**
     * Mode nuit - Obscurité (chambre)
     * Fond noir/or foncé, réduction de la lumière bleue
     */
    NIGHT_MODE,
    
    /**
     * Mode normal - Lumière tamisée (intérieur)
     * Thème standard, luminosité réduite
     */
    NORMAL_MODE,
    
    /**
     * Mode contraste élevé - Lumière forte (extérieur)
     * Contraste élevé pour meilleure lisibilité, luminosité augmentée
     */
    HIGH_CONTRAST_MODE
}

/**
 * Détermine le thème adaptatif selon la catégorie de lumière
 */
fun getAdaptiveTheme(lightCategory: LightCategory): AdaptiveReadingTheme {
    return when (lightCategory) {
        LightCategory.DARK, LightCategory.VERY_DIM -> AdaptiveReadingTheme.NIGHT_MODE
        LightCategory.DIM, LightCategory.NORMAL -> AdaptiveReadingTheme.NORMAL_MODE
        LightCategory.BRIGHT, LightCategory.VERY_BRIGHT -> AdaptiveReadingTheme.HIGH_CONTRAST_MODE
    }
}

/**
 * Couleurs pour le mode nuit (fond noir/or foncé)
 */
private val NightModeColorScheme = darkColorScheme(
    // Primary colors - Or foncé pour le mode nuit
    primary = Color(0xFFD4A574), // Or foncé
    onPrimary = Color(0xFF0D0D0D),
    primaryContainer = Color(0xFF8B6914), // Or très foncé
    onPrimaryContainer = Color(0xFFFFF8E1),
    
    // Secondary colors
    secondary = Color(0xFFB8860B), // Dark goldenrod
    onSecondary = Color(0xFF0D0D0D),
    secondaryContainer = Color(0xFF6B5B2D),
    onSecondaryContainer = Color(0xFFFFF8E1),
    
    // Tertiary colors
    tertiary = Color(0xFFCD853F), // Peru (or foncé)
    onTertiary = Color(0xFF0D0D0D),
    tertiaryContainer = Color(0xFF8B6914),
    onTertiaryContainer = Color(0xFFFFF8E1),
    
    // Background & Surface - Noir profond
    background = Color(0xFF0A0A0A), // Noir très foncé
    onBackground = Color(0xFFD4A574), // Or foncé pour le texte
    surface = Color(0xFF1A1A1A), // Noir légèrement plus clair
    onSurface = Color(0xFFD4A574),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB8860B),
    
    // Other
    outline = Color(0xFF6B5B2D),
    outlineVariant = Color(0xFF3A3A3A),
    inverseSurface = Color(0xFFD4A574),
    inverseOnSurface = Color(0xFF0A0A0A),
    inversePrimary = Color(0xFF8B6914),
    
    // Error
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFFB00020),
    onErrorContainer = Color(0xFFFFDAD4),
    
    // Scrim
    scrim = Color.Black.copy(alpha = 0.8f)
)

/**
 * Couleurs pour le mode normal (lumière tamisée)
 */
private val NormalModeColorScheme = darkColorScheme(
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
    
    // Tertiary colors
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
 * Couleurs pour le mode contraste élevé (lumière forte)
 */
private val HighContrastColorScheme = darkColorScheme(
    // Primary colors - Couleurs vives pour contraste élevé
    primary = Color(0xFFFF4444), // Rouge vif
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCC0000), // Rouge foncé
    onPrimaryContainer = Color.White,
    
    // Secondary colors
    secondary = Color(0xFFFFAA00), // Orange vif
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCC8800),
    onSecondaryContainer = Color.White,
    
    // Tertiary colors
    tertiary = Color(0xFFFFFF00), // Jaune vif
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFCCCC00),
    onTertiaryContainer = Color.Black,
    
    // Background & Surface - Noir profond pour contraste maximum
    background = Color(0xFF000000), // Noir pur
    onBackground = Color(0xFFFFFFFF), // Blanc pur
    surface = Color(0xFF1A1A1A), // Noir légèrement plus clair
    onSurface = Color(0xFFFFFFFF), // Blanc pur
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFFFE0E0), // Blanc cassé
    
    // Other
    outline = Color(0xFFFFFFFF), // Blanc pour contraste
    outlineVariant = Color(0xFF4A4A4A),
    inverseSurface = Color(0xFFFFFFFF),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFFCC0000),
    
    // Error
    error = Color(0xFFFF0000), // Rouge vif
    onError = Color.White,
    errorContainer = Color(0xFF990000),
    onErrorContainer = Color.White,
    
    // Scrim
    scrim = Color.Black.copy(alpha = 0.9f)
)

/**
 * Obtient le ColorScheme selon le thème adaptatif
 */
fun getAdaptiveColorScheme(theme: AdaptiveReadingTheme): ColorScheme {
    return when (theme) {
        AdaptiveReadingTheme.NIGHT_MODE -> NightModeColorScheme
        AdaptiveReadingTheme.NORMAL_MODE -> NormalModeColorScheme
        AdaptiveReadingTheme.HIGH_CONTRAST_MODE -> HighContrastColorScheme
    }
}

/**
 * Obtient le niveau de luminosité recommandé selon le thème
 */
fun getRecommendedBrightnessForTheme(theme: AdaptiveReadingTheme): Float {
    return when (theme) {
        AdaptiveReadingTheme.NIGHT_MODE -> 0.15f // Très faible pour la nuit
        AdaptiveReadingTheme.NORMAL_MODE -> 0.5f // Modéré pour intérieur
        AdaptiveReadingTheme.HIGH_CONTRAST_MODE -> 0.9f // Élevé pour extérieur
    }
}

