package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication1.data.model.MangaDexChapterPages
import com.example.myapplication1.sensors.MotionSensorManager
import com.example.myapplication1.sensors.MotionSensorState
import com.example.myapplication1.sensors.DeviceOrientation
import com.example.myapplication1.sensors.HorizontalMovement
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.components.DebugPanel
import com.example.myapplication1.ui.components.DebugLogManager
import com.example.myapplication1.ui.components.DebugLevel
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.myapplication1.sensors.LidarSensorManager
import com.example.myapplication1.sensors.LidarSensorState
import com.example.myapplication1.sensors.LightSensorManager
import com.example.myapplication1.sensors.LightSensorState
import com.example.myapplication1.sensors.LightCategory
import com.example.myapplication1.sensors.BrightnessManager
import com.example.myapplication1.sensors.PositionSensorManager
import com.example.myapplication1.sensors.PositionSensorState
import com.example.myapplication1.sensors.FlickDirection
import com.example.myapplication1.sensors.BlueLightFilterEffect
import com.example.myapplication1.ui.theme.AdaptiveReadingTheme
import com.example.myapplication1.ui.theme.getAdaptiveTheme
import com.example.myapplication1.ui.theme.getAdaptiveColorScheme
import com.example.myapplication1.ui.theme.getRecommendedBrightnessForTheme
import android.provider.Settings
import android.os.Build
import android.content.pm.PackageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    chapterId: String,
    chapterTitle: String,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapterPagesState by viewModel.chapterPagesState.collectAsState()
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapterPages(chapterId)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearChapterPages()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InkBlack)
    ) {
        when {
            chapterPagesState.isLoading -> {
                MangaLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            chapterPagesState.error != null -> {
                MangaErrorMessage(
                    message = chapterPagesState.error!!,
                    onRetry = { viewModel.loadChapterPages(chapterId) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            chapterPagesState.pages != null -> {
                ChapterReaderContent(
                    pages = chapterPagesState.pages!!,
                    chapterTitle = chapterTitle,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterReaderContent(
    pages: MangaDexChapterPages,
    chapterTitle: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val baseUrl = pages.baseUrl
    val hash = pages.chapter.hash
    val pageUrls = pages.chapter.data
    
    // Motion sensor state
    var sensorState by remember { mutableStateOf<MotionSensorState?>(null) }
    var motionSensorManager by remember { mutableStateOf<MotionSensorManager?>(null) }
    var sensorsAvailable by remember { mutableStateOf(false) }
    
    // LiDAR sensor state
    var lidarState by remember { mutableStateOf<LidarSensorState?>(null) }
    var lidarSensorManager by remember { mutableStateOf<LidarSensorManager?>(null) }
    var lidarAvailable by remember { mutableStateOf(false) }
    
    // Light sensor state
    var lightState by remember { mutableStateOf<LightSensorState?>(null) }
    var lightSensorManager by remember { mutableStateOf<LightSensorManager?>(null) }
    var lightAvailable by remember { mutableStateOf(false) }
    
    // Brightness manager
    val brightnessManager = remember { BrightnessManager(context) }
    var autoBrightnessEnabled by remember { mutableStateOf(true) } // Enabled by default
    var brightnessPermissionGranted by remember { mutableStateOf(false) }
    
    // Position sensor state
    var positionState by remember { mutableStateOf<PositionSensorState?>(null) }
    var positionSensorManager by remember { mutableStateOf<PositionSensorManager?>(null) }
    var positionAvailable by remember { mutableStateOf(false) }
    
    // Adaptive theme state
    var currentAdaptiveTheme by remember { mutableStateOf(AdaptiveReadingTheme.NORMAL_MODE) }
    var adaptiveThemeEnabled by remember { mutableStateOf(true) } // Enabled by default
    
    // Page navigation state
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentPageIndex by remember { mutableStateOf(0) }
    var isNavigating by remember { mutableStateOf(false) }
    var showSensorInfo by remember { mutableStateOf(false) }
    var showLidarInfo by remember { mutableStateOf(false) }
    var showLightInfo by remember { mutableStateOf(false) }
    var showPositionInfo by remember { mutableStateOf(false) }
    var showDebugPanel by remember { mutableStateOf(false) }
    
    // Initialize motion sensor manager (with error handling)
    LaunchedEffect(Unit) {
        try {
            DebugLogManager.addMessage("ChapterReader", "Initializing motion sensors...", DebugLevel.INFO)
            val manager = MotionSensorManager(context) { state ->
                // Callback is already on main thread via Handler
                sensorState = state
                DebugLogManager.addMessage("MotionSensor", "State updated: ${state.orientation.name}", DebugLevel.DEBUG)
            }
            
            sensorsAvailable = manager.areSensorsAvailable()
            DebugLogManager.addMessage("ChapterReader", "Motion sensors available: $sensorsAvailable", 
                if (sensorsAvailable) DebugLevel.INFO else DebugLevel.WARNING)
            
            if (sensorsAvailable) {
                try {
                    val started = manager.start()
                    if (started) {
                        motionSensorManager = manager
                        DebugLogManager.addMessage("ChapterReader", "Motion sensors started successfully", DebugLevel.INFO)
                    } else {
                        DebugLogManager.addMessage("ChapterReader", "Failed to start motion sensors", DebugLevel.ERROR)
                        sensorsAvailable = false
                    }
                } catch (e: Exception) {
                    DebugLogManager.addMessage("ChapterReader", "Error starting motion sensors: ${e.message}", DebugLevel.ERROR)
                    sensorsAvailable = false
                }
            } else {
                DebugLogManager.addMessage("ChapterReader", "Motion sensors not available on this device", DebugLevel.WARNING)
            }
        } catch (e: Exception) {
            DebugLogManager.addMessage("ChapterReader", "Error creating motion sensor manager: ${e.message}", DebugLevel.ERROR)
            sensorsAvailable = false
        }
    }
    
    // Initialize LiDAR sensor manager (with error handling and logging)
    LaunchedEffect(Unit) {
        try {
            DebugLogManager.addMessage("ChapterReader", "Initializing LiDAR sensor...", DebugLevel.INFO)
            val lidarManager = LidarSensorManager(context) { state ->
                // Callback is already on main thread via Handler
                lidarState = state
                DebugLogManager.addMessage("LidarSensor", "Distance: ${state.distanceCm}cm", DebugLevel.DEBUG)
            }
            
            lidarAvailable = lidarManager.isAvailable()
            DebugLogManager.addMessage("ChapterReader", "LiDAR sensor available: $lidarAvailable", 
                if (lidarAvailable) DebugLevel.INFO else DebugLevel.WARNING)
            
            if (lidarAvailable) {
                try {
                    val started = lidarManager.start()
                    if (started) {
                        lidarSensorManager = lidarManager
                        DebugLogManager.addMessage("ChapterReader", "LiDAR sensor started successfully", DebugLevel.INFO)
                    } else {
                        DebugLogManager.addMessage("ChapterReader", "Failed to start LiDAR sensor", DebugLevel.ERROR)
                        lidarAvailable = false
                    }
                } catch (e: Exception) {
                    DebugLogManager.addMessage("ChapterReader", "Error starting LiDAR sensor: ${e.message}", DebugLevel.ERROR)
                    lidarAvailable = false
                }
            } else {
                DebugLogManager.addMessage("ChapterReader", "LiDAR sensor not available on this device", DebugLevel.WARNING)
            }
        } catch (e: Exception) {
            DebugLogManager.addMessage("ChapterReader", "Error creating LiDAR sensor manager: ${e.message}", DebugLevel.ERROR)
            lidarAvailable = false
        }
    }
    
    // Check brightness permission (recheck when returning from settings)
    LaunchedEffect(Unit) {
        brightnessPermissionGranted = brightnessManager.canWriteSettings()
    }
    
    // Recheck permission when auto brightness is toggled
    LaunchedEffect(autoBrightnessEnabled) {
        if (autoBrightnessEnabled) {
            brightnessPermissionGranted = brightnessManager.canWriteSettings()
        }
    }
    
    // Initialize position sensor manager (with error handling and logging)
    LaunchedEffect(Unit) {
        try {
            DebugLogManager.addMessage("ChapterReader", "Initializing position sensor...", DebugLevel.INFO)
            val positionManager = PositionSensorManager(context) { state ->
                // Callback is already on main thread via Handler
                positionState = state
                DebugLogManager.addMessage("PositionSensor", 
                    "State updated: pitch=${state.pitch}°, roll=${state.roll}°, azimuth=${state.azimuth}°", 
                    DebugLevel.DEBUG)
            }
            
            positionAvailable = positionManager.isAvailable()
            DebugLogManager.addMessage("ChapterReader", "Position sensor available: $positionAvailable", 
                if (positionAvailable) DebugLevel.INFO else DebugLevel.WARNING)
            
            if (positionAvailable) {
                try {
                    val started = positionManager.start()
                    if (started) {
                        positionSensorManager = positionManager
                        DebugLogManager.addMessage("ChapterReader", "Position sensor started successfully", DebugLevel.INFO)
                    } else {
                        DebugLogManager.addMessage("ChapterReader", "Failed to start position sensor", DebugLevel.ERROR)
                        positionAvailable = false
                    }
                } catch (e: Exception) {
                    DebugLogManager.addMessage("ChapterReader", "Error starting position sensor: ${e.message}", DebugLevel.ERROR)
                    positionAvailable = false
                }
            } else {
                DebugLogManager.addMessage("ChapterReader", "Position sensor not available on this device", DebugLevel.WARNING)
            }
        } catch (e: Exception) {
            DebugLogManager.addMessage("ChapterReader", "Error creating position sensor manager: ${e.message}", DebugLevel.ERROR)
            positionAvailable = false
        }
    }
    
    // Initialize light sensor manager (with error handling)
    LaunchedEffect(Unit) {
        try {
            val lightManager = LightSensorManager(context) { state ->
                // Callback is already on main thread via Handler
                lightState = state
                
                // Update adaptive theme based on light level
                if (adaptiveThemeEnabled) {
                    val newTheme = getAdaptiveTheme(state.lightCategory)
                    if (newTheme != currentAdaptiveTheme) {
                        currentAdaptiveTheme = newTheme
                    }
                }
                
                // Auto-adjust brightness if enabled
                if (autoBrightnessEnabled && brightnessPermissionGranted) {
                    // Use theme-based brightness if adaptive theme is enabled
                    val recommendedBrightness = if (adaptiveThemeEnabled) {
                        getRecommendedBrightnessForTheme(currentAdaptiveTheme)
                    } else {
                        state.recommendedBrightness
                    }
                    
                    // Get WindowManager for immediate effect
                    val windowManager = try {
                        (context as? android.app.Activity)?.windowManager
                    } catch (e: Exception) {
                        null
                    }
                    
                    // Adjust brightness with debug logging
                    DebugLogManager.addMessage("Brightness", 
                        "Adjusting: recommended=${(recommendedBrightness * 100).toInt()}%, enabled=$autoBrightnessEnabled, permission=$brightnessPermissionGranted", 
                        DebugLevel.INFO)
                    brightnessManager.adjustBrightness(recommendedBrightness, windowManager)
                }
            }
            
            lightAvailable = lightManager.isAvailable()
            DebugLogManager.addMessage("ChapterReader", "Light sensor available: $lightAvailable", 
                if (lightAvailable) DebugLevel.INFO else DebugLevel.WARNING)
            
            if (lightAvailable) {
                try {
                    val started = lightManager.start()
                    if (started) {
                        lightSensorManager = lightManager
                        DebugLogManager.addMessage("ChapterReader", "Light sensor started successfully", DebugLevel.INFO)
                    } else {
                        DebugLogManager.addMessage("ChapterReader", "Failed to start light sensor", DebugLevel.ERROR)
                        lightAvailable = false
                    }
                } catch (e: Exception) {
                    DebugLogManager.addMessage("ChapterReader", "Error starting light sensor: ${e.message}", DebugLevel.ERROR)
                    lightAvailable = false
                }
            } else {
                DebugLogManager.addMessage("ChapterReader", "Light sensor not available on this device", DebugLevel.WARNING)
            }
        } catch (e: Exception) {
            DebugLogManager.addMessage("ChapterReader", "Error creating light sensor manager: ${e.message}", DebugLevel.ERROR)
            lightAvailable = false
        }
    }
    
    // Handle auto brightness toggle
    LaunchedEffect(autoBrightnessEnabled) {
        if (autoBrightnessEnabled) {
            if (brightnessPermissionGranted) {
                brightnessManager.enableAutoBrightness()
                // Apply current recommended brightness immediately
                lightState?.let { state ->
                    val windowManager = try {
                        (context as? android.app.Activity)?.windowManager
                    } catch (e: Exception) {
                        null
                    }
                    DebugLogManager.addMessage("Brightness", 
                        "Initial brightness: recommended=${(state.recommendedBrightness * 100).toInt()}%", 
                        DebugLevel.INFO)
                    brightnessManager.adjustBrightness(state.recommendedBrightness, windowManager)
                }
            }
        } else {
            brightnessManager.disableAutoBrightness()
        }
    }
    
    // Apply adaptive theme and blue light filter
    val adaptiveColorScheme = getAdaptiveColorScheme(currentAdaptiveTheme)
    
    // Apply blue light filter for night mode
    BlueLightFilterEffect(
        theme = currentAdaptiveTheme,
        enabled = adaptiveThemeEnabled && currentAdaptiveTheme == AdaptiveReadingTheme.NIGHT_MODE
    )
    
    // Cleanup on dispose (with error handling)
    DisposableEffect(Unit) {
            onDispose {
                try {
                    motionSensorManager?.stop()
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
                try {
                    lidarSensorManager?.stop()
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
                try {
                    lightSensorManager?.stop()
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
                try {
                    positionSensorManager?.stop()
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
                try {
                    // Disable auto brightness on cleanup
                    if (autoBrightnessEnabled) {
                        brightnessManager.disableAutoBrightness()
                    }
                } catch (e: Exception) {
                    // Ignore errors during cleanup
                }
            }
    }
    
    // Update current page index based on scroll position
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { visibleIndex ->
                if (visibleIndex >= 0 && visibleIndex < pageUrls.size) {
                    currentPageIndex = visibleIndex
                }
            }
    }
    
    // Handle horizontal movement for page navigation (with error handling)
    LaunchedEffect(sensorState?.horizontalMovement) {
        try {
            val movement = sensorState?.horizontalMovement
            if (movement != null && !isNavigating && pageUrls.isNotEmpty()) {
                when (movement) {
                    HorizontalMovement.LEFT -> {
                        // Move to next page (tilt left = next page)
                        if (currentPageIndex < pageUrls.size - 1) {
                            isNavigating = true
                            val nextIndex = currentPageIndex + 1
                            try {
                                listState.animateScrollToItem(nextIndex)
                            } catch (e: Exception) {
                                // If scroll fails, just update index
                                currentPageIndex = nextIndex
                            }
                            delay(300) // Debounce
                            isNavigating = false
                        }
                    }
                    HorizontalMovement.RIGHT -> {
                        // Move to previous page (tilt right = previous page)
                        if (currentPageIndex > 0) {
                            isNavigating = true
                            val prevIndex = currentPageIndex - 1
                            try {
                                listState.animateScrollToItem(prevIndex)
                            } catch (e: Exception) {
                                // If scroll fails, just update index
                                currentPageIndex = prevIndex
                            }
                            delay(300) // Debounce
                            isNavigating = false
                        }
                    }
                    HorizontalMovement.NONE -> {
                        // No action
                    }
                }
            }
        } catch (e: Exception) {
            // If navigation fails, continue normally
            isNavigating = false
        }
    }
    
    // Track last flick time for cooldown
    var lastFlickNavigationTime by remember { mutableStateOf(0L) }
    
    // Handle flick gestures for page navigation
    LaunchedEffect(positionState?.flickDirection) {
        val flick = positionState?.flickDirection
        
        // Ignore NONE completely - do nothing, no logging
        if (flick == null || flick == FlickDirection.NONE) {
            return@LaunchedEffect
        }
        
        try {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastFlick = currentTime - lastFlickNavigationTime
            
            // Check cooldown (1 second)
            if (timeSinceLastFlick < 1000L) {
                DebugLogManager.addMessage("ChapterReader", "Flick ignored: cooldown active (${(1000L - timeSinceLastFlick)}ms remaining)", DebugLevel.DEBUG)
                return@LaunchedEffect
            }
            
            if (!isNavigating && pageUrls.isNotEmpty()) {
                DebugLogManager.addMessage("ChapterReader", "Processing flick: $flick", DebugLevel.INFO)
                when (flick) {
                    FlickDirection.LEFT -> {
                        // Flick left = next page
                        if (currentPageIndex < pageUrls.size - 1) {
                            isNavigating = true
                            lastFlickNavigationTime = currentTime
                            val nextIndex = currentPageIndex + 1
                            DebugLogManager.addMessage("ChapterReader", "Navigating to next page: $nextIndex via flick LEFT", DebugLevel.INFO)
                            coroutineScope.launch {
                                try {
                                    listState.animateScrollToItem(nextIndex)
                                    currentPageIndex = nextIndex
                                    DebugLogManager.addMessage("ChapterReader", "Successfully navigated to page $nextIndex", DebugLevel.INFO)
                                } catch (e: Exception) {
                                    DebugLogManager.addMessage("ChapterReader", "Error scrolling to next page: ${e.message}", DebugLevel.ERROR)
                                    currentPageIndex = nextIndex
                                }
                                delay(500) // Debounce flick
                                isNavigating = false
                            }
                        } else {
                            DebugLogManager.addMessage("ChapterReader", "Cannot go to next page: already at last page ($currentPageIndex/${pageUrls.size - 1})", DebugLevel.DEBUG)
                        }
                    }
                    FlickDirection.RIGHT -> {
                        // Flick right = previous page
                        if (currentPageIndex > 0) {
                            isNavigating = true
                            lastFlickNavigationTime = currentTime
                            val prevIndex = currentPageIndex - 1
                            DebugLogManager.addMessage("ChapterReader", "Navigating to previous page: $prevIndex via flick RIGHT", DebugLevel.INFO)
                            coroutineScope.launch {
                                try {
                                    listState.animateScrollToItem(prevIndex)
                                    currentPageIndex = prevIndex
                                    DebugLogManager.addMessage("ChapterReader", "Successfully navigated to page $prevIndex", DebugLevel.INFO)
                                } catch (e: Exception) {
                                    DebugLogManager.addMessage("ChapterReader", "Error scrolling to previous page: ${e.message}", DebugLevel.ERROR)
                                    currentPageIndex = prevIndex
                                }
                                delay(500) // Debounce flick
                                isNavigating = false
                            }
                        } else {
                            DebugLogManager.addMessage("ChapterReader", "Cannot go to previous page: already at first page ($currentPageIndex)", DebugLevel.DEBUG)
                        }
                    }
                    FlickDirection.UP, FlickDirection.DOWN -> {
                        // Vertical flicks are ignored (not used for navigation)
                        // No logging for vertical flicks
                    }
                    FlickDirection.NONE -> {
                        // Should never reach here due to early return, but handle just in case
                    }
                }
            } else {
                if (isNavigating) {
                    DebugLogManager.addMessage("ChapterReader", "Flick ignored: already navigating", DebugLevel.DEBUG)
                } else if (pageUrls.isEmpty()) {
                    DebugLogManager.addMessage("ChapterReader", "Flick ignored: no pages available", DebugLevel.DEBUG)
                }
            }
        } catch (e: Exception) {
            DebugLogManager.addMessage("ChapterReader", "Error handling flick: ${e.message}", DebugLevel.ERROR)
            isNavigating = false
        }
    }
    
    // Handle free fall detection
    val showFreeFallWarning = sensorState?.isFreeFalling == true
    
    // Helper function for orientation display
    fun getOrientationDisplayName(orientation: DeviceOrientation): String {
        return when (orientation) {
            DeviceOrientation.PORTRAIT -> "Portrait"
            DeviceOrientation.PORTRAIT_REVERSED -> "Portrait (Reversed)"
            DeviceOrientation.LANDSCAPE -> "Landscape"
            DeviceOrientation.LANDSCAPE_REVERSED -> "Landscape (Reversed)"
            DeviceOrientation.UNKNOWN -> "Unknown"
        }
    }
    
    // Apply adaptive theme - use MaterialTheme wrapper
    MaterialTheme(
        colorScheme = adaptiveColorScheme
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            // Top bar with sensor status
        TopAppBar(
            title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                            color = adaptiveColorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Show sensor status and theme
                        val statusText = buildString {
                            append("Page ${currentPageIndex + 1}/${pageUrls.size}")
                            if (adaptiveThemeEnabled) {
                                append(" | ")
                                append(
                                    when (currentAdaptiveTheme) {
                                        AdaptiveReadingTheme.NIGHT_MODE -> "🌙 Mode Nuit"
                                        AdaptiveReadingTheme.NORMAL_MODE -> "💡 Mode Normal"
                                        AdaptiveReadingTheme.HIGH_CONTRAST_MODE -> "☀️ Contraste Élevé"
                                    }
                                )
                            }
                            if (sensorsAvailable && sensorState != null) {
                                append(" | ")
                                append(getOrientationDisplayName(sensorState!!.orientation))
                            }
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = adaptiveColorScheme.onSurfaceVariant
                        )
                    }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                            tint = adaptiveColorScheme.onSurface
                        )
                    }
                },
            actions = {
                // Debug panel button (always visible)
                IconButton(
                    onClick = { 
                        DebugLogManager.addMessage("ChapterReader", "Debug panel toggle clicked", DebugLevel.INFO)
                        showDebugPanel = !showDebugPanel 
                    }
                ) {
                    Text(
                        text = if (showDebugPanel) "🐛" else "🔍",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Toggle position sensor info button
                if (positionAvailable) {
                    IconButton(
                        onClick = { 
                            DebugLogManager.addMessage("ChapterReader", "Position info toggle clicked", DebugLevel.INFO)
                            showPositionInfo = !showPositionInfo 
                        }
                    ) {
                        Text(
                            text = if (showPositionInfo) "📍" else "🧭",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Toggle light sensor info button
                if (lightAvailable) {
                    IconButton(
                        onClick = { 
                            DebugLogManager.addMessage("ChapterReader", "Light info toggle clicked", DebugLevel.INFO)
                            showLightInfo = !showLightInfo 
                        }
                    ) {
                        Text(
                            text = if (showLightInfo) "💡" else "☀️",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Toggle LiDAR info button
                if (lidarAvailable) {
                    IconButton(
                        onClick = { 
                            DebugLogManager.addMessage("ChapterReader", "LiDAR info toggle clicked", DebugLevel.INFO)
                            showLidarInfo = !showLidarInfo 
                        }
                    ) {
                        Text(
                            text = if (showLidarInfo) "📏" else "🔦",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Toggle sensor info button
                if (sensorsAvailable) {
                    IconButton(
                        onClick = { 
                            DebugLogManager.addMessage("ChapterReader", "Sensor info toggle clicked", DebugLevel.INFO)
                            showSensorInfo = !showSensorInfo 
                        }
                    ) {
                        Text(
                            text = if (showSensorInfo) "👁️" else "📊",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = adaptiveColorScheme.surface.copy(alpha = 0.9f)
                )
            )
        
        // Free fall warning
        AnimatedVisibility(
            visible = showFreeFallWarning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Free fall detected!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Face proximity warning (from LiDAR)
        AnimatedVisibility(
            visible = lidarState?.isFaceTooClose == true && lidarState?.faceProximityWarning != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lidarState?.faceProximityWarning?.let { warning ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Sensor info card (when enabled)
        AnimatedVisibility(
            visible = showSensorInfo && sensorState != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            sensorState?.let { state ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = adaptiveColorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📊 Sensor Information",
                            style = MaterialTheme.typography.titleSmall,
                            color = adaptiveColorScheme.onSurface
                        )
                        SensorInfoRow("Orientation", state.orientation.name)
                        SensorInfoRow("Acceleration", String.format("%.2f m/s²", state.accelerationMagnitude))
                        SensorInfoRow("Rotation", String.format("%.2f rad/s", state.rotationMagnitude))
                        if (state.isVibrating) {
                            SensorInfoRow("Status", "📳 Vibrating")
                        }
                        if (state.isMovingHorizontally) {
                            SensorInfoRow("Movement", state.horizontalMovement.name)
                        }
                    }
                }
            }
        }
        
        // LiDAR info card (when enabled)
        AnimatedVisibility(
            visible = showLidarInfo && lidarState != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lidarState?.let { state ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = adaptiveColorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔦 LiDAR / ToF Sensor",
                                style = MaterialTheme.typography.titleSmall,
                                color = adaptiveColorScheme.onSurface
                            )
                            if (state.isActive) {
                                Text(
                                    text = "●",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        if (state.hasValidDistance) {
                            SensorInfoRow(
                                "Distance", 
                                String.format("%.2f m (%.1f cm)", state.distance, state.distanceCm)
                            )
                            if (state.averageDistance > 0f) {
                                SensorInfoRow(
                                    "Average", 
                                    String.format("%.2f m", state.averageDistance)
                                )
                            }
                            SensorInfoRow(
                                "Range", 
                                String.format("%.2f - %.2f m", state.minDistance, state.maxDistance)
                            )
                        } else {
                            Text(
                                text = "No valid distance measurement",
                                style = MaterialTheme.typography.labelSmall,
                                color = adaptiveColorScheme.onSurfaceVariant
                            )
                        }
                        
                        SensorInfoRow("Accuracy", state.accuracy.name)
                        SensorInfoRow("Status", if (state.isActive) "Active" else "Inactive")
                        
                        if (state.detectedObjects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detected Objects: ${state.detectedObjects.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = adaptiveColorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        // Position sensor info card (when enabled)
        AnimatedVisibility(
            visible = showPositionInfo && positionState != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            positionState?.let { state ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = adaptiveColorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧭 Position Sensor",
                                style = MaterialTheme.typography.titleSmall,
                                color = adaptiveColorScheme.onSurface
                            )
                            if (state.isStable) {
                                Text(
                                    text = "●",
                                    color = adaptiveColorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        SensorInfoRow(
                            "Position",
                            state.devicePosition.name.replace("_", " ")
                        )
                        SensorInfoRow(
                            "Recommended Rotation",
                            state.recommendedRotation.name.replace("_", " ")
                        )
                        SensorInfoRow(
                            "Pitch",
                            String.format("%.1f°", state.smoothedPitch)
                        )
                        SensorInfoRow(
                            "Roll",
                            String.format("%.1f°", state.smoothedRoll)
                        )
                        SensorInfoRow(
                            "Azimuth",
                            String.format("%.1f°", state.azimuth)
                        )
                        SensorInfoRow(
                            "Tilt Angle",
                            String.format("%.1f°", state.tiltAngle)
                        )
                        SensorInfoRow(
                            "Stable",
                            if (state.isStable) "Yes" else "No"
                        )
                        SensorInfoRow("Accuracy", state.accuracy.name)
                    }
                }
            }
        }
        
        // Light sensor info card (when enabled)
        AnimatedVisibility(
            visible = showLightInfo && lightState != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lightState?.let { state ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = adaptiveColorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☀️ Light Sensor (Photoelectric Cell)",
                                style = MaterialTheme.typography.titleSmall,
                                color = adaptiveColorScheme.onSurface
                            )
                            if (state.isActive) {
                                Text(
                                    text = "●",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        
                        // Light level
                        SensorInfoRow(
                            "Light Level", 
                            String.format("%.1f lux", state.lightLevel)
                        )
                        
                        // Light category
                        SensorInfoRow(
                            "Category", 
                            state.lightCategory.name.replace("_", " ")
                        )
                        
                        // Average light
                        if (state.averageLightLevel > 0f) {
                            SensorInfoRow(
                                "Average", 
                                String.format("%.1f lux", state.averageLightLevel)
                            )
                        }
                        
                        // Environmental conditions
                        if (state.isDark) {
                            SensorInfoRow("Condition", "🌙 Dark")
                        } else if (state.isNight) {
                            SensorInfoRow("Condition", "🌃 Night")
                        } else if (state.isBright) {
                            SensorInfoRow("Condition", "☀️ Bright")
                        }
                        
                        // Recommended brightness
                        SensorInfoRow(
                            "Recommended Brightness", 
                            String.format("%.0f%%", state.recommendedBrightness * 100f)
                        )
                        
                        // Auto brightness toggle
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (autoBrightnessEnabled) "💡" else "☀️",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (autoBrightnessEnabled) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        adaptiveColorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Auto Brightness",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = adaptiveColorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = autoBrightnessEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !brightnessPermissionGranted) {
                                        // Request permission
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            // Note: User needs to grant permission manually in settings
                                            // We'll show a message
                                        }
                                    }
                                    autoBrightnessEnabled = enabled
                                },
                                enabled = brightnessPermissionGranted || !autoBrightnessEnabled
                            )
                        }
                        
                        if (!brightnessPermissionGranted && autoBrightnessEnabled) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "⚠️",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Permission Required",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Auto brightness needs permission to modify system settings.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Button(
                                        onClick = {
                                            brightnessManager.openSettingsToGrantPermission()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Text("Open Settings")
                                    }
                                    Text(
                                        text = "After granting permission, return to the app and toggle Auto Brightness again.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        // RGB color data (if available)
                        if (state.hasColorData) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Color Analysis:",
                                style = MaterialTheme.typography.labelSmall,
                                color = adaptiveColorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            SensorInfoRow("Red", String.format("%.1f", state.redLight))
                            SensorInfoRow("Green", String.format("%.1f", state.greenLight))
                            SensorInfoRow("Blue", String.format("%.1f", state.blueLight))
                            SensorInfoRow(
                                "Dominant Color", 
                                state.dominantColor.name
                            )
                            if (state.colorTemperature > 0f) {
                                SensorInfoRow(
                                    "Color Temperature", 
                                    String.format("%.0f K", state.colorTemperature)
                                )
                            }
                        }
                        
                        SensorInfoRow("Accuracy", state.accuracy.name)
                        SensorInfoRow("Status", if (state.isActive) "Active" else "Inactive")
                    }
                }
            }
        }
        
        // Debug panel (always available)
        AnimatedVisibility(
            visible = showDebugPanel,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DebugPanel(
                visible = showDebugPanel,
                onClose = { showDebugPanel = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // Chapter pages - Always visible, sensors are optional
        if (pageUrls.isNotEmpty()) {
        LazyColumn(
                state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
                itemsIndexed(
                items = pageUrls,
                    key = { _, page -> page }
                ) { index, page ->
                ChapterPageImage(
                    imageUrl = "$baseUrl/data/$hash/$page",
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(index, pageUrls.size, coroutineScope) {
                                val scope = coroutineScope
                                detectTapGestures(
                                    onTap = { offset ->
                                        // Tap left side for previous, right side for next
                                        val screenWidth = size.width
                                        DebugLogManager.addMessage("ChapterReader", "Tap detected at x=${offset.x}, screenWidth=$screenWidth, index=$index", DebugLevel.DEBUG)
                                        
                                        if (offset.x < screenWidth / 2) {
                                            // Left side - previous page
                                            if (index > 0) {
                                                DebugLogManager.addMessage("ChapterReader", "Navigating to previous page: ${index - 1}", DebugLevel.INFO)
                                                scope.launch {
                                                    try {
                                                        listState.animateScrollToItem(index - 1)
                                                    } catch (e: Exception) {
                                                        DebugLogManager.addMessage("ChapterReader", "Error scrolling to previous page: ${e.message}", DebugLevel.ERROR)
                                                    }
                                                }
                                            }
                                        } else {
                                            // Right side - next page
                                            if (index < pageUrls.size - 1) {
                                                DebugLogManager.addMessage("ChapterReader", "Navigating to next page: ${index + 1}", DebugLevel.INFO)
                                                scope.launch {
                                                    try {
                                                        listState.animateScrollToItem(index + 1)
                                                    } catch (e: Exception) {
                                                        DebugLogManager.addMessage("ChapterReader", "Error scrolling to next page: ${e.message}", DebugLevel.ERROR)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        } else {
            // Fallback if no pages
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pages available",
                    color = adaptiveColorScheme.onSurfaceVariant
                )
            }
        }
        }
    }
}

@Composable
private fun SensorInfoRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurface
        )
    }
}

@Composable
private fun ChapterPageImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
