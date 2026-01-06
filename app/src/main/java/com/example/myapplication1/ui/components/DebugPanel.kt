package com.example.myapplication1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Debug message data class
 */
data class DebugMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: DebugLevel = DebugLevel.DEBUG
)

enum class DebugLevel {
    DEBUG, INFO, WARNING, ERROR
}

/**
 * Debug log manager that stores messages in memory
 */
object DebugLogManager {
    private val maxMessages = 100
    private val messages = mutableListOf<DebugMessage>()
    
    fun addMessage(tag: String, message: String, level: DebugLevel = DebugLevel.DEBUG) {
        synchronized(messages) {
            messages.add(DebugMessage(tag = tag, message = message, level = level))
            if (messages.size > maxMessages) {
                messages.removeAt(0)
            }
        }
    }
    
    fun getMessages(): List<DebugMessage> {
        return synchronized(messages) {
            messages.toList()
        }
    }
    
    fun clear() {
        synchronized(messages) {
            messages.clear()
        }
    }
}

/**
 * Debug panel composable that displays debug information
 */
@Composable
fun DebugPanel(
    visible: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = remember { mutableStateOf<List<DebugMessage>>(emptyList()) }
    
    // Update messages periodically
    LaunchedEffect(visible) {
        while (visible) {
            messages.value = DebugLogManager.getMessages()
            kotlinx.coroutines.delay(500) // Update every 500ms
        }
    }
    
    if (visible) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "🐛 Debug Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Row {
                        TextButton(onClick = { DebugLogManager.clear() }) {
                            Text("Clear")
                        }
                        TextButton(onClick = onClose) {
                            Text("Close")
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Messages
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false)
                ) {
                    if (messages.value.isEmpty()) {
                        Text(
                            text = "No debug messages yet. Interact with the app to see logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        messages.value.takeLast(50).forEach { msg ->
                            DebugMessageRow(msg)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugMessageRow(message: DebugMessage) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))
    
    val color = when (message.level) {
        DebugLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        DebugLevel.INFO -> MaterialTheme.colorScheme.primary
        DebugLevel.WARNING -> MaterialTheme.colorScheme.errorContainer
        DebugLevel.ERROR -> MaterialTheme.colorScheme.error
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "[$timeString]",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "[${message.tag}]",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = message.message,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}

