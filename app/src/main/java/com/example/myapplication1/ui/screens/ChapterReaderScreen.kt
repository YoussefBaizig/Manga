package com.example.myapplication1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication1.data.model.MangaDexChapter
import com.example.myapplication1.data.model.MangaDexChapterPages
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

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
    val baseUrl = pages.baseUrl
    val hash = pages.chapter.hash
    val pageUrls = pages.chapter.data
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        TopAppBar(
            title = { 
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1
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
                containerColor = InkBlack.copy(alpha = 0.9f)
            )
        )
        
        // Chapter pages
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = pageUrls,
                key = { it }
            ) { page ->
                ChapterPageImage(
                    imageUrl = "$baseUrl/data/$hash/$page",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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

