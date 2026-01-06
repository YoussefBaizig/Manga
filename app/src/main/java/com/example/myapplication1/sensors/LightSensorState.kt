package com.example.myapplication1.sensors

/**
 * Represents the current state of the ambient light sensor (photoelectric cell)
 * 
 * The photoelectric cell in smartphones measures ambient light using photodiodes
 * or phototransistors that utilize the photoelectric effect. When light hits the
 * depletion layer with sufficient energy, it ionizes atoms and generates
 * electron-hole pairs, creating a photocurrent proportional to light intensity.
 */
data class LightSensorState(
    // Primary light measurement
    val lightLevel: Float = 0f,              // Light intensity in lux (lx)
    
    // Color component measurements (if available)
    val redLight: Float = 0f,                // Red light component
    val greenLight: Float = 0f,              // Green light component
    val blueLight: Float = 0f,               // Blue light component
    
    // Advanced measurements (if available)
    val uvIndex: Float = 0f,                 // UV index (0-11+)
    val infraredLevel: Float = 0f,           // Infrared light level
    
    // Sensor status
    val isAvailable: Boolean = false,         // Whether light sensor is available
    val isActive: Boolean = false,           // Whether sensor is currently active
    val accuracy: SensorAccuracy = SensorAccuracy.UNKNOWN,
    
    // Computed values
    val averageLightLevel: Float = 0f,       // Average light over recent readings
    val lightHistory: List<Float> = emptyList(), // Recent light readings
    
    // Environmental conditions
    val isDark: Boolean = false,             // True if light level indicates darkness
    val isBright: Boolean = false,           // True if light level indicates brightness
    val isNight: Boolean = false,            // True if conditions suggest nighttime
    
    // Color analysis
    val dominantColor: ColorComponent = ColorComponent.UNKNOWN, // Dominant color detected
    val colorTemperature: Float = 0f         // Color temperature in Kelvin (if calculable)
) {
    /**
     * Get light level category
     */
    val lightCategory: LightCategory
        get() = when {
            lightLevel < 1f -> LightCategory.DARK
            lightLevel < 10f -> LightCategory.VERY_DIM
            lightLevel < 50f -> LightCategory.DIM
            lightLevel < 200f -> LightCategory.NORMAL
            lightLevel < 1000f -> LightCategory.BRIGHT
            else -> LightCategory.VERY_BRIGHT
        }
    
    /**
     * Get recommended screen brightness (0.0 to 1.0)
     */
    val recommendedBrightness: Float
        get() = when (lightCategory) {
            LightCategory.DARK -> 0.1f
            LightCategory.VERY_DIM -> 0.2f
            LightCategory.DIM -> 0.4f
            LightCategory.NORMAL -> 0.6f
            LightCategory.BRIGHT -> 0.8f
            LightCategory.VERY_BRIGHT -> 1.0f
        }
    
    /**
     * Check if RGB color detection is available
     */
    val hasColorData: Boolean
        get() = redLight > 0f || greenLight > 0f || blueLight > 0f
}

/**
 * Light level categories
 */
enum class LightCategory {
    DARK,           // < 1 lux (very dark, night)
    VERY_DIM,       // 1-10 lux (dim indoor)
    DIM,            // 10-50 lux (normal indoor)
    NORMAL,         // 50-200 lux (well-lit indoor)
    BRIGHT,         // 200-1000 lux (bright indoor/outdoor shade)
    VERY_BRIGHT     // > 1000 lux (direct sunlight)
}

/**
 * Dominant color component detected
 */
enum class ColorComponent {
    RED,
    GREEN,
    BLUE,
    BALANCED,       // Balanced RGB
    UNKNOWN
}

