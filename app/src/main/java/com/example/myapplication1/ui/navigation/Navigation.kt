package com.example.myapplication1.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Explore : Screen("explore")
    data object Detail : Screen("detail/{mangaId}") {
        fun createRoute(mangaId: Int) = "detail/$mangaId"
    }
    data object Genre : Screen("genre/{genreId}/{genreName}") {
        fun createRoute(genreId: Int, genreName: String) = "genre/$genreId/$genreName"
    }
    data object ChaptersList : Screen("chapters/{mangaDexId}/{mangaTitle}") {
        fun createRoute(mangaDexId: String, mangaTitle: String) = "chapters/$mangaDexId/${mangaTitle.replace("/", "_")}"
    }
    data object ChapterReader : Screen("reader/{chapterId}/{chapterTitle}") {
        fun createRoute(chapterId: String, chapterTitle: String) = "reader/$chapterId/${chapterTitle.replace("/", "_")}"
    }
    data object TopRated : Screen("top_rated")
    data object Popular : Screen("popular")
    data object Watchlist : Screen("watchlist")
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object FirstPage : Screen("first_page")
}

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Search : BottomNavItem(
        route = Screen.Search.route,
        title = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    data object Explore : BottomNavItem(
        route = Screen.Explore.route,
        title = "Browse",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )
    
    data object Watchlist : BottomNavItem(
        route = Screen.Watchlist.route,
        title = "Watchlist",
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Outlined.Star
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Search,
    BottomNavItem.Explore,
    BottomNavItem.Watchlist
)

