package com.example.myapplication1.data.repository

import com.example.myapplication1.data.api.GenreInfo
import com.example.myapplication1.data.api.JikanApiService
import com.example.myapplication1.data.api.MangaDexApiService
import com.example.myapplication1.data.api.MangaRecommendation
import com.example.myapplication1.data.model.*
import com.example.myapplication1.data.network.ApiResult
import com.example.myapplication1.data.network.NetworkModule
import com.example.myapplication1.data.network.safeApiCall

/**
 * Manga Repository - Single source of truth for manga data
 * Uses Jikan API for search/info and MangaDex API for chapters/reading
 */
class MangaRepository {
    
    private val jikanApiService: JikanApiService = NetworkModule.createJikanService()
    private val mangadexApiService: MangaDexApiService = NetworkModule.createMangaDexService()
    
    /**
     * Get top manga list
     */
    suspend fun getTopManga(
        page: Int = 1,
        limit: Int = 25,
        filter: String? = null
    ): ApiResult<Pair<List<Manga>, Pagination?>> {
        return when (val result = safeApiCall { jikanApiService.getTopManga(page, limit, filter) }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = Pair(result.data.data, result.data.pagination),
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Search manga
     */
    suspend fun searchManga(
        query: String,
        page: Int = 1,
        limit: Int = 25,
        type: String? = null,
        status: String? = null,
        orderBy: String? = null,
        sort: String? = null,
        genres: String? = null
    ): ApiResult<Pair<List<Manga>, Pagination?>> {
        return when (val result = safeApiCall { 
            jikanApiService.searchManga(query, page, limit, type, status, orderBy, sort, genres) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = Pair(result.data.data, result.data.pagination),
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga details by ID
     */
    suspend fun getMangaById(id: Int): ApiResult<Manga> {
        return when (val result = safeApiCall { jikanApiService.getMangaById(id) }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga characters
     */
    suspend fun getMangaCharacters(id: Int): ApiResult<List<CharacterEntry>> {
        return when (val result = safeApiCall { jikanApiService.getMangaCharacters(id) }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga recommendations
     */
    suspend fun getMangaRecommendations(id: Int): ApiResult<List<MangaRecommendation>> {
        return when (val result = safeApiCall { jikanApiService.getMangaRecommendations(id) }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get random manga
     */
    suspend fun getRandomManga(): ApiResult<Manga> {
        return when (val result = safeApiCall { jikanApiService.getRandomManga() }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga by genre
     */
    suspend fun getMangaByGenre(
        genreIds: String,
        page: Int = 1,
        limit: Int = 25
    ): ApiResult<Pair<List<Manga>, Pagination?>> {
        return when (val result = safeApiCall { 
            jikanApiService.getMangaByGenre(genreIds, page, limit) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = Pair(result.data.data, result.data.pagination),
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get all manga genres
     */
    suspend fun getMangaGenres(): ApiResult<List<GenreInfo>> {
        return when (val result = safeApiCall { jikanApiService.getMangaGenres() }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    // ========== MangaDex API Methods ==========
    
    /**
     * Search manga on MangaDex by title
     */
    suspend fun searchMangaDex(
        title: String? = null,
        limit: Int = 20,
        offset: Int = 0,
        includedTagIds: List<String>? = null,
        excludedTagIds: List<String>? = null,
        contentRating: List<String>? = listOf("safe", "suggestive")
    ): ApiResult<Pair<List<MangaDexManga>, Int?>> {
        return when (val result = safeApiCall { 
            mangadexApiService.searchManga(
                title = title,
                limit = limit,
                offset = offset,
                includedTags = includedTagIds,
                excludedTags = excludedTagIds,
                contentRating = contentRating
            ) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = Pair(result.data.data, result.data.total),
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga details from MangaDex
     */
    suspend fun getMangaDexById(id: String): ApiResult<MangaDexManga> {
        return when (val result = safeApiCall { 
            mangadexApiService.getMangaById(id) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get manga chapters from MangaDex
     */
    suspend fun getMangaDexChapters(
        mangaId: String,
        limit: Int = 100,
        offset: Int = 0,
        language: List<String> = listOf("en")
    ): ApiResult<Pair<List<MangaDexChapter>, Int?>> {
        return when (val result = safeApiCall { 
            mangadexApiService.getMangaChapters(mangaId, limit, offset, language) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = Pair(result.data.data, result.data.total),
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get chapter pages from MangaDex
     */
    suspend fun getChapterPages(chapterId: String): ApiResult<MangaDexChapterPages> {
        return when (val result = safeApiCall { 
            mangadexApiService.getChapterPages(chapterId) 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get all MangaDex tags
     */
    suspend fun getMangaDexTags(): ApiResult<List<MangaDexTag>> {
        return when (val result = safeApiCall { 
            mangadexApiService.getTags() 
        }) {
            is ApiResult.Success -> {
                ApiResult.Success(
                    data = result.data.data,
                    requestId = result.requestId
                )
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
    
    /**
     * Get tag IDs by names (for filtering)
     */
    suspend fun getTagIdsByNames(
        includedNames: List<String>,
        excludedNames: List<String>
    ): ApiResult<Pair<List<String>, List<String>>> {
        return when (val tagsResult = getMangaDexTags()) {
            is ApiResult.Success -> {
                val tags = tagsResult.data
                val includedIds = tags
                    .filter { tag -> includedNames.any { name -> 
                        tag.getName().equals(name, ignoreCase = true) 
                    }}
                    .map { it.id }
                val excludedIds = tags
                    .filter { tag -> excludedNames.any { name -> 
                        tag.getName().equals(name, ignoreCase = true) 
                    }}
                    .map { it.id }
                ApiResult.Success(
                    data = Pair(includedIds, excludedIds),
                    requestId = tagsResult.requestId
                )
            }
            is ApiResult.Error -> tagsResult
            is ApiResult.Loading -> tagsResult
        }
    }
    
    companion object {
        @Volatile
        private var instance: MangaRepository? = null
        
        fun getInstance(): MangaRepository {
            return instance ?: synchronized(this) {
                instance ?: MangaRepository().also { instance = it }
            }
        }
    }
}

