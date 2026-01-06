package com.example.myapplication1.sensors

import android.app.Activity
import android.util.Log
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Gestionnaire du filtre de réduction de lumière bleue (mode nuit)
 * 
 * Applique un filtre qui réduit la lumière bleue pour un confort visuel
 * amélioré en conditions de faible luminosité.
 */
class BlueLightFilter {
    
    companion object {
        private const val TAG = "BlueLightFilter"
        
        // Intensity levels (0.0 = no filter, 1.0 = maximum filter)
        private const val NIGHT_MODE_INTENSITY = 0.6f // 60% reduction
        private const val DIM_MODE_INTENSITY = 0.3f   // 30% reduction
        private const val NORMAL_MODE_INTENSITY = 0.0f // No filter
    }
    
    /**
     * Get filter intensity based on theme mode
     */
    fun getFilterIntensity(theme: com.example.myapplication1.ui.theme.AdaptiveReadingTheme): Float {
        return when (theme) {
            com.example.myapplication1.ui.theme.AdaptiveReadingTheme.NIGHT_MODE -> NIGHT_MODE_INTENSITY
            com.example.myapplication1.ui.theme.AdaptiveReadingTheme.NORMAL_MODE -> DIM_MODE_INTENSITY
            com.example.myapplication1.ui.theme.AdaptiveReadingTheme.HIGH_CONTRAST_MODE -> NORMAL_MODE_INTENSITY
        }
    }
    
    /**
     * Apply blue light filter to a window
     * Note: This is a placeholder for future implementation
     * On newer Android versions, blue light filtering is better handled
     * through system-level night mode or composable overlays
     */
    fun applyFilter(window: Window, intensity: Float) {
        try {
            // For now, we just log the intensity
            // In a full implementation, this would apply a color filter overlay
            if (intensity > 0f) {
                Log.d(TAG, "Blue light filter intensity: $intensity")
                // Future: Apply color filter overlay to window
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply blue light filter", e)
        }
    }
    
    /**
     * Remove blue light filter from a window
     */
    fun removeFilter(window: Window) {
        try {
            val view = window.decorView.rootView
            view.overlay.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove blue light filter", e)
        }
    }
}

/**
 * Composable effect to apply blue light filter based on theme
 */
@Composable
fun BlueLightFilterEffect(
    theme: com.example.myapplication1.ui.theme.AdaptiveReadingTheme,
    enabled: Boolean = true
) {
    val view = LocalView.current
    val filter = remember { BlueLightFilter() }
    
    DisposableEffect(theme, enabled) {
        val activity = if (view.context is Activity) view.context as Activity else null
        val window = activity?.window
        
        if (enabled && window != null) {
            val intensity = filter.getFilterIntensity(theme)
            if (intensity > 0f) {
                // Apply filter using window color filter
                // Note: On newer Android versions, we use a composable overlay instead
                // This is a simplified approach
                filter.applyFilter(window, intensity)
                Log.d("BlueLightFilter", "Applying blue light filter with intensity: $intensity")
            }
        }
        
        onDispose {
            if (enabled && window != null) {
                filter.removeFilter(window)
            }
        }
    }
}

