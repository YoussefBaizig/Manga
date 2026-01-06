package com.example.myapplication1.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication1.data.model.Manga
import com.example.myapplication1.data.model.getDisplayTitle
import com.example.myapplication1.data.model.getGenreNames
import com.example.myapplication1.data.model.getImageUrl
import com.example.myapplication1.ui.theme.*

/**
 * Manga Card - Featured/Large variant
 */
@Composable
fun MangaCardFeatured(
    manga: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100)
    )
    
    Card(
        modifier = modifier
            .width(200.dp)
            .height(300.dp)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = CrimsonPrimary.copy(alpha = 0.3f)
            )
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InkBlackCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cover image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.getImageUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = manga.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                InkBlack.copy(alpha = 0.7f),
                                InkBlack.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
            
            // Score badge
            manga.score?.let { score ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = CrimsonPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = CyberGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", score),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Title and info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = manga.getDisplayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Genres
                val genres = manga.getGenreNames().take(2)
                if (genres.isNotEmpty()) {
                    Text(
                        text = genres.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Manga Card - Compact/Grid variant
 */
@Composable
fun MangaCardCompact(
    manga: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InkBlackCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.getImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = manga.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Score badge
                manga.score?.let { score ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = InkBlack.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = CyberGold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", score),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Status badge
                manga.status?.let { status ->
                    val statusColor = when (status.lowercase()) {
                        "publishing" -> StatusPublishing
                        "finished" -> StatusCompleted
                        "on hiatus" -> StatusHiatus
                        else -> TextMuted
                    }
                    
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = manga.getDisplayTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                manga.chapters?.let { chapters ->
                    Text(
                        text = "$chapters chapters",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Manga List Item - Horizontal layout for lists
 */
@Composable
fun MangaListItem(
    manga: Manga,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InkBlackCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number (optional)
            rank?.let {
                Text(
                    text = "#$it",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (it <= 3) CyberGold else TextMuted,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(48.dp)
                )
            }
            
            // Cover image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.getImageUrl())
                    .crossfade(true)
                    .build(),
                contentDescription = manga.title,
                modifier = Modifier
                    .size(70.dp, 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = manga.getDisplayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Type and chapters
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    manga.type?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonLight
                        )
                        Text(
                            text = " • ",
                            color = TextMuted
                        )
                    }
                    manga.chapters?.let {
                        Text(
                            text = "$it ch",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } ?: Text(
                        text = "Ongoing",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusPublishing
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Score
                manga.score?.let { score ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = CyberGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.2f", score),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        manga.scoredBy?.let {
                            Text(
                                text = " (${formatNumber(it)} votes)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
            
            // Bookmark icon
            IconButton(onClick = { /* TODO: Add to favorites */ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Bookmark",
                    tint = TextSecondary
                )
            }
        }
    }
}

/**
 * Genre Chip
 */
@Composable
fun GenreChip(
    name: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CrimsonPrimary else SurfaceLight,
        animationSpec = tween(200)
    )
    
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

/**
 * Loading indicator
 */
@Composable
fun MangaLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = CrimsonPrimary,
            trackColor = SurfaceLight
        )
    }
}

/**
 * Error message component
 */
@Composable
fun MangaErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
        ) {
            Text("Retry")
        }
    }
}

/**
 * Section header
 */
@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        onSeeAllClick?.let {
            TextButton(onClick = it) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrimsonPrimary
                )
            }
        }
    }
}

/**
 * Empty state
 */
@Composable
fun EmptyState(
    emoji: String = "📚",
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

// Helper function to format numbers
private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

