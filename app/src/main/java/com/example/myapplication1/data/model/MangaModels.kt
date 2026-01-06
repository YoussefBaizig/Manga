package com.example.myapplication1.data.model

import com.google.gson.annotations.SerializedName

/**
 * Jikan API Response Models
 * API Documentation: https://docs.api.jikan.moe/
 * 
 * MERGED: Contains models from both Manga-Mobile and MyApplication1
 * - Added hentai filtering functionality from MyApplication1
 */

// Base response wrapper
data class JikanResponse<T>(
    @SerializedName("data") val data: T,
    @SerializedName("pagination") val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("last_visible_page") val lastVisiblePage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("items") val items: PaginationItems? = null
)

data class PaginationItems(
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("per_page") val perPage: Int
)

// Manga model
data class Manga(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("url") val url: String?,
    @SerializedName("images") val images: MangaImages?,
    @SerializedName("approved") val approved: Boolean?,
    @SerializedName("titles") val titles: List<MangaTitle>?,
    @SerializedName("title") val title: String,
    @SerializedName("title_english") val titleEnglish: String?,
    @SerializedName("title_japanese") val titleJapanese: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("chapters") val chapters: Int?,
    @SerializedName("volumes") val volumes: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("publishing") val publishing: Boolean?,
    @SerializedName("published") val published: Published?,
    @SerializedName("score") val score: Double?,
    @SerializedName("scored_by") val scoredBy: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("popularity") val popularity: Int?,
    @SerializedName("members") val members: Int?,
    @SerializedName("favorites") val favorites: Int?,
    @SerializedName("synopsis") val synopsis: String?,
    @SerializedName("background") val background: String?,
    @SerializedName("authors") val authors: List<MangaAuthor>?,
    @SerializedName("serializations") val serializations: List<MangaSerialization>?,
    @SerializedName("genres") val genres: List<MangaGenre>?,
    @SerializedName("themes") val themes: List<MangaGenre>?,
    @SerializedName("demographics") val demographics: List<MangaGenre>?
)

data class MangaImages(
    @SerializedName("jpg") val jpg: ImageUrls?,
    @SerializedName("webp") val webp: ImageUrls?
)

data class ImageUrls(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("small_image_url") val smallImageUrl: String?,
    @SerializedName("large_image_url") val largeImageUrl: String?
)

data class MangaTitle(
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?
)

data class Published(
    @SerializedName("from") val from: String?,
    @SerializedName("to") val to: String?,
    @SerializedName("string") val string: String?
)

data class MangaAuthor(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

data class MangaSerialization(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

data class MangaGenre(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

// Character model
data class Character(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("url") val url: String?,
    @SerializedName("images") val images: CharacterImages?,
    @SerializedName("name") val name: String?,
    @SerializedName("role") val role: String?
)

data class CharacterImages(
    @SerializedName("jpg") val jpg: CharacterImageUrls?,
    @SerializedName("webp") val webp: CharacterImageUrls?
)

data class CharacterImageUrls(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("small_image_url") val smallImageUrl: String?
)

data class CharacterEntry(
    @SerializedName("character") val character: Character?,
    @SerializedName("role") val role: String?
)

// Extension functions for convenience
fun Manga.getImageUrl(): String? {
    return images?.jpg?.largeImageUrl 
        ?: images?.jpg?.imageUrl 
        ?: images?.webp?.largeImageUrl 
        ?: images?.webp?.imageUrl
}

fun Manga.getSmallImageUrl(): String? {
    return images?.jpg?.smallImageUrl 
        ?: images?.jpg?.imageUrl 
        ?: images?.webp?.smallImageUrl 
        ?: images?.webp?.imageUrl
}

fun Manga.getDisplayTitle(): String {
    return titleEnglish ?: title
}

fun Manga.getGenreNames(): List<String> {
    return genres?.mapNotNull { it.name } ?: emptyList()
}

fun Manga.getAuthorNames(): List<String> {
    return authors?.mapNotNull { it.name } ?: emptyList()
}

// ========== Content Filtering (from MyApplication1) ==========

/**
 * Check if manga has Hentai genre - filters out adult content
 */
fun Manga.hasHentaiGenre(): Boolean {
    val genreNames = getGenreNames()
    val hentaiKeywords = listOf(
        "Hentai",
        "Erotica",
        "Ecchi",
        "Adult",
        "Pornographic",
        "Sexual Content"
    )
    return genreNames.any { genreName ->
        hentaiKeywords.any { keyword ->
            genreName.equals(keyword, ignoreCase = true) ||
            genreName.contains(keyword, ignoreCase = true)
        }
    }
}

/**
 * Filter out hentai manga from a list
 */
fun List<Manga>.filterHentai(): List<Manga> {
    return this.filter { !it.hasHentaiGenre() }
}

