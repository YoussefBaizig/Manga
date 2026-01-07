package com.example.myapplication1

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication1.anime.AnimeMainActivity
import com.example.myapplication1.ui.navigation.Screen
import com.example.myapplication1.ui.navigation.bottomNavItems
import com.example.myapplication1.ui.screens.*
import com.example.myapplication1.ui.theme.*
import com.example.myapplication1.ui.viewmodel.MangaViewModel
import com.example.myapplication1.ui.viewmodel.UserViewModel
import com.example.myapplication1.ui.viewmodel.ViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startRoute = intent.getStringExtra("nav_route") ?: Screen.FirstPage.route
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
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as MangaApplication
    val viewModelFactory = remember { ViewModelFactory.create(application) }
    val mangaViewModel: MangaViewModel = viewModel(factory = viewModelFactory)
    val userViewModel: UserViewModel = viewModel(factory = viewModelFactory)
    
    val authState by userViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Set current user ID in MangaViewModel when authenticated
    LaunchedEffect(authState.isAuthenticated, authState.currentUser) {
        if (authState.isAuthenticated && authState.currentUser != null) {
            mangaViewModel.setCurrentUserId(authState.currentUser!!.id)
        } else {
            mangaViewModel.setCurrentUserId(null)
        }
    }
    
    // Navigate based on authentication state
    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated && currentRoute != Screen.Login.route && currentRoute != Screen.Signup.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) // Clear back stack
            }
        } else if (authState.isAuthenticated && (currentRoute == Screen.Login.route || currentRoute == Screen.Signup.route)) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) // Clear back stack
            }
        }
    }
    
    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Explore.route,
        Screen.Watchlist.route
    ) && authState.isAuthenticated
    
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
                            mangaViewModel.clearGenreSelection()
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
            // First Page - Splash screen with fingerprint authentication
            composable(Screen.FirstPage.route) {
                FirstPage(
                    onNavigateToHome = {
                        // Navigate to appropriate screen based on auth state
                        if (authState.isAuthenticated) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) // Clear back stack
                            }
                        } else {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) // Clear back stack
                            }
                        }
                    }
                )
            }
            
            // Authentication Screens
            composable(Screen.Login.route) {
                LoginScreen(
                    userViewModel = userViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) // Clear back stack
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    }
                )
            }
            
            composable(Screen.Signup.route) {
                SignupScreen(
                    userViewModel = userViewModel,
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) // Clear back stack
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            
            // Main App Screens (only accessible when authenticated)
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = mangaViewModel,
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
                    viewModel = mangaViewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Explore.route) { backStackEntry ->
                // Clear genre selection when navigating to Explore to always show genre list
                LaunchedEffect(backStackEntry) {
                    mangaViewModel.clearGenreSelection()
                }
                
                ExploreScreen(
                    viewModel = mangaViewModel,
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.TopRated.route) {
                TopRatedListScreen(
                    viewModel = mangaViewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Popular.route) {
                PopularListScreen(
                    viewModel = mangaViewModel,
                    onBackClick = { navController.popBackStack() },
                    onMangaClick = { mangaId ->
                        navController.navigate(Screen.Detail.createRoute(mangaId))
                    }
                )
            }
            
            composable(Screen.Watchlist.route) {
                WatchlistScreen(
                    viewModel = mangaViewModel,
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
                    viewModel = mangaViewModel,
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
                    viewModel = mangaViewModel,
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
                    viewModel = mangaViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
@Composable
public fun MangaBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

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
                        imageVector = if (isSelected)
                            item.selectedIcon
                        else
                            item.unselectedIcon,
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
                onClick = {
                    // 🔴 CAS SPÉCIAL : ANIME → Activity Java
                    if (item.route == Screen.Anime.route) {
                        context.startActivity(
                            Intent(context, AnimeMainActivity::class.java)
                        )
                    } else {
                        onNavigate(item.route)
                    }
                },
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

