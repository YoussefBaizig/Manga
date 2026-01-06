package com.example.myapplication1.data.repository

import com.example.myapplication1.data.local.AppDatabase
import com.example.myapplication1.data.local.entity.WatchlistItem
import com.example.myapplication1.data.model.Manga
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Watchlist operations
 * Handles adding, removing, and retrieving manga from user's watchlist
 */
class WatchlistRepository private constructor(database: AppDatabase) {
    
    private val watchlistDao = database.watchlistDao()
    
    /**
     * Get all watchlist items for a user
     */
    fun getWatchlistByUserId(userId: String): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistByUserId(userId)
    }
    
    /**
     * Get watchlist items (suspend version)
     */
    suspend fun getWatchlistByUserIdSuspend(userId: String): List<WatchlistItem> {
        return watchlistDao.getWatchlistByUserIdSuspend(userId)
    }
    
    /**
     * Check if manga is in user's watchlist
     */
    suspend fun isMangaInWatchlist(userId: String, mangaId: Int): Boolean {
        return watchlistDao.isMangaInWatchlist(userId, mangaId)
    }
    
    /**
     * Add manga to watchlist
     */
    suspend fun addToWatchlist(
        userId: String,
        manga: Manga
    ): Result<Long> {
        return try {
            // Check if already in watchlist
            if (watchlistDao.isMangaInWatchlist(userId, manga.malId)) {
                return Result.failure(IllegalArgumentException("Manga already in watchlist"))
            }
            
            val item = WatchlistItem(
                userId = userId,
                mangaId = manga.malId,
                mangaTitle = manga.title,
                mangaImageUrl = manga.images?.jpg?.imageUrl ?: manga.images?.webp?.imageUrl,
                mangaSynopsis = manga.synopsis,
                mangaScore = manga.score,
                addedAt = System.currentTimeMillis()
            )
            
            val id = watchlistDao.insertWatchlistItem(item)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove manga from watchlist
     */
    suspend fun removeFromWatchlist(
        userId: String,
        mangaId: Int
    ): Result<Unit> {
        return try {
            watchlistDao.deleteWatchlistItem(userId, mangaId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get watchlist count for a user
     */
    suspend fun getWatchlistCount(userId: String): Int {
        return watchlistDao.getWatchlistCount(userId)
    }
    
    /**
     * Clear all watchlist items for a user
     */
    suspend fun clearWatchlist(userId: String): Result<Unit> {
        return try {
            watchlistDao.deleteAllWatchlistItems(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update watchlist item
     */
    suspend fun updateWatchlistItem(item: WatchlistItem): Result<Unit> {
        return try {
            watchlistDao.updateWatchlistItem(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: WatchlistRepository? = null
        
        fun getInstance(database: AppDatabase): WatchlistRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = WatchlistRepository(database)
                INSTANCE = instance
                instance
            }
        }
    }
}

