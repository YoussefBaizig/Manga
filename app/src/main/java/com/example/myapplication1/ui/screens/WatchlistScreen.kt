package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: MangaViewModel,
    onMangaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlistManga by viewModel.watchlistManga.collectAsState()
    
    // Refresh watchlist when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.refreshWatchlistManga()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMiddle,
                        GradientEnd
                    )
                )
            )
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📚 Watchlist",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${watchlistManga.size} manga saved",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
        
        when {
            watchlistManga.isEmpty() -> {
                EmptyState(
                    emoji = "📖",
                    title = "Your watchlist is empty",
                    subtitle = "Add manga to your watchlist to see them here"
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = watchlistManga,
                        key = { it.malId }
                    ) { manga ->
                        MangaCardCompact(
                            manga = manga,
                            onClick = { onMangaClick(manga.malId) }
                        )
                    }
                }
            }
        }
    }
}
