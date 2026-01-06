package com.example.myapplication1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.myapplication1.data.api.GenreInfo
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: MangaViewModel,
    onMangaClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val genreState by viewModel.genreState.collectAsState()
    
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
            if (genreState.selectedGenre != null) {
                // Back button when viewing genre manga
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.clearGenreSelection() // Clear selection and return to genres view
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = genreState.selectedGenre!!.name ?: "Genre",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Browse manga by genre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
        
        when {
            genreState.isLoading && genreState.genres.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MangaLoadingIndicator()
                }
            }
            
            genreState.error != null && genreState.genres.isEmpty() -> {
                MangaErrorMessage(
                    message = genreState.error!!,
                    onRetry = { viewModel.loadGenres() }
                )
            }
            
            genreState.selectedGenre != null -> {
                // Show manga for selected genre
                GenreMangaGrid(
                    viewModel = viewModel,
                    genreState = genreState,
                    onMangaClick = onMangaClick
                )
            }
            
            else -> {
                // Show genres grid
                GenresGrid(
                    genres = genreState.genres,
                    onGenreClick = { viewModel.selectGenre(it) }
                )
            }
        }
    }
}

@Composable
private fun GenresGrid(
    genres: List<GenreInfo>,
    onGenreClick: (GenreInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(genres) { genre ->
            GenreCard(
                genre = genre,
                onClick = { onGenreClick(genre) }
            )
        }
    }
}

@Composable
private fun GenreCard(
    genre: GenreInfo,
    onClick: () -> Unit
) {
    val genreColors = mapOf(
        "Action" to GenreAction,
        "Romance" to GenreRomance,
        "Comedy" to GenreComedy,
        "Fantasy" to GenreFantasy,
        "Horror" to GenreHorror,
        "Sci-Fi" to GenreSciFi,
        "Drama" to GenreDrama,
        "Slice of Life" to GenreSliceOfLife
    )
    
    val backgroundColor = genreColors[genre.name] ?: CrimsonPrimary.copy(alpha = 0.8f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = genre.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                genre.count?.let { count ->
                    Text(
                        text = "$count manga",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreMangaGrid(
    viewModel: MangaViewModel,
    genreState: com.example.myapplication1.ui.viewmodel.GenreState,
    onMangaClick: (Int) -> Unit
) {
    when {
        genreState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MangaLoadingIndicator()
            }
        }
        
        genreState.error != null -> {
            MangaErrorMessage(
                message = genreState.error!!,
                onRetry = { 
                    genreState.selectedGenre?.let { viewModel.selectGenre(it) }
                }
            )
        }
        
        genreState.genreManga.isEmpty() -> {
            EmptyState(
                emoji = "📭",
                title = "No manga found",
                subtitle = "Try another genre"
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
                    items = genreState.genreManga,
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

