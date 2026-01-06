package com.example.myapplication1.data.api

import com.example.myapplication1.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * MangaDex API Service Interface
 * Base URL: https://api.mangadex.org/
 * 
 * Note: MangaDex API requires proper error handling with X-Request-ID
 */
interface MangaDexApiService {
    
    /**
     * Search manga by title
     * @param title Search query
     * @param limit Number of results (max 100)
     * @param offset Pagination offset
     * @param includedTags Comma-separated tag IDs to include
     * @param excludedTags Comma-separated tag IDs to exclude
     * @param contentRating Content rating filter (safe, suggestive, erotica, pornographic)
     * @param order Order by field (latestUploadedChapter, relevance, rating, followedCount, createdAt, updatedAt, title)
     */
    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includedTags[]") includedTags: List<String>? = null,
        @Query("excludedTags[]") excludedTags: List<String>? = null,
        @Query("contentRating[]") contentRating: List<String>? = null,
        @Query("order[title]") orderTitle: String? = null,
        @Query("order[relevance]") orderRelevance: String? = null,
        @Query("order[rating]") orderRating: String? = null,
        @Query("order[followedCount]") orderFollowedCount: String? = null,
        @Query("order[createdAt]") orderCreatedAt: String? = null,
        @Query("order[updatedAt]") orderUpdatedAt: String? = null,
        @Query("includes[]") includes: List<String>? = listOf("cover_art", "author", "artist")
    ): Response<MangaDexResponse<List<MangaDexManga>>>
    
    /**
     * Get manga by ID
     * @param id Manga ID
     */
    @GET("manga/{id}")
    suspend fun getMangaById(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String>? = listOf("cover_art", "author", "artist", "tag")
    ): Response<MangaDexResponse<MangaDexManga>>
    
    /**
     * Get manga chapters
     * @param mangaId Manga ID
     * @param limit Number of results
     * @param offset Pagination offset
     * @param translatedLanguage Language code (e.g., "en")
     * @param order Order by (asc, desc)
     */
    @GET("manga/{id}/feed")
    suspend fun getMangaChapters(
        @Path("id") mangaId: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("translatedLanguage[]") translatedLanguage: List<String>? = listOf("en"),
        @Query("order[chapter]") order: String? = "asc",
        @Query("includes[]") includes: List<String>? = listOf("scanlation_group", "user")
    ): Response<MangaDexResponse<List<MangaDexChapter>>>
    
    /**
     * Get chapter by ID
     * @param chapterId Chapter ID
     */
    @GET("chapter/{id}")
    suspend fun getChapterById(
        @Path("id") chapterId: String,
        @Query("includes[]") includes: List<String>? = listOf("scanlation_group", "user", "manga")
    ): Response<MangaDexResponse<MangaDexChapter>>
    
    /**
     * Get chapter pages/images
     * @param chapterId Chapter ID
     */
    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(
        @Path("chapterId") chapterId: String
    ): Response<MangaDexChapterPages>
    
    /**
     * Get all tags
     */
    @GET("manga/tag")
    suspend fun getTags(): Response<MangaDexTagList>
}

