package com.example.myapplication1.sensors

/**
 * Représente l'état actuel des capteurs de position
 * 
 * Contient les informations sur :
 * - L'orientation de l'appareil (azimuth, pitch, roll)
 * - La position de l'appareil (vertical, horizontal, incliné, etc.)
 * - La rotation recommandée pour l'écran
 * - L'angle d'inclinaison
 * - La stabilité de l'appareil
 */
data class PositionSensorState(
    // Sensor availability
    val isAvailable: Boolean = false,
    val isActive: Boolean = false,
    
    // Orientation angles (in degrees)
    val azimuth: Float = 0f,      // Rotation around Z axis (0-360°)
    val pitch: Float = 0f,        // Rotation around X axis (tilt forward/back, -90 to +90°)
    val roll: Float = 0f,         // Rotation around Y axis (tilt left/right, -180 to +180°)
    
    // Smoothed angles (averaged over recent readings)
    val smoothedPitch: Float = 0f,
    val smoothedRoll: Float = 0f,
    
    // Computed values
    val devicePosition: DevicePosition = DevicePosition.UPRIGHT,
    val recommendedRotation: RecommendedRotation = RecommendedRotation.PORTRAIT,
    val tiltAngle: Float = 0f,    // Total tilt angle from vertical (0-90°)
    val isStable: Boolean = false, // Whether device is stable (not moving)
    
    // Flick detection (for page navigation)
    val flickDirection: FlickDirection = FlickDirection.NONE, // Direction of flick gesture
    val flickSpeed: Float = 0f, // Speed of flick (degrees per second)
    
    // Sensor status
    val accuracy: SensorAccuracy = SensorAccuracy.UNKNOWN
) {
    /**
     * Check if device is in portrait orientation
     */
    val isPortrait: Boolean
        get() = devicePosition == DevicePosition.VERTICAL_FORWARD || 
                devicePosition == DevicePosition.VERTICAL_BACKWARD ||
                devicePosition == DevicePosition.UPRIGHT
    
    /**
     * Check if device is in landscape orientation
     */
    val isLandscape: Boolean
        get() = devicePosition == DevicePosition.HORIZONTAL_LEFT || 
                devicePosition == DevicePosition.HORIZONTAL_RIGHT
    
    /**
     * Check if device is tilted significantly
     */
    val isTilted: Boolean
        get() = tiltAngle > 15f
}

/**
 * Position physique de l'appareil
 */
enum class DevicePosition {
    UPRIGHT,              // Appareil droit (portrait normal)
    VERTICAL_FORWARD,     // Vertical, incliné vers l'avant
    VERTICAL_BACKWARD,    // Vertical, incliné vers l'arrière
    HORIZONTAL_LEFT,      // Horizontal, incliné vers la gauche (paysage)
    HORIZONTAL_RIGHT,     // Horizontal, incliné vers la droite (paysage)
    TILTED,               // Incliné dans une position intermédiaire
    FLAT                  // À plat (sur une table)
}

/**
 * Rotation recommandée pour l'écran
 */
enum class RecommendedRotation {
    PORTRAIT,           // Rotation portrait (0°)
    LANDSCAPE_LEFT,     // Rotation paysage gauche (90°)
    LANDSCAPE_RIGHT,    // Rotation paysage droite (270°)
    PORTRAIT_REVERSED  // Rotation portrait inversée (180°)
}

/**
 * Direction du flick (mouvement rapide) pour la navigation
 */
enum class FlickDirection {
    NONE,       // Pas de flick
    LEFT,       // Flick vers la gauche (page suivante)
    RIGHT,      // Flick vers la droite (page précédente)
    UP,         // Flick vers le haut
    DOWN        // Flick vers le bas
}

