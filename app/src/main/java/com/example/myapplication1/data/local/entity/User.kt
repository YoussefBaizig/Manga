package com.example.myapplication1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Entity for Room Database
 * Stores user authentication and profile information
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val passwordHash: String, // Store hashed password, never plain text
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null,
    val isActive: Boolean = true
)

