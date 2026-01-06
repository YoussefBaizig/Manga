package com.example.myapplication1.sensors

/**
 * Represents the current state of motion sensors
 * 
 * This data class holds all sensor readings and computed values
 * from the accelerometer and gyroscope sensors.
 */
data class MotionSensorState(
    // Accelerometer values (m/s²)
    val accelerationX: Float = 0f,
    val accelerationY: Float = 0f,
    val accelerationZ: Float = 0f,
    
    // Gyroscope values (rad/s)
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f,
    
    // Computed values
    val orientation: DeviceOrientation = DeviceOrientation.UNKNOWN,
    val isVibrating: Boolean = false,
    val isFreeFalling: Boolean = false,
    val horizontalMovement: HorizontalMovement = HorizontalMovement.NONE,
    
    // Additional computed values
    val accelerationMagnitude: Float = 0f, // Total acceleration magnitude
    val rotationMagnitude: Float = 0f       // Total rotation magnitude
) {
    /**
     * Check if device is in portrait mode (normal or reversed)
     */
    val isPortrait: Boolean
        get() = orientation == DeviceOrientation.PORTRAIT || 
                orientation == DeviceOrientation.PORTRAIT_REVERSED
    
    /**
     * Check if device is in landscape mode (normal or reversed)
     */
    val isLandscape: Boolean
        get() = orientation == DeviceOrientation.LANDSCAPE || 
                orientation == DeviceOrientation.LANDSCAPE_REVERSED
    
    /**
     * Check if device is moving horizontally
     */
    val isMovingHorizontally: Boolean
        get() = horizontalMovement != HorizontalMovement.NONE
}

/**
 * Device orientation based on accelerometer readings
 * 
 * When the device is still, the accelerometer primarily shows
 * the direction of gravity, which we use to determine orientation.
 */
enum class DeviceOrientation {
    PORTRAIT,           // Vertical, normal (home button at bottom)
    PORTRAIT_REVERSED,  // Vertical, upside down
    LANDSCAPE,          // Horizontal, normal (home button on right)
    LANDSCAPE_REVERSED, // Horizontal, reversed (home button on left)
    UNKNOWN             // Flat on table or other orientation
}

/**
 * Horizontal movement direction detected from accelerometer
 * 
 * This indicates the direction of horizontal movement/tilting
 * of the device, useful for gesture-based navigation.
 */
enum class HorizontalMovement {
    LEFT,   // Device is tilting/moving left
    RIGHT,  // Device is tilting/moving right
    NONE    // No significant horizontal movement detected
}
