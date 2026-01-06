package com.example.myapplication1.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Manages LiDAR/ToF (Time of Flight) sensor for distance measurement
 * 
 * On Android, LiDAR functionality is typically provided through:
 * 1. ToF (Time of Flight) sensor - TYPE_PROXIMITY with special capabilities
 * 2. Depth sensor - Camera2 API depth map
 * 3. Some devices have dedicated ToF sensors
 * 
 * This manager attempts to use the best available option on the device.
 */
class LidarSensorManager(
    private val context: Context,
    private val onLidarStateChanged: (LidarSensorState) -> Unit
) : SensorEventListener {
    
    companion object {
        private const val TAG = "LidarSensorManager"
        
        // Sensor update frequency
        private const val UPDATE_DELAY_MS = 100L // Update every 100ms (10Hz)
        
        // Distance thresholds (in meters)
        private const val MIN_DISTANCE = 0.01f    // 1 cm
        private const val MAX_DISTANCE = 5.0f     // 5 meters (typical ToF range)
        
        // History size for averaging (increased for better precision)
        private const val DISTANCE_HISTORY_SIZE = 20
        
        // Face proximity threshold (in meters)
        private const val FACE_PROXIMITY_THRESHOLD = 0.30f // 30 cm - too close for reading
        
        private const val PROXIMITY_WARNING_COOLDOWN_MS = 3000L // 3 seconds between warnings
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val cameraManager: CameraManager? = 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } else {
            null
        }
    
    // Try to find ToF/Proximity sensor with distance measurement capability
    private val tofSensor: Sensor? = findToFSensor()
    
    private var currentState = LidarSensorState(
        isAvailable = tofSensor != null,
        minDistance = MIN_DISTANCE,
        maxDistance = MAX_DISTANCE
    )
    
    private var isListening = false
    
    // Distance history for averaging
    private val distanceHistory = mutableListOf<Float>()
    
    // Face proximity detection
    private var lastProximityWarningTime = 0L
    
    // Handler for debouncing updates on main thread
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    /**
     * Find the best available ToF sensor
     * 
     * Note: Android proximity sensors can behave differently:
     * - Some return 0 for "far" and maxRange for "near" (binary)
     * - Others return actual distance in meters (continuous)
     * - We'll detect the type and adapt accordingly
     */
    private fun findToFSensor(): Sensor? {
        // Try to find a proximity sensor that can measure distance
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        
        proximitySensor?.let {
            val maxRange = it.maximumRange
            Log.d(TAG, "Found proximity sensor: name=${it.name}, maxRange=$maxRange m, vendor=${it.vendor}")
            
            // Accept any proximity sensor (we'll handle binary vs continuous in onSensorChanged)
            if (maxRange > 0f) {
                Log.d(TAG, "Using proximity sensor with max range: $maxRange m")
                return it
            } else {
                Log.w(TAG, "Proximity sensor has invalid maxRange: $maxRange")
            }
        }
        
        // Try to find other distance sensors
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensorList) {
            // Some devices have custom ToF sensors
            if (sensor.name.contains("ToF", ignoreCase = true) ||
                sensor.name.contains("Time of Flight", ignoreCase = true) ||
                sensor.name.contains("Distance", ignoreCase = true) ||
                sensor.name.contains("Proximity", ignoreCase = true)) {
                Log.d(TAG, "Found custom distance sensor: ${sensor.name}, maxRange=${sensor.maximumRange} m")
                return sensor
            }
        }
        
        Log.w(TAG, "No ToF/proximity sensor found on this device")
        return null
    }
    
    /**
     * Check if device has depth camera support (Camera2 API)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hasDepthCamera(): Boolean {
        return try {
            cameraManager?.let { manager ->
                val cameraIds = manager.cameraIdList
                for (cameraId in cameraIds) {
                    val characteristics = manager.getCameraCharacteristics(cameraId)
                    val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
                    )
                    
                    // Check if camera supports depth output
                    if (capabilities != null && 
                        capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT)) {
                        Log.d(TAG, "Found depth camera: $cameraId")
                        return true
                    }
                }
            }
            false
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error accessing camera", e)
            false
        }
    }
    
    /**
     * Start listening to LiDAR/ToF sensor
     * 
     * @return true if sensor was successfully started, false otherwise
     */
    fun start(): Boolean {
        if (isListening) {
            Log.w(TAG, "LiDAR sensor already listening")
            return true
        }
        
        if (tofSensor == null) {
            Log.w(TAG, "No ToF sensor available on this device")
            currentState = currentState.copy(
                isAvailable = false,
                isActive = false
            )
            onLidarStateChanged(currentState)
            return false
        }
        
        val success = sensorManager.registerListener(
            this,
            tofSensor,
            SensorManager.SENSOR_DELAY_NORMAL // ~10Hz updates
        )
        
        if (success) {
            isListening = true
            currentState = currentState.copy(
                isActive = true,
                accuracy = SensorAccuracy.MEDIUM
            )
            Log.d(TAG, "LiDAR sensor started")
            return true
        } else {
            Log.e(TAG, "Failed to start LiDAR sensor")
            return false
        }
    }
    
    /**
     * Stop listening to LiDAR/ToF sensor
     */
    fun stop() {
        if (!isListening) return
        
        sensorManager.unregisterListener(this)
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        distanceHistory.clear()
        isListening = false
        
        currentState = currentState.copy(
            isActive = false,
            distance = 0f,
            averageDistance = 0f
        )
        
        Log.d(TAG, "LiDAR sensor stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        if (event.values == null || event.values.isEmpty()) {
            Log.w(TAG, "Invalid sensor event: empty values")
            return
        }

        // Proximity/ToF sensor typically returns distance in event.values[0]
        val rawDistance = event.values[0]
        val maxRange = event.sensor.maximumRange

        // Validate and interpret distance reading
        // Some sensors return 0 for "far" and maxRange for "near"
        // Others return actual distance in meters
        val isValidReading = rawDistance > 0f && rawDistance < maxRange
        val distance = if (isValidReading) {
            // Valid distance measurement - use it
            rawDistance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
        } else if (rawDistance >= maxRange) {
            // Sensor indicates "far" - beyond range
            maxRange
        } else {
            // Invalid reading (0 or negative) - skip this reading
            Log.d(TAG, "Invalid distance reading: $rawDistance (maxRange=$maxRange), skipping")
            return
        }

        // Log for debugging (only valid readings)
        if (isValidReading) {
            Log.d(TAG, "Distance reading: ${String.format("%.3f", distance)}m (${String.format("%.1f", distance * 100)}cm), maxRange=${String.format("%.3f", maxRange)}m")
        }

        // Add to history for averaging (only valid readings)
        if (isValidReading) {
            distanceHistory.add(distance)
            if (distanceHistory.size > DISTANCE_HISTORY_SIZE) {
                distanceHistory.removeAt(0)
            }
        }
        
        // Calculate average distance with outlier filtering (similar to brightness logic)
        val averageDistance = if (distanceHistory.size >= 3) {
            // Remove outliers (values that are too far from median)
            val sortedDistances = distanceHistory.sorted()
            val median = sortedDistances[sortedDistances.size / 2]
            val filteredDistances = distanceHistory.filter { 
                abs(it - median) < (median * 0.3f) // Within 30% of median
            }
            
            if (filteredDistances.isNotEmpty()) {
                filteredDistances.average().toFloat()
            } else {
                distanceHistory.average().toFloat() // Fallback to simple average
            }
        } else if (distanceHistory.isNotEmpty()) {
            // Not enough readings yet, use simple average
            distanceHistory.average().toFloat()
        } else {
            // No history yet
            if (isValidReading) distance else 0f
        }
        
        // Check for face proximity (only if we have valid average distance)
        val currentTime = System.currentTimeMillis()
        val isFaceTooClose = if (averageDistance > 0f && averageDistance < maxRange && averageDistance < FACE_PROXIMITY_THRESHOLD) {
            // Valid distance measurement and too close
            true
        } else {
            false
        }
        
        val proximityWarning = if (isFaceTooClose && (currentTime - lastProximityWarningTime) > PROXIMITY_WARNING_COOLDOWN_MS) {
            lastProximityWarningTime = currentTime
            val distanceCm = averageDistance * 100f
            Log.d(TAG, "⚠️ Face too close detected: ${String.format("%.1f", distanceCm)}cm")
            "⚠️ Too close! Please move your face at least 30cm away from the screen (current: ${String.format("%.1f", distanceCm)}cm)"
        } else if (isFaceTooClose) {
            currentState.faceProximityWarning // Keep previous warning during cooldown
        } else {
            null
        }
        
        // Debounce updates
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = Runnable {
            currentState = currentState.copy(
                distance = if (isValidReading) distance else currentState.distance,
                averageDistance = averageDistance,
                distanceHistory = distanceHistory.toList(),
                accuracy = determineAccuracy(event.accuracy),
                isFaceTooClose = isFaceTooClose,
                faceProximityWarning = proximityWarning
            )
            onLidarStateChanged(currentState)
        }
        handler.postDelayed(updateRunnable!!, UPDATE_DELAY_MS)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val sensorAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w(TAG, "LiDAR sensor accuracy: UNRELIABLE")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w(TAG, "LiDAR sensor accuracy: LOW")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                Log.d(TAG, "LiDAR sensor accuracy: MEDIUM")
                SensorAccuracy.MEDIUM
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                Log.d(TAG, "LiDAR sensor accuracy: HIGH")
                SensorAccuracy.HIGH
            }
            else -> SensorAccuracy.UNKNOWN
        }
        
        currentState = currentState.copy(accuracy = sensorAccuracy)
    }
    
    private fun determineAccuracy(accuracy: Int): SensorAccuracy {
        return when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.LOW
            else -> SensorAccuracy.UNKNOWN
        }
    }
    
    /**
     * Check if LiDAR/ToF sensor is available on this device
     * 
     * @return true if sensor is available, false otherwise
     */
    fun isAvailable(): Boolean {
        return tofSensor != null
    }
    
    /**
     * Check if sensor is currently active
     */
    fun isActive(): Boolean = isListening
    
    /**
     * Get current distance measurement
     */
    fun getCurrentDistance(): Float = currentState.distance
    
    /**
     * Get average distance over recent readings
     */
    fun getAverageDistance(): Float = currentState.averageDistance
}
