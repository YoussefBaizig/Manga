package com.example.myapplication1.data.local.dao

import androidx.room.*
import com.example.myapplication1.data.local.entity.WatchlistItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Watchlist operations
 * Provides CRUD operations for WatchlistItem entity
 */
@Dao
interface WatchlistDao {
    
    /**
     * Get all watchlist items for a specific user
     */
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistByUserId(userId: String): Flow<List<WatchlistItem>>
    
    /**
     * Get watchlist items for a specific user (suspend function)
     */
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    suspend fun getWatchlistByUserIdSuspend(userId: String): List<WatchlistItem>
    
    /**
     * Check if manga is in user's watchlist
     */
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND mangaId = :mangaId)")
    suspend fun isMangaInWatchlist(userId: String, mangaId: Int): Boolean
    
    /**
     * Get specific watchlist item
     */
    @Query("SELECT * FROM watchlist WHERE userId = :userId AND mangaId = :mangaId")
    suspend fun getWatchlistItem(userId: String, mangaId: Int): WatchlistItem?
    
    /**
     * Get watchlist count for a user
     */
    @Query("SELECT COUNT(*) FROM watchlist WHERE userId = :userId")
    suspend fun getWatchlistCount(userId: String): Int
    
    /**
     * Insert watchlist item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem): Long
    
    /**
     * Insert multiple watchlist items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItems(items: List<WatchlistItem>)
    
    /**
     * Update watchlist item
     */
    @Update
    suspend fun updateWatchlistItem(item: WatchlistItem)
    
    /**
     * Delete watchlist item
     */
    @Delete
    suspend fun deleteWatchlistItem(item: WatchlistItem)
    
    /**
     * Delete watchlist item by user and manga ID
     */
    @Query("DELETE FROM watchlist WHERE userId = :userId AND mangaId = :mangaId")
    suspend fun deleteWatchlistItem(userId: String, mangaId: Int)
    
    /**
     * Delete all watchlist items for a user
     */
    @Query("DELETE FROM watchlist WHERE userId = :userId")
    suspend fun deleteAllWatchlistItems(userId: String)
    
    /**
     * Delete all watchlist items
     */
    @Query("DELETE FROM watchlist")
    suspend fun deleteAllWatchlistItems()
}

