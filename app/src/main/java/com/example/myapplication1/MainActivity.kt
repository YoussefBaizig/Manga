package com.example.myapplication1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication1.ui.navigation.Screen
import com.example.myapplication1.ui.navigation.bottomNavItems
import com.example.myapplication1.ui.screens.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MangaAppTheme {
                MangaApp()
            }
        }
    }
}

@Composable
fun MangaApp() {
    val navController = rememberNavController()
    val viewModel: MangaViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Explore.route,
        Screen.Watchlist.route
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = InkBlack,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                MangaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // Clear genre selection when navigating to Explore
                        if (route == Screen.Explore.route) {
                            viewModel.clearGenreSelection()
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FirstPage.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (showBottomBar) 0.dp else innerPadding.calculateBottomPadding()
                )
        ) {
            composable(Screen.FirstPage.route) {
                FirstPage(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.FirstPage.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    },
                    onSeeAllTopRated = {
                        navController.navigate(Screen.TopRated.route)
                    },
                    onSeeAllPopular = {
                        navController.navigate(Screen.Popular.route)
                    }
                )
            }
            
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Explore.route) { backStackEntry ->
                // Clear genre selection when navigating to Explore to always show genre list
                LaunchedEffect(backStackEntry) {
                    viewModel.clearGenreSelection()
                }
                
                ExploreScreen(
                    viewModel = viewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.TopRated.route) {
                TopRatedListScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Popular.route) {
                PopularListScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = viewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("mangaId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getInt("mangaId") ?: return@composable
                DetailScreen(
                    mangaId = mangaId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    },
                    onReadMangaClick = { mangaDexId, mangaTitle ->
                        navController.navigate(Screen.ChaptersList.createRoute(mangaDexId, mangaTitle))
                    }
                )
            }
            
            composable(
                route = Screen.ChaptersList.route,
                arguments = listOf(
                    navArgument("mangaDexId") { type = NavType.StringType },
                    navArgument("mangaTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mangaDexId = backStackEntry.arguments?.getString("mangaDexId") ?: return@composable
                val mangaTitle = backStackEntry.arguments?.getString("mangaTitle")?.replace("_", "/") ?: "Manga"
                ChaptersListScreen(
                    mangaTitle = mangaTitle,
                    mangaDexId = mangaDexId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterId, chapterTitle ->
                        navController.navigate(Screen.ChapterReader.createRoute(chapterId, chapterTitle))
                    }
                )
            }
            
            composable(
                route = Screen.ChapterReader.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("chapterTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
                val chapterTitle = backStackEntry.arguments?.getString("chapterTitle")?.replace("_", "/") ?: "Chapter"
                ChapterReaderScreen(
                    chapterId = chapterId,
                    chapterTitle = chapterTitle,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun MangaBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .height(70.dp),
        containerColor = SurfaceDark,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CrimsonPrimary,
                    selectedTextColor = CrimsonPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = CrimsonPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
