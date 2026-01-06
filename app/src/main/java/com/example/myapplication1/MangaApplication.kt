package com.example.myapplication1

import android.app.Application
import com.example.myapplication1.data.local.AppDatabase

/**
 * Application class to initialize database and other app-wide components
 */
class MangaApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize database immediately so it's available in Database Inspector
        // This creates the database file on app startup
        database.openHelper.writableDatabase
    }
}

