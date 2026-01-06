package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication1.data.model.*
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mangaId: Int,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onMangaClick: (Int) -> Unit,
    onReadMangaClick: (String, String) -> Unit, // mangaDexId, mangaTitle
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val isInWatchlist = mangaId in watchlistIds
    
    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetails(mangaId)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMangaDetails()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InkBlack)
    ) {
        when {
            detailState.isLoading -> {
                MangaLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            detailState.error != null -> {
                MangaErrorMessage(
                    message = detailState.error!!,
                    onRetry = { viewModel.loadMangaDetails(mangaId) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            detailState.manga != null -> {
                MangaDetailContent(
                    manga = detailState.manga!!,
                    viewModel = viewModel,
                    onBackClick = onBackClick,
                    onReadMangaClick = onReadMangaClick,
                    isInWatchlist = isInWatchlist
                )
            }
        }
        
        // Top bar overlay
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            InkBlack.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { /* TODO: Share */ },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            InkBlack.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        detailState.manga?.let { manga ->
                            if (isInWatchlist) {
                                viewModel.removeFromWatchlist(manga.malId)
                            } else {
                                viewModel.addToWatchlist(manga)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            InkBlack.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                        tint = if (isInWatchlist) CrimsonPrimary else Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun MangaDetailContent(
    manga: Manga,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onReadMangaClick: (String, String) -> Unit,
    isInWatchlist: Boolean = false
) {
    var isSearchingMangaDex by remember { mutableStateOf(false) }
    var mangaDexId by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background blur image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(manga.getImageUrl())
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .blur(20.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            InkBlack.copy(alpha = 0.8f),
                            InkBlack
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Cover and basic info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Cover image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.getImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = manga.title,
                    modifier = Modifier
                        .width(140.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = manga.getDisplayTitle(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    manga.titleJapanese?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Score
                    manga.score?.let { score ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = CyberGold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.2f", score),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            manga.scoredBy?.let {
                                Text(
                                    text = " / ${formatNumber(it)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Status
                    manga.status?.let { status ->
                        val statusColor = when (status.lowercase()) {
                            "publishing" -> StatusPublishing
                            "finished" -> StatusCompleted
                            "on hiatus" -> StatusHiatus
                            else -> TextMuted
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor
                        ) {
                            Text(
                                text = status,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Read Manga Button - PROMINENT!
            Button(
                onClick = {
                    isSearchingMangaDex = true
                    viewModel.searchMangaDexForReading(manga.getDisplayTitle()) { foundId ->
                        isSearchingMangaDex = false
                        if (foundId != null) {
                            mangaDexId = foundId
                            onReadMangaClick(foundId, manga.getDisplayTitle())
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSearchingMangaDex
            ) {
                if (isSearchingMangaDex) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Searching MangaDex...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "📖 Read Manga",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Rank",
                    value = manga.rank?.let { "#$it" } ?: "N/A",
                    emoji = "🏆"
                )
                StatItem(
                    label = "Popularity",
                    value = manga.popularity?.let { "#$it" } ?: "N/A",
                    emoji = "📈"
                )
                StatItem(
                    label = "Chapters",
                    value = manga.chapters?.toString() ?: "?",
                    emoji = "📖"
                )
                StatItem(
                    label = "Volumes",
                    value = manga.volumes?.toString() ?: "?",
                    emoji = "📚"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Genres
            if (!manga.genres.isNullOrEmpty()) {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(manga.genres!!) { genre ->
                        GenreChip(name = genre.name ?: "")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Synopsis
            manga.synopsis?.let { synopsis ->
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Authors
            if (!manga.authors.isNullOrEmpty()) {
                Text(
                    text = "Authors",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                manga.authors!!.forEach { author ->
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = author.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Additional info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoRow("Type", manga.type ?: "N/A")
                    InfoRow("Published", manga.published?.string ?: "N/A")
                    InfoRow("Members", manga.members?.let { formatNumber(it) } ?: "N/A")
                    InfoRow("Favorites", manga.favorites?.let { formatNumber(it) } ?: "N/A")
                    
                    manga.serializations?.firstOrNull()?.name?.let {
                        InfoRow("Serialization", it)
                    }
                }
            }
            
            // Background info (if available)
            manga.background?.let { background ->
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Background",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = background,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}

