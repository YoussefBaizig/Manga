package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshotFlow
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularListScreen(
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onMangaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val popularMangaState by viewModel.popularMangaState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📈 Most Popular",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            when {
                popularMangaState.isLoading && popularMangaState.mangaList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MangaLoadingIndicator()
                    }
                }
                popularMangaState.error != null && popularMangaState.mangaList.isEmpty() -> {
                    MangaErrorMessage(
                        message = popularMangaState.error!!,
                        onRetry = { viewModel.loadPopularManga() }
                    )
                }
                popularMangaState.mangaList.isEmpty() -> {
                    EmptyState(
                        emoji = "📚",
                        title = "No manga found",
                        subtitle = "Try refreshing"
                    )
                }
                else -> {
                    val listState = rememberLazyListState()
                    
                    // Auto-load more when scrolling near the end
                    LaunchedEffect(listState) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                        }.collect { lastVisibleIndex ->
                            val totalItems = popularMangaState.mangaList.size
                            if (lastVisibleIndex >= totalItems - 3 && 
                                popularMangaState.canLoadMore && 
                                !popularMangaState.isLoading) {
                                viewModel.loadMorePopularManga()
                            }
                        }
                    }
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        itemsIndexed(
                            items = popularMangaState.mangaList,
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
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                        
                        // Loading indicator at the bottom when loading more
                        if (popularMangaState.isLoading && popularMangaState.mangaList.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = CrimsonPrimary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                        }
                        
                        // Load more button if available and not loading
                        if (popularMangaState.canLoadMore && !popularMangaState.isLoading && popularMangaState.mangaList.isNotEmpty()) {
                            item {
                                Button(
                                    onClick = { viewModel.loadMorePopularManga() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrimsonPrimary
                                    )
                                ) {
                                    Text(
                                        text = "Load More",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
