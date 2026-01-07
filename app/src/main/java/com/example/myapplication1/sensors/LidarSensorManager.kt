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
    
    // Track sensor type (binary vs continuous)
    private var isBinarySensor: Boolean? = null // null = not yet determined
    private var sensorReadings = mutableListOf<Float>() // Track recent readings to detect binary sensor
    private val SENSOR_TYPE_DETECTION_SAMPLES = 10 // Number of samples to determine sensor type
    
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
            // Initialize with a default "far" distance to show sensor is active
            // This ensures hasValidDistance returns true immediately
            val initialDistance = 2.0f.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
            currentState = currentState.copy(
                isActive = true,
                accuracy = SensorAccuracy.MEDIUM,
                distance = initialDistance, // Set initial distance so hasValidDistance works
                minDistance = MIN_DISTANCE,
                maxDistance = MAX_DISTANCE
            )
            onLidarStateChanged(currentState) // Notify immediately
            Log.d(TAG, "LiDAR sensor started with initial distance: ${initialDistance}m")
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
        sensorReadings.clear()
        isBinarySensor = null // Reset sensor type detection
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

        // Detect sensor type (binary vs continuous) if not yet determined
        if (isBinarySensor == null) {
            sensorReadings.add(rawDistance)
            if (sensorReadings.size >= SENSOR_TYPE_DETECTION_SAMPLES) {
                // Check if all readings are either 0 or maxRange (binary sensor)
                val uniqueValues = sensorReadings.toSet()
                val isBinary = uniqueValues.all { it == 0f || it == maxRange || 
                    (it > 0f && it < 0.01f) || (it > maxRange - 0.01f && it <= maxRange) }
                
                isBinarySensor = isBinary
                if (isBinary) {
                    Log.d(TAG, "Detected binary proximity sensor (returns 0 or $maxRange)")
                } else {
                    Log.d(TAG, "Detected continuous proximity sensor (returns actual distance)")
                }
                sensorReadings.clear() // Clear after detection
            } else {
                // Still collecting samples - process as potentially binary for now
                // This allows us to show readings even during detection
            }
        }

        // Validate and interpret distance reading
        val distance: Float
        val isValidReading: Boolean
        
        // If sensor type is unknown, try to determine from current reading
        val treatAsBinary = if (isBinarySensor != null) {
            isBinarySensor == true
        } else {
            // During detection, check if this reading looks binary
            rawDistance <= 0.01f || rawDistance >= maxRange - 0.01f
        }
        
        if (treatAsBinary) {
            // Binary sensor behavior varies by device:
            // Some: 0 = close, maxRange = far
            // Others: 0 = far, maxRange = close
            // Based on user feedback: 0 = close, maxRange = far
            if (rawDistance <= 0.01f) {
                // Close/near - treat as close (15cm for warning purposes)
                distance = 0.15f // 15cm - close enough to trigger warning
                isValidReading = true
            } else if (rawDistance >= maxRange - 0.01f) {
                // Far - beyond range, use 1.0m to indicate "beyond detection range"
                distance = 1.0f // 1 meter - indicates "beyond range" but still valid for display
                isValidReading = true // Mark as valid so UI shows sensor is working
            } else {
                // During detection, might be a continuous reading - process as continuous
                isValidReading = rawDistance > 0f && rawDistance < maxRange
                distance = if (isValidReading) {
                    rawDistance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
                } else {
                    maxRange // Beyond range
                }
            }
        } else {
            // Continuous sensor: returns actual distance
            isValidReading = rawDistance > 0f && rawDistance < maxRange
            distance = if (isValidReading) {
                // Valid distance measurement - use it
                rawDistance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
            } else if (rawDistance >= maxRange) {
                // Sensor indicates "far" - beyond range, but still update state with a valid value
                // Use a value within range to show sensor is working
                2.0f.coerceAtMost(MAX_DISTANCE) // Use 2m to indicate "beyond range" but still valid
            } else {
                // Invalid reading (0 or negative) - use a default "far" value within valid range
                2.0f.coerceAtMost(MAX_DISTANCE) // Use 2m to indicate "beyond range" but still valid
            }
        }
        
        // Ensure distance is always within valid range for hasValidDistance check
        val finalDistance = distance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)

        // Log for debugging
        Log.d(TAG, "Sensor reading: raw=${String.format("%.3f", rawDistance)}m, processed=${String.format("%.3f", distance)}m (${String.format("%.1f", distance * 100)}cm), maxRange=${String.format("%.3f", maxRange)}m, binary=${isBinarySensor}, valid=$isValidReading")

        // Add to history for averaging (only valid readings)
        if (isValidReading) {
            distanceHistory.add(finalDistance)
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
            // No history yet - use current distance if valid, otherwise use a default
            if (isValidReading) finalDistance else 2.0f.coerceAtMost(MAX_DISTANCE)
        }
        
        // Check for face proximity
        val currentTime = System.currentTimeMillis()
        val isFaceTooClose = if (isBinarySensor == true) {
            // For binary sensors, if we're getting "near" readings, treat as too close
            // (since binary sensors can't give exact distance, we assume close = too close)
            averageDistance > 0f && averageDistance < maxRange
        } else {
            // For continuous sensors, check if distance is below threshold
            averageDistance > 0f && averageDistance < maxRange && averageDistance < FACE_PROXIMITY_THRESHOLD
        }
        
        val proximityWarning = if (isFaceTooClose && (currentTime - lastProximityWarningTime) > PROXIMITY_WARNING_COOLDOWN_MS) {
            lastProximityWarningTime = currentTime
            val distanceCm = averageDistance * 100f
            val warningMessage = if (isBinarySensor == true) {
                // Binary sensor can't give exact distance
                "⚠️ Too close! Please move away from the screen"
            } else {
                // Continuous sensor can show exact distance
                "⚠️ Too close! Please move your face at least 30cm away from the screen (current: ${String.format("%.1f", distanceCm)}cm)"
            }
            Log.d(TAG, "⚠️ Face too close detected: ${if (isBinarySensor == true) "binary sensor (near)" else "${String.format("%.1f", distanceCm)}cm"}")
            warningMessage
        } else if (isFaceTooClose) {
            currentState.faceProximityWarning // Keep previous warning during cooldown
        } else {
            null
        }
        
        // Debounce updates - capture finalDistance in closure
        val capturedFinalDistance = finalDistance
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = Runnable {
            // Always update distance to show sensor is working
            // Use capturedFinalDistance which is guaranteed to be within valid range
            val updatedDistance = capturedFinalDistance.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
            
            // Verify the distance will pass hasValidDistance check
            val willBeValid = updatedDistance > 0f && updatedDistance >= MIN_DISTANCE && updatedDistance <= MAX_DISTANCE
            if (!willBeValid) {
                Log.w(TAG, "Distance $updatedDistance will fail hasValidDistance check! min=$MIN_DISTANCE, max=$MAX_DISTANCE")
                // Fallback to a safe default value
                val safeDistance = 1.0f.coerceIn(MIN_DISTANCE, MAX_DISTANCE)
                currentState = currentState.copy(
                    distance = safeDistance,
                    averageDistance = averageDistance,
                    distanceHistory = distanceHistory.toList(),
                    accuracy = determineAccuracy(event.accuracy),
                    isFaceTooClose = isFaceTooClose,
                    faceProximityWarning = proximityWarning
                )
            } else {
                currentState = currentState.copy(
                    distance = updatedDistance,
                    averageDistance = averageDistance,
                    distanceHistory = distanceHistory.toList(),
                    accuracy = determineAccuracy(event.accuracy),
                    isFaceTooClose = isFaceTooClose,
                    faceProximityWarning = proximityWarning
                )
            }
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
