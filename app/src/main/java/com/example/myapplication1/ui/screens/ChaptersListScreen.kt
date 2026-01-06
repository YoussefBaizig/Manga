package com.example.myapplication1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.data.model.MangaDexChapter
import com.example.myapplication1.ui.components.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersListScreen(
    mangaTitle: String,
    mangaDexId: String,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onChapterClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chaptersState by viewModel.mangadexChaptersState.collectAsState()
    
    LaunchedEffect(mangaDexId) {
        viewModel.loadMangaDexChapters(mangaDexId)
    }
    
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = { 
                    Text(
                        text = "Chapters",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
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
                    containerColor = SurfaceDark
                )
            )
            
            // Manga title
            Text(
                text = mangaTitle,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Chapters list
            when {
                chaptersState.first.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            emoji = "📖",
                            title = "No chapters found",
                            subtitle = "This manga may not be available on MangaDex"
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = chaptersState.first.reversed(), // Show latest first
                            key = { it.id }
                        ) { chapter ->
                            ChapterListItem(
                                chapter = chapter,
                                onClick = {
                                    val title = chapter.attributes.title 
                                        ?: "Chapter ${chapter.attributes.chapter ?: "?"}"
                                    onChapterClick(chapter.id, title)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterListItem(
    chapter: MangaDexChapter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InkBlackCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.attributes.title 
                        ?: "Chapter ${chapter.attributes.chapter ?: "?"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    chapter.attributes.chapter?.let {
                        Text(
                            text = "Ch. $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonPrimary
                        )
                    }
                    
                    chapter.attributes.volume?.let {
                        Text(
                            text = " • Vol. $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    
                    chapter.attributes.translatedLanguage?.let {
                        Text(
                            text = " • $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                
                chapter.attributes.pages?.let { pages ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$pages pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            
            // Read button
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

