package com.example.myapplication1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.api.GenreInfo
import com.example.myapplication1.data.local.entity.WatchlistItem
import com.example.myapplication1.data.model.*
import com.example.myapplication1.data.network.ApiResult
import com.example.myapplication1.data.repository.MangaRepository
import com.example.myapplication1.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * UI State for manga lists
 */
data class MangaListState(
    val mangaList: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pagination: Pagination? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = false
)

/**
 * UI State for search
 */
data class SearchState(
    val query: String = "",
    val results: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val pagination: Pagination? = null
)

/**
 * UI State for manga detail
 */
data class MangaDetailState(
    val manga: Manga? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI State for genres
 */
data class GenreState(
    val genres: List<GenreInfo> = emptyList(),
    val selectedGenre: GenreInfo? = null,
    val genreManga: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Main ViewModel for manga operations
 * 
 * MERGED: Contains features from both Manga-Mobile and MyApplication1:
 * - Watchlist functionality (from Manga-Mobile)
 * - Content filtering / hentai filter (from MyApplication1)
 * - Genre selection clearing (from Manga-Mobile)
 */
class MangaViewModel(
    private val watchlistRepository: WatchlistRepository? = null
) : ViewModel() {
    
    private val repository = MangaRepository.getInstance()
    
    // Current user ID for watchlist operations
    private var currentUserId: String? = null
    
    // Top manga state
    private val _topMangaState = MutableStateFlow(MangaListState())
    val topMangaState: StateFlow<MangaListState> = _topMangaState.asStateFlow()
    
    // Popular manga state
    private val _popularMangaState = MutableStateFlow(MangaListState())
    val popularMangaState: StateFlow<MangaListState> = _popularMangaState.asStateFlow()
    
    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    // Detail state
    private val _detailState = MutableStateFlow(MangaDetailState())
    val detailState: StateFlow<MangaDetailState> = _detailState.asStateFlow()
    
    // Genre state
    private val _genreState = MutableStateFlow(GenreState())
    val genreState: StateFlow<GenreState> = _genreState.asStateFlow()
    
    // Random manga state
    private val _randomManga = MutableStateFlow<Manga?>(null)
    val randomManga: StateFlow<Manga?> = _randomManga.asStateFlow()
    
    // MangaDex states
    private val _mangadexChaptersState = MutableStateFlow<Pair<List<MangaDexChapter>, Int?>>(Pair(emptyList(), null))
    val mangadexChaptersState: StateFlow<Pair<List<MangaDexChapter>, Int?>> = _mangadexChaptersState.asStateFlow()
    
    private val _chapterPagesState = MutableStateFlow(ChapterPagesState())
    val chapterPagesState: StateFlow<ChapterPagesState> = _chapterPagesState.asStateFlow()
    
    private val _mangadexTagsState = MutableStateFlow<List<MangaDexTag>>(emptyList())
    val mangadexTagsState: StateFlow<List<MangaDexTag>> = _mangadexTagsState.asStateFlow()
    
    // ========== Watchlist (from Manga-Mobile) ==========
    
    // Watchlist state - stored as Set for quick lookup (for backward compatibility)
    private val _watchlistIds = MutableStateFlow<Set<Int>>(emptySet())
    val watchlistIds: StateFlow<Set<Int>> = _watchlistIds.asStateFlow()
    
    // Watchlist manga items (from database)
    private val _watchlistManga = MutableStateFlow<List<Manga>>(emptyList())
    val watchlistManga: StateFlow<List<Manga>> = _watchlistManga.asStateFlow()
    
    // Watchlist items from database
    private val _watchlistItems = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlistItems: StateFlow<List<WatchlistItem>> = _watchlistItems.asStateFlow()
    
    init {
        loadTopManga()
        loadPopularManga()
        loadGenres()
        loadMangaDexTags()
    }
    
    /**
     * Load top manga (with content filtering from MyApplication1)
     */
    fun loadTopManga(page: Int = 1) {
        viewModelScope.launch {
            _topMangaState.value = _topMangaState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getTopManga(page = page)) {
                is ApiResult.Success -> {
                    val (mangaList, pagination) = result.data
                    // Filter out hentai manga (from MyApplication1)
                    val filteredManga = mangaList.filterHentai()
                    _topMangaState.value = _topMangaState.value.copy(
                        mangaList = if (page == 1) filteredManga else _topMangaState.value.mangaList + filteredManga,
                        isLoading = false,
                        pagination = pagination,
                        currentPage = page,
                        canLoadMore = pagination?.hasNextPage ?: false
                    )
                }
                is ApiResult.Error -> {
                    _topMangaState.value = _topMangaState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Load popular manga (with content filtering from MyApplication1)
     */
    fun loadPopularManga(page: Int = 1) {
        viewModelScope.launch {
            _popularMangaState.value = _popularMangaState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getTopManga(page = page, filter = "bypopularity")) {
                is ApiResult.Success -> {
                    val (mangaList, pagination) = result.data
                    // Filter out hentai manga (from MyApplication1)
                    val filteredManga = mangaList.filterHentai()
                    _popularMangaState.value = _popularMangaState.value.copy(
                        mangaList = if (page == 1) filteredManga else _popularMangaState.value.mangaList + filteredManga,
                        isLoading = false,
                        pagination = pagination,
                        currentPage = page,
                        canLoadMore = pagination?.hasNextPage ?: false
                    )
                }
                is ApiResult.Error -> {
                    _popularMangaState.value = _popularMangaState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Search manga (with content filtering from MyApplication1)
     */
    fun searchManga(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState()
            return
        }
        
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                query = query,
                isLoading = true,
                error = null,
                hasSearched = true
            )
            
            when (val result = repository.searchManga(query = query)) {
                is ApiResult.Success -> {
                    val (mangaList, pagination) = result.data
                    // Filter out hentai manga (from MyApplication1)
                    val filteredManga = mangaList.filterHentai()
                    _searchState.value = _searchState.value.copy(
                        results = filteredManga,
                        isLoading = false,
                        pagination = pagination
                    )
                }
                is ApiResult.Error -> {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Clear search
     */
    fun clearSearch() {
        _searchState.value = SearchState()
    }
    
    /**
     * Load manga details
     */
    fun loadMangaDetails(id: Int) {
        viewModelScope.launch {
            _detailState.value = MangaDetailState(isLoading = true)
            
            when (val result = repository.getMangaById(id)) {
                is ApiResult.Success -> {
                    _detailState.value = MangaDetailState(
                        manga = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _detailState.value = MangaDetailState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Clear manga details
     */
    fun clearMangaDetails() {
        _detailState.value = MangaDetailState()
    }
    
    /**
     * Load genres
     */
    fun loadGenres() {
        viewModelScope.launch {
            _genreState.value = _genreState.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getMangaGenres()) {
                is ApiResult.Success -> {
                    _genreState.value = _genreState.value.copy(
                        genres = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _genreState.value = _genreState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Select a genre and load manga for it (with content filtering from MyApplication1)
     */
    fun selectGenre(genre: GenreInfo) {
        viewModelScope.launch {
            _genreState.value = _genreState.value.copy(
                selectedGenre = genre,
                isLoading = true,
                error = null
            )
            
            when (val result = repository.getMangaByGenre(genre.mal_id.toString())) {
                is ApiResult.Success -> {
                    val (mangaList, _) = result.data
                    // Filter out hentai manga (from MyApplication1)
                    val filteredManga = mangaList.filterHentai()
                    _genreState.value = _genreState.value.copy(
                        genreManga = filteredManga,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _genreState.value = _genreState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Clear genre selection and return to genre list (from Manga-Mobile)
     */
    fun clearGenreSelection() {
        _genreState.value = _genreState.value.copy(
            selectedGenre = null,
            genreManga = emptyList(),
            error = null
        )
    }
    
    /**
     * Get random manga
     */
    fun loadRandomManga() {
        viewModelScope.launch {
            when (val result = repository.getRandomManga()) {
                is ApiResult.Success -> {
                    _randomManga.value = result.data
                }
                is ApiResult.Error -> {
                    // Silently fail for random manga
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Load more top manga (pagination)
     */
    fun loadMoreTopManga() {
        val currentState = _topMangaState.value
        if (!currentState.isLoading && currentState.canLoadMore) {
            loadTopManga(currentState.currentPage + 1)
        }
    }
    
    /**
     * Load more popular manga (pagination)
     */
    fun loadMorePopularManga() {
        val currentState = _popularMangaState.value
        if (!currentState.isLoading && currentState.canLoadMore) {
            loadPopularManga(currentState.currentPage + 1)
        }
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadTopManga()
        loadPopularManga()
        loadGenres()
        loadMangaDexTags()
    }
    
    // ========== MangaDex Methods ==========
    
    /**
     * Load manga chapters from MangaDex
     */
    fun loadMangaDexChapters(mangaId: String, language: List<String> = listOf("en")) {
        viewModelScope.launch {
            when (val result = repository.getMangaDexChapters(mangaId, language = language)) {
                is ApiResult.Success -> {
                    _mangadexChaptersState.value = result.data
                }
                is ApiResult.Error -> {
                    // Handle error if needed
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Load chapter pages
     */
    fun loadChapterPages(chapterId: String) {
        viewModelScope.launch {
            _chapterPagesState.value = ChapterPagesState(isLoading = true)
            
            when (val result = repository.getChapterPages(chapterId)) {
                is ApiResult.Success -> {
                    _chapterPagesState.value = ChapterPagesState(
                        pages = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _chapterPagesState.value = ChapterPagesState(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Clear chapter pages
     */
    fun clearChapterPages() {
        _chapterPagesState.value = ChapterPagesState()
    }
    
    /**
     * Load MangaDex tags
     */
    fun loadMangaDexTags() {
        viewModelScope.launch {
            when (val result = repository.getMangaDexTags()) {
                is ApiResult.Success -> {
                    _mangadexTagsState.value = result.data
                }
                is ApiResult.Error -> {
                    // Silently fail - tags are optional
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Search MangaDex with tag filtering
     */
    fun searchMangaDex(
        title: String? = null,
        includedTagNames: List<String> = emptyList(),
        excludedTagNames: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val (includedIds, excludedIds) = if (includedTagNames.isNotEmpty() || excludedTagNames.isNotEmpty()) {
                when (val tagResult = repository.getTagIdsByNames(includedTagNames, excludedTagNames)) {
                    is ApiResult.Success -> tagResult.data
                    else -> Pair(emptyList(), emptyList())
                }
            } else {
                Pair(null, null)
            }
            
            when (val result = repository.searchMangaDex(
                title = title,
                includedTagIds = includedIds,
                excludedTagIds = excludedIds
            )) {
                is ApiResult.Success -> {
                    // Store results - you can add a state for this
                }
                is ApiResult.Error -> {
                    // Handle error
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    /**
     * Search MangaDex by title for reading - finds first match
     * Automatically excludes hentai/erotic content (from MyApplication1)
     */
    fun searchMangaDexForReading(
        title: String,
        onFound: (String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.searchMangaDex(
                title = title,
                limit = 5, // Only need first few results
                contentRating = listOf("safe", "suggestive") // Force safe content only (from MyApplication1)
            )) {
                is ApiResult.Success -> {
                    // Filter out any hentai that might have slipped through (from MyApplication1)
                    val safeManga = result.data.first.firstOrNull { manga ->
                        manga.attributes.contentRating?.lowercase() !in listOf("erotica", "pornographic") &&
                        !manga.attributes.tags.orEmpty().any { tag ->
                            val tagName = tag.getName().lowercase()
                            tagName.contains("hentai") || tagName.contains("erotica") ||
                            tagName.contains("sexual content") || tagName.contains("pornographic")
                        }
                    }
                    onFound(safeManga?.id)
                }
                is ApiResult.Error -> {
                    onFound(null)
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    // ========== Watchlist Methods (from Manga-Mobile) ==========
    
    /**
     * Set current user ID for watchlist operations
     */
    fun setCurrentUserId(userId: String?) {
        currentUserId = userId
        if (userId != null && watchlistRepository != null) {
            // Observe watchlist changes
            viewModelScope.launch {
                watchlistRepository.getWatchlistByUserId(userId).collect { items ->
                    _watchlistItems.value = items
                    _watchlistIds.value = items.map { it.mangaId }.toSet()
                    // Load full manga details for watchlist items
                    refreshWatchlistMangaFromItems(items)
                }
            }
        } else {
            _watchlistItems.value = emptyList()
            _watchlistIds.value = emptySet()
            _watchlistManga.value = emptyList()
        }
    }
    
    /**
     * Check if manga is in watchlist (synchronous check using cached state)
     */
    fun isInWatchlist(mangaId: Int): Boolean {
        return mangaId in _watchlistIds.value
    }
    
    /**
     * Check if manga is in watchlist (async, updates state)
     */
    fun checkWatchlistStatus(mangaId: Int) {
        if (watchlistRepository != null && currentUserId != null) {
            viewModelScope.launch {
                val exists = watchlistRepository.isMangaInWatchlist(currentUserId!!, mangaId)
                // Update local state
                if (exists && mangaId !in _watchlistIds.value) {
                    _watchlistIds.value = _watchlistIds.value + mangaId
                } else if (!exists && mangaId in _watchlistIds.value) {
                    _watchlistIds.value = _watchlistIds.value - mangaId
                }
            }
        }
    }
    
    /**
     * Add manga to watchlist
     */
    fun addToWatchlist(manga: Manga) {
        if (watchlistRepository != null && currentUserId != null) {
            // Use database
            viewModelScope.launch {
                val result = watchlistRepository.addToWatchlist(currentUserId!!, manga)
                result.fold(
                    onSuccess = {
                        // State will be updated automatically via Flow
                    },
                    onFailure = {
                        // Handle error if needed
                    }
                )
            }
        } else {
            // Fallback to in-memory storage
            val newIds = _watchlistIds.value + manga.malId
            _watchlistIds.value = newIds
            val currentManga = _watchlistManga.value
            if (manga !in currentManga) {
                _watchlistManga.value = currentManga + manga
            }
        }
    }
    
    /**
     * Remove manga from watchlist
     */
    fun removeFromWatchlist(mangaId: Int) {
        if (watchlistRepository != null && currentUserId != null) {
            // Use database
            viewModelScope.launch {
                val result = watchlistRepository.removeFromWatchlist(currentUserId!!, mangaId)
                result.fold(
                    onSuccess = {
                        // State will be updated automatically via Flow
                    },
                    onFailure = {
                        // Handle error if needed
                    }
                )
            }
        } else {
            // Fallback to in-memory storage
            val newIds = _watchlistIds.value - mangaId
            _watchlistIds.value = newIds
            _watchlistManga.value = _watchlistManga.value.filter { it.malId != mangaId }
        }
    }
    
    /**
     * Load full details for watchlist manga
     */
    fun refreshWatchlistManga() {
        if (watchlistRepository != null && currentUserId != null) {
            // Load from database items
            viewModelScope.launch {
                val items = watchlistRepository.getWatchlistByUserIdSuspend(currentUserId!!)
                refreshWatchlistMangaFromItems(items)
            }
        } else {
            // Fallback to in-memory
            viewModelScope.launch {
                val ids = _watchlistIds.value
                if (ids.isEmpty()) {
                    _watchlistManga.value = emptyList()
                    return@launch
                }
                
                val loadedManga = mutableListOf<Manga>()
                ids.forEach { mangaId ->
                    when (val result = repository.getMangaById(mangaId)) {
                        is ApiResult.Success -> {
                            loadedManga.add(result.data)
                        }
                        else -> {}
                    }
                }
                _watchlistManga.value = loadedManga
            }
        }
    }
    
    /**
     * Refresh watchlist manga from database items
     */
    private suspend fun refreshWatchlistMangaFromItems(items: List<WatchlistItem>) {
        if (items.isEmpty()) {
            _watchlistManga.value = emptyList()
            return
        }
        
        val loadedManga = mutableListOf<Manga>()
        items.forEach { item ->
            when (val result = repository.getMangaById(item.mangaId)) {
                is ApiResult.Success -> {
                    loadedManga.add(result.data)
                }
                else -> {}
            }
        }
        _watchlistManga.value = loadedManga
    }
}

/**
 * UI State for chapter pages
 */
data class ChapterPagesState(
    val pages: MangaDexChapterPages? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

