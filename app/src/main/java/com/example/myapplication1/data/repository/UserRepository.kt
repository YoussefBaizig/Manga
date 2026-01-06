package com.example.myapplication1.data.repository

import com.example.myapplication1.data.local.AppDatabase
import com.example.myapplication1.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

/**
 * Repository for User operations and authentication
 * Handles user registration, login, and CRUD operations
 */
class UserRepository private constructor(database: AppDatabase) {
    
    private val userDao = database.userDao()
    
    /**
     * Register a new user
     * @param username Unique username
     * @param email Unique email
     * @param password Plain text password (will be hashed)
     * @return Result with User or error message
     */
    suspend fun registerUser(
        username: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            // Validate input
            if (username.isBlank()) {
                return Result.failure(IllegalArgumentException("Username cannot be empty"))
            }
            if (email.isBlank() || !email.contains("@")) {
                return Result.failure(IllegalArgumentException("Invalid email address"))
            }
            if (password.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }
            
            // Check if username already exists
            if (userDao.usernameExists(username)) {
                return Result.failure(IllegalArgumentException("Username already exists"))
            }
            
            // Check if email already exists
            if (userDao.emailExists(email)) {
                return Result.failure(IllegalArgumentException("Email already registered"))
            }
            
            // Create new user
            val userId = UUID.randomUUID().toString()
            val passwordHash = hashPassword(password)
            val user = User(
                id = userId,
                username = username,
                email = email,
                passwordHash = passwordHash,
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            
            // Insert user
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login user
     * @param identifier Username or email
     * @param password Plain text password
     * @return Result with User or error message
     */
    suspend fun loginUser(
        identifier: String,
        password: String
    ): Result<User> {
        return try {
            val passwordHash = hashPassword(password)
            val user = userDao.authenticateUser(identifier, passwordHash)
            
            if (user != null) {
                // Update last login time
                userDao.updateLastLogin(user.id, System.currentTimeMillis())
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Invalid username/email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user by ID
     */
    fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }
    
    /**
     * Get all users
     */
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    /**
     * Update user
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Change user password
     */
    suspend fun changePassword(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val user = userDao.getUserByIdSuspend(userId)
                ?: return Result.failure(IllegalArgumentException("User not found"))
            
            // Verify old password
            val oldPasswordHash = hashPassword(oldPassword)
            if (user.passwordHash != oldPasswordHash) {
                return Result.failure(IllegalArgumentException("Incorrect password"))
            }
            
            // Validate new password
            if (newPassword.length < 6) {
                return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
            }
            
            // Update password
            val updatedUser = user.copy(passwordHash = hashPassword(newPassword))
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete user
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUserById(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if username exists
     */
    suspend fun usernameExists(username: String): Boolean {
        return userDao.usernameExists(username)
    }
    
    /**
     * Check if email exists
     */
    suspend fun emailExists(email: String): Boolean {
        return userDao.emailExists(email)
    }
    
    /**
     * Hash password using SHA-256
     * Note: For production, use bcrypt or Argon2 instead
     */
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null
        
        fun getInstance(database: AppDatabase): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(database)
                INSTANCE = instance
                instance
            }
        }
    }
}

