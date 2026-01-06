package com.example.myapplication1.biometric

import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Manages biometric authentication (fingerprint) for the app.
 * 
 * This class provides a clean interface for checking biometric availability
 * and authenticating users using their fingerprint.
 */
class BiometricManager(
    private val activity: FragmentActivity
) {
    private val androidBiometricManager = AndroidBiometricManager.from(activity)
    
    /**
     * Check if biometric authentication is available on the device.
     * 
     * @return BiometricAvailability indicating the status
     */
    fun checkBiometricAvailability(): BiometricAvailability {
        return when (androidBiometricManager.canAuthenticate(AndroidBiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            AndroidBiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            else -> BiometricAvailability.UNAVAILABLE
        }
    }
    
    /**
     * Authenticate the user using biometric (fingerprint).
     * 
     * @param title Title for the biometric prompt
     * @param subtitle Subtitle for the biometric prompt
     * @param onSuccess Callback invoked when authentication succeeds
     * @param onError Callback invoked when authentication fails or is cancelled
     */
    fun authenticate(
        title: String = "Authenticate",
        subtitle: String = "Place your finger on the fingerprint sensor",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            onError("Authentication cancelled")
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            onError("Too many failed attempts. Please try again later.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Biometric authentication is permanently locked. Please use device settings to unlock.")
                        }
                        else -> {
                            onError("Authentication failed: $errString")
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Fingerprint not recognized. Please try again.")
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(AndroidBiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}

/**
 * Represents the availability status of biometric authentication.
 */
enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NOT_ENROLLED,
    UNAVAILABLE
}

