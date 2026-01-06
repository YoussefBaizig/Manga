package com.example.myapplication1.sensors

/**
 * Represents the current state of LiDAR/ToF (Time of Flight) sensor
 * 
 * LiDAR (Light Detection and Ranging) uses laser pulses to measure distances.
 * On Android, this is typically implemented as a ToF (Time of Flight) sensor
 * or Depth sensor that measures the time it takes for light to bounce back.
 */
data class LidarSensorState(
    // Distance measurements (in meters)
    val distance: Float = 0f,              // Primary distance measurement
    val minDistance: Float = 0f,            // Minimum measurable distance
    val maxDistance: Float = 0f,            // Maximum measurable distance
    
    // Depth map data (if available)
    val depthMap: FloatArray? = null,       // 2D array of depth measurements
    val depthMapWidth: Int = 0,             // Width of depth map
    val depthMapHeight: Int = 0,            // Height of depth map
    
    // Sensor status
    val isAvailable: Boolean = false,       // Whether LiDAR sensor is available
    val isActive: Boolean = false,          // Whether sensor is currently active
    val accuracy: SensorAccuracy = SensorAccuracy.UNKNOWN,
    
    // Computed values
    val averageDistance: Float = 0f,        // Average distance over recent readings
    val distanceHistory: List<Float> = emptyList(), // Recent distance readings
    
    // Object detection
    val detectedObjects: List<DetectedObject> = emptyList(), // Objects detected in the scene
    
    // Face proximity warning
    val isFaceTooClose: Boolean = false, // True if face is detected too close to screen
    val faceProximityWarning: String? = null // Warning message if face is too close
) {
    /**
     * Check if a valid distance measurement is available
     */
    val hasValidDistance: Boolean
        get() = distance > 0f && distance >= minDistance && distance <= maxDistance
    
    /**
     * Get distance in centimeters
     */
    val distanceCm: Float
        get() = distance * 100f
    
    /**
     * Get distance in millimeters
     */
    val distanceMm: Float
        get() = distance * 1000f
}

/**
 * Sensor accuracy level
 */
enum class SensorAccuracy {
    UNKNOWN,
    LOW,        // Low accuracy
    MEDIUM,     // Medium accuracy
    HIGH,       // High accuracy
    VERY_HIGH   // Very high accuracy
}

/**
 * Represents a detected object in the LiDAR scan
 */
data class DetectedObject(
    val centerX: Float,           // X coordinate of object center
    val centerY: Float,           // Y coordinate of object center
    val distance: Float,          // Distance to object (meters)
    val width: Float,             // Estimated width (meters)
    val height: Float,             // Estimated height (meters)
    val confidence: Float = 0f    // Detection confidence (0.0 to 1.0)
)

