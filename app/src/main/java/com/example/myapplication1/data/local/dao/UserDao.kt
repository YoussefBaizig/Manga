package com.example.myapplication1.data.local.dao

import androidx.room.*
import com.example.myapplication1.data.local.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User operations
 * Provides CRUD operations for User entity
 */
@Dao
interface UserDao {
    
    /**
     * Get all users
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>
    
    /**
     * Get user by ID (suspend function for one-time access)
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSuspend(userId: String): User?
    
    /**
     * Get user by username
     */
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Get user by email
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    /**
     * Check if username exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun usernameExists(username: String): Boolean
    
    /**
     * Check if email exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean
    
    /**
     * Authenticate user (check username/email and password hash)
     */
    @Query("SELECT * FROM users WHERE (username = :identifier OR email = :identifier) AND passwordHash = :passwordHash AND isActive = 1")
    suspend fun authenticateUser(identifier: String, passwordHash: String): User?
    
    /**
     * Insert user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    /**
     * Insert multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    /**
     * Update user
     */
    @Update
    suspend fun updateUser(user: User)
    
    /**
     * Update last login time
     */
    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)
    
    /**
     * Delete user
     */
    @Delete
    suspend fun deleteUser(user: User)
    
    /**
     * Delete user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    /**
     * Get user count
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

