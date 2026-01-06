package com.example.myapplication1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication1.MangaApplication
import com.example.myapplication1.data.repository.UserRepository
import com.example.myapplication1.data.repository.WatchlistRepository

/**
 * ViewModel Factory for dependency injection
 * Provides ViewModels with required dependencies
 */
class ViewModelFactory(
    private val userRepository: UserRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MangaViewModel::class.java) -> {
                MangaViewModel(watchlistRepository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
    
    companion object {
        /**
         * Create factory from Application context
         */
        fun create(application: MangaApplication): ViewModelFactory {
            val database = application.database
            val userRepository = UserRepository.getInstance(database)
            val watchlistRepository = WatchlistRepository.getInstance(database)
            return ViewModelFactory(userRepository, watchlistRepository)
        }
    }
}

