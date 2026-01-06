package com.example.myapplication1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication1.R
import com.example.myapplication1.biometric.BiometricAvailability
import com.example.myapplication1.biometric.BiometricManager
import com.example.myapplication1.ui.theme.CrimsonPrimary
import com.example.myapplication1.ui.theme.TextSecondary

/**
 * FirstPage - Splash screen with biometric authentication
 * 
 * Displays the splash screen and automatically prompts for fingerprint authentication.
 * If biometric is not available, provides a fallback tap-to-continue option.
 */
@Composable
fun FirstPage(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    
    var biometricAvailability by remember { mutableStateOf<BiometricAvailability?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFallback by remember { mutableStateOf(false) }
    
    // Check biometric availability when screen is first displayed
    LaunchedEffect(Unit) {
        if (activity != null) {
            val biometricManager = BiometricManager(activity)
            val availability = biometricManager.checkBiometricAvailability()
            biometricAvailability = availability
            
            when (availability) {
                BiometricAvailability.AVAILABLE -> {
                    // Start authentication automatically
                    biometricManager.authenticate(
                        title = "Touch to Start",
                        subtitle = "Place your finger on the fingerprint sensor",
                        onSuccess = {
                            errorMessage = null
                            onNavigateToHome()
                        },
                        onError = { error ->
                            errorMessage = error
                            // Show fallback after error
                            showFallback = true
                        }
                    )
                }
                BiometricAvailability.NOT_ENROLLED -> {
                    errorMessage = "No fingerprint enrolled. Please set up a fingerprint in device settings."
                    showFallback = true
                }
                BiometricAvailability.NO_HARDWARE -> {
                    errorMessage = "Fingerprint sensor not available on this device."
                    showFallback = true
                }
                BiometricAvailability.HARDWARE_UNAVAILABLE -> {
                    errorMessage = "Fingerprint sensor is currently unavailable."
                    showFallback = true
                }
                BiometricAvailability.UNAVAILABLE -> {
                    errorMessage = "Biometric authentication is not available."
                    showFallback = true
                }
            }
        } else {
            errorMessage = "Unable to access biometric authentication."
            showFallback = true
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image - splashscreen.png
        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = "Splash Screen - Touch Fingerprint to Start",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Error message or fallback option
        if (showFallback || errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .clickable { onNavigateToHome() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Text(
                    text = "Tap anywhere to continue",
                    color = CrimsonPrimary,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }
    }
}

