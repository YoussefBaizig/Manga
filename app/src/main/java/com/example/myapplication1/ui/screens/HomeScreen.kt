package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.data.model.Manga
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaListState
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MangaViewModel,
    onMangaClick: (Int) -> Unit,
    onSeeAllTopRated: () -> Unit = {},
    onSeeAllPopular: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val topMangaState by viewModel.topMangaState.collectAsState()
    val popularMangaState by viewModel.popularMangaState.collectAsState()
    
    Box(
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header with refresh button
            item {
                HomeHeader(
                    isLoading = topMangaState.isLoading || popularMangaState.isLoading,
                    onRefresh = { viewModel.refresh() }
                )
            }
            
            // Top Manga Section (Featured horizontal scroll)
            item {
                SectionHeader(
                    title = "🔥 Top Rated Manga",
                    onSeeAllClick = onSeeAllTopRated
                )
            }
            
            item {
                TopMangaSection(
                    state = topMangaState,
                    onMangaClick = onMangaClick,
                    onRetry = { viewModel.loadTopManga() }
                )
            }
            
            // Popular Manga Section (Vertical list)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "📈 Most Popular",
                    onSeeAllClick = onSeeAllPopular
                )
            }
            
            if (popularMangaState.isLoading && popularMangaState.mangaList.isEmpty()) {
                item {
                    MangaLoadingIndicator(
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else if (popularMangaState.error != null && popularMangaState.mangaList.isEmpty()) {
                item {
                    MangaErrorMessage(
                        message = popularMangaState.error!!,
                        onRetry = { viewModel.loadPopularManga() }
                    )
                }
            } else {
                itemsIndexed(
                    items = popularMangaState.mangaList.take(10),
                    key = { _, manga -> manga.malId }
                ) { index, manga ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it * (index + 1) / 10 }
                        )
                    ) {
                        MangaListItem(
                            manga = manga,
                            onClick = { onMangaClick(manga.malId) },
                            rank = index + 1,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "マンガ",
                style = MaterialTheme.typography.displaySmall,
                color = CrimsonPrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Discover amazing manga",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }
        
        IconButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CrimsonPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CrimsonPrimary
                )
            }
        }
    }
}

@Composable
private fun TopMangaSection(
    state: MangaListState,
    onMangaClick: (Int) -> Unit,
    onRetry: () -> Unit
) {
    when {
        state.isLoading && state.mangaList.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                MangaLoadingIndicator()
            }
        }
        state.error != null && state.mangaList.isEmpty() -> {
            MangaErrorMessage(
                message = state.error!!,
                onRetry = onRetry,
                modifier = Modifier.height(300.dp)
            )
        }
        else -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = state.mangaList.take(15),
                    key = { it.malId }
                ) { manga ->
                    MangaCardFeatured(
                        manga = manga,
                        onClick = { onMangaClick(manga.malId) }
                    )
                }
            }
        }
    }
}
