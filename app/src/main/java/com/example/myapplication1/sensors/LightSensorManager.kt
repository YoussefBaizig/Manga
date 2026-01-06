package com.example.myapplication1.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.abs
import kotlin.math.max

/**
 * Manages the ambient light sensor (photoelectric cell) for light measurement
 * 
 * The photoelectric cell uses photodiodes or phototransistors that utilize
 * the photoelectric effect. When light hits the depletion layer with sufficient
 * energy, it ionizes atoms and generates electron-hole pairs, creating a
 * photocurrent proportional to light intensity.
 * 
 * This sensor can be used for:
 * - Measuring ambient light levels
 * - Adjusting screen brightness
 * - Detecting day/night conditions
 * - Color analysis (if RGB sensor available)
 * - UV and infrared detection (on advanced sensors)
 */
class LightSensorManager(
    private val context: Context,
    private val onLightStateChanged: (LightSensorState) -> Unit
) : SensorEventListener {
    
    companion object {
        private const val TAG = "LightSensorManager"
        
        // Sensor update frequency
        private const val UPDATE_DELAY_MS = 100L // Update every 100ms (10Hz)
        
        // Light level thresholds (in lux)
        private const val DARK_THRESHOLD = 1f
        private const val DIM_THRESHOLD = 50f
        private const val BRIGHT_THRESHOLD = 200f
        private const val VERY_BRIGHT_THRESHOLD = 1000f
        private const val NIGHT_THRESHOLD = 5f
        
        // History size for averaging
        private const val LIGHT_HISTORY_SIZE = 10
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Primary light sensor
    private val lightSensor: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    
    // Try to find RGB light sensor (some devices have separate RGB sensors)
    private val rgbLightSensor: Sensor? = findRgbLightSensor()
    
    private var currentState = LightSensorState(
        isAvailable = lightSensor != null
    )
    
    private var isListening = false
    
    // Light history for averaging
    private val lightHistory = mutableListOf<Float>()
    
    // Handler for debouncing updates on main thread
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    /**
     * Find RGB light sensor if available
     */
    private fun findRgbLightSensor(): Sensor? {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensorList) {
            // Some devices have RGB light sensors
            if (sensor.name.contains("RGB", ignoreCase = true) ||
                sensor.name.contains("Color", ignoreCase = true) ||
                sensor.name.contains("Light RGB", ignoreCase = true)) {
                Log.d(TAG, "Found RGB light sensor: ${sensor.name}")
                return sensor
            }
        }
        return null
    }
    
    /**
     * Start listening to light sensor
     * 
     * @return true if sensor was successfully started, false otherwise
     */
    fun start(): Boolean {
        if (isListening) {
            Log.w(TAG, "Light sensor already listening")
            return true
        }
        
        if (lightSensor == null) {
            Log.w(TAG, "No light sensor available on this device")
            currentState = currentState.copy(
                isAvailable = false,
                isActive = false
            )
            onLightStateChanged(currentState)
            return false
        }
        
        val success = sensorManager.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL // ~10Hz updates
        )
        
        // Also register RGB sensor if available
        rgbLightSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        if (success) {
            isListening = true
            currentState = currentState.copy(
                isActive = true,
                accuracy = SensorAccuracy.MEDIUM
            )
            Log.d(TAG, "Light sensor started")
            return true
        } else {
            Log.e(TAG, "Failed to start light sensor")
            return false
        }
    }
    
    /**
     * Stop listening to light sensor
     */
    fun stop() {
        if (!isListening) return
        
        sensorManager.unregisterListener(this)
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        lightHistory.clear()
        isListening = false
        
        currentState = currentState.copy(
            isActive = false,
            lightLevel = 0f,
            averageLightLevel = 0f
        )
        
        Log.d(TAG, "Light sensor stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            Log.w(TAG, "Null sensor event received")
            return
        }
        
        if (event.values == null || event.values.isEmpty()) {
            Log.w(TAG, "Invalid sensor event: empty values")
            return
        }
        
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                updateLightData(event.values)
            }
            // Handle RGB sensor if it's a custom sensor type
            else -> {
                // Check if it's an RGB sensor by name
                if (event.sensor.name.contains("RGB", ignoreCase = true) ||
                    event.sensor.name.contains("Color", ignoreCase = true)) {
                    updateRgbData(event.values)
                }
            }
        }
        
        // Debounce updates
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = Runnable {
            computeDerivedValues()
            onLightStateChanged(currentState)
        }
        handler.postDelayed(updateRunnable!!, UPDATE_DELAY_MS)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val sensorAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w(TAG, "Light sensor accuracy: UNRELIABLE")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w(TAG, "Light sensor accuracy: LOW")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                Log.d(TAG, "Light sensor accuracy: MEDIUM")
                SensorAccuracy.MEDIUM
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                Log.d(TAG, "Light sensor accuracy: HIGH")
                SensorAccuracy.HIGH
            }
            else -> SensorAccuracy.UNKNOWN
        }
        
        currentState = currentState.copy(accuracy = sensorAccuracy)
    }
    
    private fun updateLightData(values: FloatArray) {
        if (values.isEmpty()) {
            Log.w(TAG, "Empty values array in updateLightData")
            return
        }
        
        val lightLevel = values[0] // Light level in lux
        
        // Validate reading (should be positive)
        if (lightLevel < 0f) {
            Log.w(TAG, "Invalid light reading: $lightLevel, skipping")
            return
        }
        
        Log.d(TAG, "Light reading: ${String.format("%.1f", lightLevel)} lux")
        
        // Add to history for averaging
        lightHistory.add(lightLevel)
        if (lightHistory.size > LIGHT_HISTORY_SIZE) {
            lightHistory.removeAt(0)
        }
        
        currentState = currentState.copy(
            lightLevel = lightLevel
        )
    }
    
    private fun updateRgbData(values: FloatArray) {
        if (values.size < 3) return
        
        // Some RGB sensors provide R, G, B values
        currentState = currentState.copy(
            redLight = values[0],
            greenLight = values[1],
            blueLight = values[2]
        )
    }
    
    private fun computeDerivedValues() {
        val state = currentState
        
        // Calculate average light level with outlier filtering (similar to distance logic)
        val averageLightLevel = if (lightHistory.size >= 3) {
            // Remove outliers (values that are too far from median)
            val sortedLight = lightHistory.sorted()
            val median = sortedLight[sortedLight.size / 2]
            val filteredLight = lightHistory.filter { 
                abs(it - median) < (median * 0.3f) // Within 30% of median
            }
            
            if (filteredLight.isNotEmpty()) {
                filteredLight.average().toFloat()
            } else {
                lightHistory.average().toFloat() // Fallback to simple average
            }
        } else if (lightHistory.isNotEmpty()) {
            lightHistory.average().toFloat()
        } else {
            state.lightLevel
        }
        
        Log.d(TAG, "Average light level: ${String.format("%.1f", averageLightLevel)} lux (from ${lightHistory.size} readings)")
        
        // Determine environmental conditions
        val isDark = state.lightLevel < DARK_THRESHOLD
        val isBright = state.lightLevel > BRIGHT_THRESHOLD
        val isNight = state.lightLevel < NIGHT_THRESHOLD
        
        // Determine dominant color (if RGB data available)
        val dominantColor = if (state.hasColorData) {
            val maxComponent = max(
                max(state.redLight, state.greenLight),
                state.blueLight
            )
            when {
                abs(state.redLight - maxComponent) < 0.1f -> ColorComponent.RED
                abs(state.greenLight - maxComponent) < 0.1f -> ColorComponent.GREEN
                abs(state.blueLight - maxComponent) < 0.1f -> ColorComponent.BLUE
                abs(state.redLight - state.greenLight) < 0.2f && 
                abs(state.greenLight - state.blueLight) < 0.2f -> ColorComponent.BALANCED
                else -> ColorComponent.UNKNOWN
            }
        } else {
            ColorComponent.UNKNOWN
        }
        
        // Calculate color temperature (simplified, in Kelvin)
        // This is a rough approximation
        val colorTemperature = if (state.hasColorData && 
            (state.redLight + state.greenLight + state.blueLight) > 0f) {
            val total = state.redLight + state.greenLight + state.blueLight
            val redRatio = state.redLight / total
            val blueRatio = state.blueLight / total
            
            // Warmer light has more red, cooler light has more blue
            // Rough approximation: 2000K (warm) to 10000K (cool)
            when {
                redRatio > 0.5f -> 2000f + (redRatio - 0.5f) * 2000f // 2000-4000K
                blueRatio > 0.4f -> 6000f + (blueRatio - 0.4f) * 4000f // 6000-10000K
                else -> 4000f + (blueRatio - redRatio) * 2000f // 4000-6000K
            }
        } else {
            0f
        }
        
        currentState = state.copy(
            averageLightLevel = averageLightLevel,
            lightHistory = lightHistory.toList(),
            isDark = isDark,
            isBright = isBright,
            isNight = isNight,
            dominantColor = dominantColor,
            colorTemperature = colorTemperature
        )
    }
    
    /**
     * Check if light sensor is available on this device
     * 
     * @return true if sensor is available, false otherwise
     */
    fun isAvailable(): Boolean {
        return lightSensor != null
    }
    
    /**
     * Check if sensor is currently active
     */
    fun isActive(): Boolean = isListening
    
    /**
     * Get current light level in lux
     */
    fun getCurrentLightLevel(): Float = currentState.lightLevel
    
    /**
     * Get recommended screen brightness (0.0 to 1.0)
     */
    fun getRecommendedBrightness(): Float = currentState.recommendedBrightness
}

