package com.example.myapplication1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication1.data.local.dao.UserDao
import com.example.myapplication1.data.local.dao.WatchlistDao
import com.example.myapplication1.data.local.entity.User
import com.example.myapplication1.data.local.entity.WatchlistItem

/**
 * Room Database for the application
 * Manages User and Watchlist data persistence
 */
@Database(
    entities = [User::class, WatchlistItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun watchlistDao(): WatchlistDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "manga_app_database"
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Migration example (for future schema changes)
         * Uncomment and modify when you need to update the database schema
         */
        /*
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration SQL here
                // database.execSQL("ALTER TABLE users ADD COLUMN new_column TEXT")
            }
        }
        */
    }
}

