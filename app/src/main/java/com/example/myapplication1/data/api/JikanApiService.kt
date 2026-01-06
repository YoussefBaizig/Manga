package com.example.myapplication1.data.api

import com.example.myapplication1.data.model.CharacterEntry
import com.example.myapplication1.data.model.JikanResponse
import com.example.myapplication1.data.model.Manga
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Jikan API Service Interface
 * Base URL: https://api.jikan.moe/v4/
 * 
 * Note: Jikan API has rate limiting (3 requests per second for free tier)
 */
interface JikanApiService {
    
    /**
     * Get top manga list
     * @param page Page number (default: 1)
     * @param limit Number of results per page (max: 25)
     * @param filter Filter by type: publishing, upcoming, bypopularity, favorite
     */
    @GET("top/manga")
    suspend fun getTopManga(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("filter") filter: String? = null
    ): Response<JikanResponse<List<Manga>>>
    
    /**
     * Search manga by query
     * @param query Search query string
     * @param page Page number
     * @param limit Number of results per page
     * @param type Filter by type: manga, novel, lightnovel, oneshot, doujin, manhwa, manhua
     * @param status Filter by status: publishing, complete, hiatus, discontinued, upcoming
     * @param orderBy Order by field: mal_id, title, start_date, end_date, chapters, volumes, score, scored_by, rank, popularity, members, favorites
     * @param sort Sort direction: asc, desc
     */
    @GET("manga")
    suspend fun searchManga(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("order_by") orderBy: String? = null,
        @Query("sort") sort: String? = null,
        @Query("genres") genres: String? = null
    ): Response<JikanResponse<List<Manga>>>
    
    /**
     * Get manga details by ID
     * @param id Manga ID (mal_id)
     */
    @GET("manga/{id}/full")
    suspend fun getMangaById(
        @Path("id") id: Int
    ): Response<JikanResponse<Manga>>
    
    /**
     * Get manga characters
     * @param id Manga ID (mal_id)
     */
    @GET("manga/{id}/characters")
    suspend fun getMangaCharacters(
        @Path("id") id: Int
    ): Response<JikanResponse<List<CharacterEntry>>>
    
    /**
     * Get manga recommendations
     * @param id Manga ID (mal_id)
     */
    @GET("manga/{id}/recommendations")
    suspend fun getMangaRecommendations(
        @Path("id") id: Int
    ): Response<JikanResponse<List<MangaRecommendation>>>
    
    /**
     * Get random manga
     */
    @GET("random/manga")
    suspend fun getRandomManga(): Response<JikanResponse<Manga>>
    
    /**
     * Get manga by genre
     * @param genres Comma separated genre IDs
     * @param page Page number
     */
    @GET("manga")
    suspend fun getMangaByGenre(
        @Query("genres") genres: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("order_by") orderBy: String = "score",
        @Query("sort") sort: String = "desc"
    ): Response<JikanResponse<List<Manga>>>
    
    /**
     * Get all manga genres
     */
    @GET("genres/manga")
    suspend fun getMangaGenres(): Response<JikanResponse<List<GenreInfo>>>
}

data class MangaRecommendation(
    val entry: Manga?,
    val votes: Int?
)

data class GenreInfo(
    val mal_id: Int,
    val name: String?,
    val url: String?,
    val count: Int?
)

