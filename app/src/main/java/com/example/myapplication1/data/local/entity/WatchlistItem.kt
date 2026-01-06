package com.example.myapplication1.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * WatchlistItem Entity for Room Database
 * Stores manga items in user's watchlist
 * Each user has their own watchlist (one-to-many relationship)
 */
@Entity(
    tableName = "watchlist",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Delete watchlist items when user is deleted
        )
    ],
    indices = [
        Index("userId"),
        Index("mangaId"),
        Index(value = ["userId", "mangaId"], unique = true) // Prevent duplicate entries
    ]
)
data class WatchlistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val mangaId: Int, // MAL ID
    val mangaTitle: String,
    val mangaImageUrl: String?,
    val mangaSynopsis: String?,
    val mangaScore: Double?,
    val addedAt: Long = System.currentTimeMillis()
)

