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
import kotlin.math.sqrt

/**
 * Manages motion sensors (accelerometer and gyroscope) for the manga reader
 * 
 * This class provides:
 * - Device orientation detection (portrait/landscape)
 * - Horizontal movement detection for gesture navigation
 * - Vibration detection
 * - Free fall detection
 * 
 * All sensor callbacks are debounced and executed on the main thread
 * to ensure safe UI updates.
 */
class MotionSensorManager(
    private val context: Context,
    private val onSensorStateChanged: (MotionSensorState) -> Unit
) : SensorEventListener {
    
    companion object {
        private const val TAG = "MotionSensorManager"
        
        // Sensor update frequency
        private const val UPDATE_DELAY_MS = 50L // Update every 50ms (20Hz)
        
        // Detection thresholds
        private const val FREE_FALL_THRESHOLD = 2.0f // m/s² (gravity is ~9.8 m/s²)
        private const val VIBRATION_VARIANCE_THRESHOLD = 0.5f
        private const val MOVEMENT_THRESHOLD = 1.2f // m/s²
        private const val MOVEMENT_COOLDOWN_MS = 800L // Minimum time between movements
        
        // History sizes for smoothing
        private const val ACCELERATION_HISTORY_SIZE = 10
        private const val MOVEMENT_HISTORY_SIZE = 5
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val gyroscope: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    private var currentState = MotionSensorState()
    private var isListening = false
    
    // For vibration detection - track recent acceleration changes
    private val accelerationHistory = mutableListOf<Float>()
    
    // For horizontal movement detection
    private val accelerationXHistory = mutableListOf<Float>()
    private var lastMovementTime = 0L
    
    // Handler for debouncing updates on main thread
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    
    /**
     * Start listening to motion sensors
     * 
     * @return true if sensors were successfully started, false otherwise
     */
    fun start(): Boolean {
        if (isListening) {
            Log.w(TAG, "Sensors already listening")
            return true
        }
        
        var started = false
        
        accelerometer?.let {
            val success = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI // ~60Hz updates
            )
            if (success) {
                started = true
                Log.d(TAG, "Accelerometer started")
            }
        } ?: Log.w(TAG, "Accelerometer not available")
        
        gyroscope?.let {
            val success = sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
            if (success) {
                Log.d(TAG, "Gyroscope started")
            }
        } ?: Log.w(TAG, "Gyroscope not available")
        
        isListening = started
        return started
    }
    
    /**
     * Stop listening to motion sensors
     */
    fun stop() {
        if (!isListening) return
        
        sensorManager.unregisterListener(this)
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
        accelerationHistory.clear()
        accelerationXHistory.clear()
        isListening = false
        
        Log.d(TAG, "Sensors stopped")
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                updateAccelerometerData(event.values)
            }
            Sensor.TYPE_GYROSCOPE -> {
                updateGyroscopeData(event.values)
            }
        }
        
        // Debounce updates to avoid too frequent callbacks
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = Runnable {
            computeDerivedValues()
            onSensorStateChanged(currentState)
        }
        handler.postDelayed(updateRunnable!!, UPDATE_DELAY_MS)
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                Log.w(TAG, "Sensor accuracy: UNRELIABLE")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                Log.w(TAG, "Sensor accuracy: LOW")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                Log.d(TAG, "Sensor accuracy: MEDIUM")
            }
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                Log.d(TAG, "Sensor accuracy: HIGH")
            }
        }
    }
    
    private fun updateAccelerometerData(values: FloatArray) {
        if (values.size < 3) return
        
        currentState = currentState.copy(
            accelerationX = values[0],
            accelerationY = values[1],
            accelerationZ = values[2]
        )
        
        // Track acceleration magnitude for vibration detection
        val magnitude = sqrt(
            values[0] * values[0] + 
            values[1] * values[1] + 
            values[2] * values[2]
        )
        
        accelerationHistory.add(magnitude)
        if (accelerationHistory.size > ACCELERATION_HISTORY_SIZE) {
            accelerationHistory.removeAt(0)
        }
    }
    
    private fun updateGyroscopeData(values: FloatArray) {
        if (values.size < 3) return
        
        currentState = currentState.copy(
            rotationX = values[0],
            rotationY = values[1],
            rotationZ = values[2]
        )
    }
    
    private fun computeDerivedValues() {
        val state = currentState
        
        // Compute acceleration and rotation magnitudes
        val accelerationMagnitude = sqrt(
            state.accelerationX * state.accelerationX +
            state.accelerationY * state.accelerationY +
            state.accelerationZ * state.accelerationZ
        )
        
        val rotationMagnitude = sqrt(
            state.rotationX * state.rotationX +
            state.rotationY * state.rotationY +
            state.rotationZ * state.rotationZ
        )
        
        // Compute orientation
        val orientation = computeOrientation(
            state.accelerationX,
            state.accelerationY,
            state.accelerationZ
        )
        
        // Detect vibration
        val isVibrating = detectVibration()
        
        // Detect free fall
        val isFreeFalling = detectFreeFall(accelerationMagnitude)
        
        // Detect horizontal movement
        val horizontalMovement = detectHorizontalMovement(state.accelerationX)
        
        currentState = state.copy(
            accelerationMagnitude = accelerationMagnitude,
            rotationMagnitude = rotationMagnitude,
            orientation = orientation,
            isVibrating = isVibrating,
            isFreeFalling = isFreeFalling,
            horizontalMovement = horizontalMovement
        )
    }
    
    private fun computeOrientation(
        accelX: Float,
        accelY: Float,
        accelZ: Float
    ): DeviceOrientation {
        // Use accelerometer to determine orientation
        // When device is still, accelerometer shows gravity direction
        
        val absX = abs(accelX)
        val absY = abs(accelY)
        val absZ = abs(accelZ)
        
        // Determine which axis has the strongest gravity component
        // Use a threshold to avoid false positives when device is moving
        val threshold = 3.0f // m/s²
        
        return when {
            // Portrait (vertical) - Y axis dominant
            absY > absX && absY > absZ && absY > threshold -> {
                if (accelY > 0) DeviceOrientation.PORTRAIT
                else DeviceOrientation.PORTRAIT_REVERSED
            }
            // Landscape (horizontal) - X axis dominant
            absX > absY && absX > absZ && absX > threshold -> {
                if (accelX > 0) DeviceOrientation.LANDSCAPE
                else DeviceOrientation.LANDSCAPE_REVERSED
            }
            // Flat on table or other orientation
            else -> DeviceOrientation.UNKNOWN
        }
    }
    
    private fun detectVibration(): Boolean {
        if (accelerationHistory.size < 3) return false
        
        // Calculate variance in acceleration magnitude
        val mean = accelerationHistory.average().toFloat()
        val variance = accelerationHistory
            .map { (it - mean) * (it - mean) }
            .average()
            .toFloat()
        
        // High variance indicates vibration
        return variance > VIBRATION_VARIANCE_THRESHOLD
    }
    
    private fun detectFreeFall(magnitude: Float): Boolean {
        // In free fall, total acceleration should be close to 0
        // (device is accelerating with gravity, so relative acceleration is low)
        // Normal gravity is ~9.8 m/s², free fall shows much less
        return magnitude < FREE_FALL_THRESHOLD
    }
    
    private fun detectHorizontalMovement(currentAccelX: Float): HorizontalMovement {
        val currentTime = System.currentTimeMillis()
        
        // Add current acceleration to history
        accelerationXHistory.add(currentAccelX)
        if (accelerationXHistory.size > MOVEMENT_HISTORY_SIZE) {
            accelerationXHistory.removeAt(0)
        }
        
        // Need enough history to detect movement
        if (accelerationXHistory.size < MOVEMENT_HISTORY_SIZE) {
            return HorizontalMovement.NONE
        }
        
        // Check cooldown to avoid too frequent movements
        if (currentTime - lastMovementTime < MOVEMENT_COOLDOWN_MS) {
            return HorizontalMovement.NONE
        }
        
        // Calculate average acceleration change (velocity indicator)
        // Compare first half vs second half of history
        val midPoint = MOVEMENT_HISTORY_SIZE / 2
        val firstHalf = accelerationXHistory
            .take(midPoint)
            .average()
            .toFloat()
        val secondHalf = accelerationXHistory
            .takeLast(MOVEMENT_HISTORY_SIZE - midPoint)
            .average()
            .toFloat()
        val deltaX = secondHalf - firstHalf
        
        return when {
            deltaX > MOVEMENT_THRESHOLD -> {
                lastMovementTime = currentTime
                HorizontalMovement.RIGHT
            }
            deltaX < -MOVEMENT_THRESHOLD -> {
                lastMovementTime = currentTime
                HorizontalMovement.LEFT
            }
            else -> HorizontalMovement.NONE
        }
    }
    
    /**
     * Check if sensors are available on this device
     * 
     * @return true if both accelerometer and gyroscope are available
     */
    fun areSensorsAvailable(): Boolean {
        val hasAccelerometer = accelerometer != null
        val hasGyroscope = gyroscope != null
        
        // Motion sensors are available if at least accelerometer is present
        // Gyroscope is optional but improves accuracy
        val isAvailable = hasAccelerometer
        
        if (!hasAccelerometer) {
            Log.w(TAG, "Accelerometer not available on this device")
        }
        if (!hasGyroscope) {
            Log.w(TAG, "Gyroscope not available on this device (optional, will use accelerometer only)")
        }
        
        if (isAvailable) {
            Log.d(TAG, "Motion sensors available: accelerometer=$hasAccelerometer, gyroscope=$hasGyroscope")
        }
        
        return isAvailable // Return true if accelerometer is available (gyroscope is optional)
    }
    
    /**
     * Check if sensors are currently listening
     */
    fun isActive(): Boolean = isListening
}
