package com.example.myapplication1.sensors

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import android.os.Build
import kotlin.math.abs

/**
 * Manages automatic screen brightness adjustment based on ambient light sensor
 * 
 * This class adjusts the screen brightness automatically:
 * - If ambient light is too bright → reduces screen brightness
 * - If ambient light is too low → increases screen brightness
 */
class BrightnessManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BrightnessManager"
        private const val MIN_BRIGHTNESS = 10 // Minimum brightness (0-255)
        private const val MAX_BRIGHTNESS = 255 // Maximum brightness (0-255)
        private const val BRIGHTNESS_CHANGE_THRESHOLD = 0.05f // Only change if difference > 5%
    }
    
    private val contentResolver: ContentResolver = context.contentResolver
    private var isAutoBrightnessEnabled = false
    private var originalBrightness: Int? = null
    
    /**
     * Enable automatic brightness adjustment
     * 
     * @return true if successful, false if permission is required
     */
    fun enableAutoBrightness(): Boolean {
        return try {
            // Check if we can write settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    Log.w(TAG, "WRITE_SETTINGS permission not granted")
                    return false
                }
            }
            
            // Save original brightness
            originalBrightness = getCurrentBrightness()
            
            // Disable system auto-brightness to take control
            try {
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not set brightness mode to manual", e)
            }
            
            isAutoBrightnessEnabled = true
            Log.d(TAG, "Auto brightness enabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable auto brightness", e)
            false
        }
    }
    
    /**
     * Disable automatic brightness adjustment
     */
    fun disableAutoBrightness() {
        try {
            isAutoBrightnessEnabled = false
            
            // Restore original brightness if saved
            originalBrightness?.let {
                setBrightness(it)
            }
            
            Log.d(TAG, "Auto brightness disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable auto brightness", e)
        }
    }
    
    /**
     * Adjust brightness based on recommended brightness level
     * 
     * @param recommendedBrightness Recommended brightness (0.0 to 1.0)
     * @param windowManager Optional WindowManager for immediate effect
     */
    fun adjustBrightness(recommendedBrightness: Float, windowManager: WindowManager? = null) {
        if (!isAutoBrightnessEnabled) {
            Log.d(TAG, "Auto brightness not enabled, skipping adjustment")
            return
        }
        
        try {
            // Convert 0.0-1.0 to 0-255 range
            val targetBrightness = (recommendedBrightness * (MAX_BRIGHTNESS - MIN_BRIGHTNESS) + MIN_BRIGHTNESS).toInt()
                .coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
            
            val currentBrightness = getCurrentBrightness()
            val brightnessDiff = abs((targetBrightness - currentBrightness) / 255f)
            
            Log.d(TAG, "Brightness adjustment: recommended=${(recommendedBrightness * 100).toInt()}%, target=$targetBrightness, current=$currentBrightness, diff=${(brightnessDiff * 100).toInt()}%")
            
            // Only adjust if the difference is significant to avoid constant changes
            if (brightnessDiff > BRIGHTNESS_CHANGE_THRESHOLD) {
                if (windowManager != null) {
                    setBrightnessImmediate(targetBrightness, windowManager)
                } else {
                    setBrightness(targetBrightness)
                }
                Log.d(TAG, "✅ Brightness adjusted: $currentBrightness -> $targetBrightness (recommended: ${(recommendedBrightness * 100).toInt()}%)")
            } else {
                Log.d(TAG, "⏭️ Brightness change skipped: difference too small (${(brightnessDiff * 100).toInt()}% < ${(BRIGHTNESS_CHANGE_THRESHOLD * 100).toInt()}%)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to adjust brightness", e)
        }
    }
    
    /**
     * Set screen brightness
     * 
     * @param brightness Brightness value (0-255)
     */
    private fun setBrightness(brightness: Int) {
        try {
            val clampedBrightness = brightness.coerceIn(0, 255)
            
            // Method 1: Use Settings.System (persistent, requires WRITE_SETTINGS permission)
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                clampedBrightness
            )
            
            // Method 2: Use WindowManager for immediate effect (requires activity context)
            // This will be called from the activity if available
            Log.d(TAG, "Brightness set via Settings: $clampedBrightness")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set brightness via Settings", e)
        }
    }
    
    /**
     * Set brightness using WindowManager for immediate effect
     * This should be called from an Activity context
     * 
     * @param brightness Brightness value (0-255)
     * @param windowManager WindowManager from activity
     */
    fun setBrightnessImmediate(brightness: Int, windowManager: WindowManager?) {
        try {
            val clampedBrightness = brightness.coerceIn(0, 255)
            val brightnessPercent = clampedBrightness / 255f
            
            // Method 1: Try WindowManager if Activity context is available
            val activity = context as? android.app.Activity
            if (activity != null) {
                try {
                    val window = activity.window
                    val layoutParams = window.attributes
                    layoutParams.screenBrightness = brightnessPercent
                    window.attributes = layoutParams
                    Log.d(TAG, "✅ Brightness set via WindowManager: $clampedBrightness ($brightnessPercent)")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to set brightness via WindowManager: ${e.message}")
                }
            } else {
                Log.w(TAG, "Context is not an Activity, cannot use WindowManager")
            }
            
            // Method 2: Also update system settings for persistence (if permission available)
            if (canWriteSettings()) {
                setBrightness(clampedBrightness)
            } else {
                Log.w(TAG, "WRITE_SETTINGS permission not granted, brightness change may not persist")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to set brightness: ${e.message}", e)
        }
    }
    
    /**
     * Get current screen brightness
     * 
     * @return Current brightness (0-255) or null if unavailable
     */
    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128 // Default to middle brightness
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get brightness", e)
            128
        }
    }
    
    /**
     * Check if auto brightness is enabled
     */
    fun isEnabled(): Boolean = isAutoBrightnessEnabled
    
    /**
     * Check if WRITE_SETTINGS permission is available
     */
    fun canWriteSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true // Permission not required on older Android versions
        }
    }
    
    /**
     * Open system settings to grant WRITE_SETTINGS permission
     * This will open the app's settings page where user can grant the permission
     */
    fun openSettingsToGrantPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened settings to grant WRITE_SETTINGS permission")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
            // Fallback: open general app settings
            try {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open app settings", e2)
            }
        }
    }
}

