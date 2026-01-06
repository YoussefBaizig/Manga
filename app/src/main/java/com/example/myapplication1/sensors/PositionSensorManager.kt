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
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Gestionnaire des capteurs de position
 * 
 * Détecte :
 * - L'orientation de l'appareil (portrait/paysage)
 * - La position de l'appareil (vertical/horizontal/incliné)
 * - L'angle d'inclinaison
 * - La rotation automatique recommandée
 */
class PositionSensorManager(
    private val context: Context,
    private val onPositionStateChanged: (PositionSensorState) -> Unit
) : SensorEventListener {
    
    companion object {
        private const val TAG = "PositionSensorManager"
        
        // Sensor update frequency
        private const val UPDATE_DELAY_MS = 100L // Update every 100ms (10Hz)
        
        // Angle thresholds (in degrees)
        private const val PORTRAIT_THRESHOLD = 45f // Device is portrait if angle < 45°
        private const val LANDSCAPE_THRESHOLD = 45f // Device is landscape if angle > 45°
        private const val FLAT_THRESHOLD = 15f // Device is flat if angle < 15°
        
        // History size for smoothing
        private const val ANGLE_HISTORY_SIZE = 5
        
        // Flick detection thresholds
        private const val FLICK_SPEED_THRESHOLD = 150f // degrees per second to detect flick (high threshold to avoid false positives)
        private const val FLICK_COOLDOWN_MS = 5000L // Minimum time between flicks (5 seconds)
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val magnetometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    private var currentState = PositionSensorState()
    private var isListening = false
    
    // For orientation calculation
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    
    // Track if we have valid readings
    private var hasValidAccelerometerReading = false
    private var hasValidMagnetometerReading = false
    
    // Angle history for smoothing
    private val pitchHistory = mutableListOf<Float>()
    private val rollHistory = mutableListOf<Float>()
    
    // Flick detection
    private val angleVelocityHistory = mutableListOf<Pair<Float, Float>>() // (pitch velocity, roll velocity)
    private var lastFlickTime = 0L
    private var lastPitch = 0f
    private var lastRoll = 0f
    private var lastUpdateTime = 0L
    
    // Handler for debouncing updates on main thread
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    /**
     * Start listening to position sensors
     * 
     * @return true if sensors were successfully started, false otherwise
     */
    fun start(): Boolean {
        if (isListening) {
            Log.w(TAG, "Position sensors already listening")
            return true
        }
        
        // Reset state
        hasValidAccelerometerReading = false
        hasValidMagnetometerReading = false
        accelerometerReading.fill(0f)
        magnetometerReading.fill(0f)
        pitchHistory.clear()
        rollHistory.clear()
        angleVelocityHistory.clear()
        lastFlickTime = 0L
        lastPitch = 0f
        lastRoll = 0f
        lastUpdateTime = 0L
        
        var started = false
        
        accelerometer?.let {
            val success = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL // ~10Hz updates
            )
            if (success) {
                started = true
                Log.d(TAG, "Accelerometer started for position detection")
            } else {
                Log.e(TAG, "Failed to register accelerometer listener")
            }
        } ?: Log.w(TAG, "Accelerometer not available")
        
        magnetometer?.let {
            val success = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (success) {
                Log.d(TAG, "Magnetometer started for position detection")
            } else {
                Log.e(TAG, "Failed to register magnetometer listener")
            }
        } ?: Log.w(TAG, "Magnetometer not available (orientation may be less accurate)")
        
        isListening = started
        
        // Initialize state with available status
        if (started) {
            currentState = currentState.copy(
                isAvailable = true,
                isActive = true
            )
            onPositionStateChanged(currentState)
        }
        
        return started
    }
    
    /**
     * Stop listening to position sensors
     */
    fun stop() {
        if (!isListening) return
        
        sensorManager.unregisterListener(this)
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        pitchHistory.clear()
        rollHistory.clear()
        hasValidAccelerometerReading = false
        hasValidMagnetometerReading = false
        accelerometerReading.fill(0f)
        magnetometerReading.fill(0f)
        isListening = false
        
        // Reset state
        currentState = currentState.copy(
            isActive = false,
            azimuth = 0f,
            pitch = 0f,
            roll = 0f,
            smoothedPitch = 0f,
            smoothedRoll = 0f
        )
        
        Log.d(TAG, "Position sensors stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values == null || event.values.isEmpty()) {
            Log.w(TAG, "Invalid sensor event received")
            return
        }
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (event.values.size >= 3) {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, 3)
                    
                    // Check if values are valid (not all zeros and within reasonable range)
                    val magnitude = sqrt(
                        (accelerometerReading[0] * accelerometerReading[0] +
                        accelerometerReading[1] * accelerometerReading[1] +
                        accelerometerReading[2] * accelerometerReading[2]).toDouble()
                    ).toFloat()
                    
                    // Gravity is ~9.8 m/s², so valid readings should be around 0-20 m/s²
                    // (0 when in free fall, ~9.8 when stationary, higher when accelerating)
                    hasValidAccelerometerReading = magnitude > 0.1f && magnitude < 50f
                    
                    if (!hasValidAccelerometerReading) {
                        Log.d(TAG, "Accelerometer reading invalid: magnitude=$magnitude, values=[${accelerometerReading[0]}, ${accelerometerReading[1]}, ${accelerometerReading[2]}]")
                    }
                } else {
                    Log.w(TAG, "Accelerometer event has insufficient values: ${event.values.size}")
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (event.values.size >= 3) {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, 3)
                    
                    // Check if values are valid (not all zeros and within reasonable range)
                    val magnitude = sqrt(
                        (magnetometerReading[0] * magnetometerReading[0] +
                        magnetometerReading[1] * magnetometerReading[1] +
                        magnetometerReading[2] * magnetometerReading[2]).toDouble()
                    ).toFloat()
                    
                    // Earth's magnetic field is typically 20-60 microtesla, but sensor values vary
                    // We just check that it's not zero
                    hasValidMagnetometerReading = magnitude > 0.1f
                    
                    if (!hasValidMagnetometerReading) {
                        Log.d(TAG, "Magnetometer reading invalid: magnitude=$magnitude, values=[${magnetometerReading[0]}, ${magnetometerReading[1]}, ${magnetometerReading[2]}]")
                    }
                } else {
                    Log.w(TAG, "Magnetometer event has insufficient values: ${event.values.size}")
                }
            }
        }
        
        // Calculate orientation if we have valid readings
        // We can compute orientation with just accelerometer, but it's more accurate with both
        if (hasValidAccelerometerReading) {
            if (hasValidMagnetometerReading) {
                // Both sensors available - compute full orientation
                computeOrientation()
            } else {
                // Only accelerometer - compute basic orientation (pitch and roll only)
                computeBasicOrientation()
            }
            
            // Detect flick based on angle velocity
            detectFlick()
        }
        
        // Debounce updates
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = Runnable {
            computeDerivedValues()
            onPositionStateChanged(currentState)
        }
        handler.postDelayed(updateRunnable!!, UPDATE_DELAY_MS)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val sensorAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w(TAG, "Position sensor accuracy: UNRELIABLE")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w(TAG, "Position sensor accuracy: LOW")
                SensorAccuracy.LOW
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                Log.d(TAG, "Position sensor accuracy: MEDIUM")
                SensorAccuracy.MEDIUM
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                Log.d(TAG, "Position sensor accuracy: HIGH")
                SensorAccuracy.HIGH
            }
            else -> SensorAccuracy.UNKNOWN
        }
        
        currentState = currentState.copy(accuracy = sensorAccuracy)
    }
    
    /**
     * Compute device orientation using accelerometer and magnetometer
     */
    private fun computeOrientation() {
        if (!hasValidAccelerometerReading || !hasValidMagnetometerReading) {
            Log.d(TAG, "Cannot compute orientation: missing valid readings")
            return
        }
        
        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        
        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            // Convert radians to degrees
            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat() // Rotation around Z
            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat() // Rotation around X (tilt forward/back)
            val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat() // Rotation around Y (tilt left/right)
            
            // Add to history for smoothing
            pitchHistory.add(pitch)
            if (pitchHistory.size > ANGLE_HISTORY_SIZE) {
                pitchHistory.removeAt(0)
            }
            
            rollHistory.add(roll)
            if (rollHistory.size > ANGLE_HISTORY_SIZE) {
                rollHistory.removeAt(0)
            }
            
            // Calculate smoothed angles
            val smoothedPitch = if (pitchHistory.isNotEmpty()) pitchHistory.average().toFloat() else pitch
            val smoothedRoll = if (rollHistory.isNotEmpty()) rollHistory.average().toFloat() else roll
            
            currentState = currentState.copy(
                azimuth = azimuth,
                pitch = pitch,
                roll = roll,
                smoothedPitch = smoothedPitch,
                smoothedRoll = smoothedRoll
            )
            
            Log.d(TAG, "Orientation computed: azimuth=$azimuth°, pitch=$pitch°, roll=$roll°")
        } else {
            Log.w(TAG, "Failed to compute rotation matrix - using basic orientation")
            computeBasicOrientation()
        }
    }
    
    /**
     * Compute basic orientation using only accelerometer (pitch and roll, no azimuth)
     */
    private fun computeBasicOrientation() {
        if (!hasValidAccelerometerReading) {
            return
        }
        
        val ax = accelerometerReading[0]
        val ay = accelerometerReading[1]
        val az = accelerometerReading[2]
        
        // Calculate pitch (rotation around X axis)
        val pitch = Math.toDegrees(atan2(-ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))).toFloat()
        
        // Calculate roll (rotation around Y axis)
        val roll = Math.toDegrees(atan2(ay.toDouble(), az.toDouble())).toFloat()
        
        // Azimuth cannot be calculated without magnetometer, keep current value or 0
        val azimuth = if (hasValidMagnetometerReading) currentState.azimuth else 0f
        
        // Add to history for smoothing
        pitchHistory.add(pitch)
        if (pitchHistory.size > ANGLE_HISTORY_SIZE) {
            pitchHistory.removeAt(0)
        }
        
        rollHistory.add(roll)
        if (rollHistory.size > ANGLE_HISTORY_SIZE) {
            rollHistory.removeAt(0)
        }
        
        // Calculate smoothed angles
        val smoothedPitch = if (pitchHistory.isNotEmpty()) pitchHistory.average().toFloat() else pitch
        val smoothedRoll = if (rollHistory.isNotEmpty()) rollHistory.average().toFloat() else roll
        
        currentState = currentState.copy(
            azimuth = azimuth,
            pitch = pitch,
            roll = roll,
            smoothedPitch = smoothedPitch,
            smoothedRoll = smoothedRoll
        )
        
        Log.d(TAG, "Basic orientation computed: pitch=$pitch°, roll=$roll°")
    }
    
    private fun computeDerivedValues() {
        val state = currentState
        
        // Only compute derived values if we have valid readings
        if (!hasValidAccelerometerReading) {
            Log.d(TAG, "Skipping derived values computation: no valid accelerometer reading")
            return
        }
        
        // Determine device position
        val devicePosition = determineDevicePosition(state.smoothedPitch, state.smoothedRoll)
        
        // Determine recommended rotation
        val recommendedRotation = determineRecommendedRotation(devicePosition, state.smoothedPitch, state.smoothedRoll)
        
        // Calculate tilt angle (how much device is tilted from vertical)
        val tiltAngle = sqrt(
            (state.smoothedPitch * state.smoothedPitch + 
            state.smoothedRoll * state.smoothedRoll).toDouble()
        ).toFloat()
        
        // Determine if device is stable (not moving much)
        val isStable = isDeviceStable(state.pitch, state.roll)
        
        currentState = state.copy(
            devicePosition = devicePosition,
            recommendedRotation = recommendedRotation,
            tiltAngle = tiltAngle,
            isStable = isStable,
            flickDirection = state.flickDirection, // Keep current flick direction
            flickSpeed = state.flickSpeed // Keep current flick speed
        )
    }
    
    /**
     * Detect flick gesture based on rapid angle changes
     */
    private fun detectFlick() {
        val currentTime = System.currentTimeMillis()
        
        // Calculate angle velocities (degrees per second)
        if (lastUpdateTime > 0 && currentTime > lastUpdateTime) {
            val deltaTime = (currentTime - lastUpdateTime) / 1000.0f // Convert to seconds
            val pitchVelocity = (currentState.pitch - lastPitch) / deltaTime
            val rollVelocity = (currentState.roll - lastRoll) / deltaTime
            
            // Add to history
            angleVelocityHistory.add(Pair(pitchVelocity, rollVelocity))
            if (angleVelocityHistory.size > 5) {
                angleVelocityHistory.removeAt(0)
            }
            
            // Calculate average velocity for smoothing
            val avgPitchVelocity = if (angleVelocityHistory.isNotEmpty()) {
                angleVelocityHistory.map { it.first }.average().toFloat()
            } else 0f
            val avgRollVelocity = if (angleVelocityHistory.isNotEmpty()) {
                angleVelocityHistory.map { it.second }.average().toFloat()
            } else 0f
            
            // Check if we're in cooldown period
            val timeSinceLastFlick = currentTime - lastFlickTime

            // Detect flick based on velocity threshold
            if (timeSinceLastFlick > FLICK_COOLDOWN_MS) {
                val flickSpeed = sqrt((avgPitchVelocity * avgPitchVelocity + avgRollVelocity * avgRollVelocity).toDouble()).toFloat()

                // Log velocity for debugging
                if (flickSpeed > 10f) { // Log if speed is significant
                    Log.d(TAG, "Velocity: pitch=$avgPitchVelocity deg/s, roll=$avgRollVelocity deg/s, speed=$flickSpeed deg/s (threshold=${FLICK_SPEED_THRESHOLD})")
                }

                if (flickSpeed > FLICK_SPEED_THRESHOLD) {
                    // Determine flick direction based on dominant velocity (only LEFT/RIGHT)
                    val flickDirection = if (abs(avgRollVelocity) > abs(avgPitchVelocity)) {
                        // Horizontal flick only
                        if (avgRollVelocity > 0) FlickDirection.LEFT else FlickDirection.RIGHT
                    } else {
                        // If vertical movement is dominant, ignore it (no UP/DOWN flicks)
                        FlickDirection.NONE
                    }
                    
                    // Only proceed if we have a valid horizontal flick
                    if (flickDirection == FlickDirection.NONE) {
                        return@detectFlick
                    }
                    
                    // Update state with flick
                    currentState = currentState.copy(
                        flickDirection = flickDirection,
                        flickSpeed = flickSpeed
                    )
                    
                    lastFlickTime = currentTime
                    Log.d(TAG, "Flick detected: $flickDirection at speed $flickSpeed deg/s")
                    
                    // Immediately notify listener of flick
                    onPositionStateChanged(currentState)
                    
                    // Reset flick after a short delay
                    handler.postDelayed({
                        currentState = currentState.copy(
                            flickDirection = FlickDirection.NONE,
                            flickSpeed = 0f
                        )
                        onPositionStateChanged(currentState)
                    }, 200)
                } else {
                    // No flick, reset if not in cooldown
                    if (currentState.flickDirection != FlickDirection.NONE) {
                        currentState = currentState.copy(
                            flickDirection = FlickDirection.NONE,
                            flickSpeed = 0f
                        )
                    }
                }
            } else {
                // In cooldown, log for debugging
                if (currentState.flickDirection != FlickDirection.NONE) {
                    Log.d(TAG, "Flick in cooldown: ${timeSinceLastFlick}ms since last flick")
                }
            }
        }
        
        // Update tracking variables
        lastPitch = currentState.pitch
        lastRoll = currentState.roll
        lastUpdateTime = currentTime
    }
    
    /**
     * Determine device position based on pitch and roll
     */
    private fun determineDevicePosition(pitch: Float, roll: Float): DevicePosition {
        val absPitch = abs(pitch)
        val absRoll = abs(roll)
        
        return when {
            // Device is flat (lying on table)
            absPitch < FLAT_THRESHOLD && absRoll < FLAT_THRESHOLD -> DevicePosition.FLAT
            
            // Device is vertical (portrait)
            absPitch > 70f && absRoll < 30f -> {
                if (pitch > 0) DevicePosition.VERTICAL_FORWARD
                else DevicePosition.VERTICAL_BACKWARD
            }
            
            // Device is horizontal (landscape)
            absRoll > 70f && absPitch < 30f -> {
                if (roll > 0) DevicePosition.HORIZONTAL_LEFT
                else DevicePosition.HORIZONTAL_RIGHT
            }
            
            // Device is tilted
            absPitch > 30f || absRoll > 30f -> DevicePosition.TILTED
            
            // Default to upright
            else -> DevicePosition.UPRIGHT
        }
    }
    
    /**
     * Determine recommended screen rotation
     */
    private fun determineRecommendedRotation(
        position: DevicePosition,
        pitch: Float,
        roll: Float
    ): RecommendedRotation {
        return when (position) {
            DevicePosition.VERTICAL_FORWARD, DevicePosition.VERTICAL_BACKWARD -> {
                RecommendedRotation.PORTRAIT
            }
            DevicePosition.HORIZONTAL_LEFT -> {
                RecommendedRotation.LANDSCAPE_LEFT
            }
            DevicePosition.HORIZONTAL_RIGHT -> {
                RecommendedRotation.LANDSCAPE_RIGHT
            }
            DevicePosition.FLAT -> {
                RecommendedRotation.PORTRAIT // Default for flat
            }
            DevicePosition.TILTED -> {
                // For tilted positions, recommend based on dominant axis
                if (abs(roll) > abs(pitch)) {
                    if (roll > 0) RecommendedRotation.LANDSCAPE_LEFT
                    else RecommendedRotation.LANDSCAPE_RIGHT
                } else {
                    RecommendedRotation.PORTRAIT
                }
            }
            DevicePosition.UPRIGHT -> {
                RecommendedRotation.PORTRAIT
            }
        }
    }
    
    /**
     * Check if device is stable (not moving much)
     */
    private fun isDeviceStable(currentPitch: Float, currentRoll: Float): Boolean {
        if (pitchHistory.size < 3 || rollHistory.size < 3) return false
        
        // Calculate variance in recent readings
        val pitchVariance = calculateVariance(pitchHistory)
        val rollVariance = calculateVariance(rollHistory)
        
        // Device is stable if variance is low
        return pitchVariance < 5f && rollVariance < 5f
    }
    
    /**
     * Calculate variance of a list of values
     */
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        return variance
    }
    
    /**
     * Check if position sensors are available on this device
     * 
     * @return true if accelerometer is available, false otherwise
     */
    fun isAvailable(): Boolean {
        val hasAccelerometer = accelerometer != null
        val hasMagnetometer = magnetometer != null
        
        if (!hasAccelerometer) {
            Log.w(TAG, "Accelerometer not available for position detection")
        }
        if (!hasMagnetometer) {
            Log.w(TAG, "Magnetometer not available (orientation may be less accurate)")
        }
        
        return hasAccelerometer // At least accelerometer is needed
    }
    
    /**
     * Check if sensors are currently active
     */
    fun isActive(): Boolean = isListening
}

