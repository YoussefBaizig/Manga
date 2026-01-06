package com.example.myapplication1.data.model

import com.google.gson.annotations.SerializedName

/**
 * MangaDex API Response Models
 * API Documentation: https://api.mangadex.org/docs/
 */

// Base response wrapper
data class MangaDexResponse<T>(
    @SerializedName("result") val result: String,
    @SerializedName("response") val response: String,
    @SerializedName("data") val data: T,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("offset") val offset: Int? = null,
    @SerializedName("total") val total: Int? = null
)

// Manga model
data class MangaDexManga(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: MangaDexMangaAttributes,
    @SerializedName("relationships") val relationships: List<MangaDexRelationship>? = null
)

data class MangaDexMangaAttributes(
    @SerializedName("title") val title: Map<String, String>?,
    @SerializedName("altTitles") val altTitles: List<Map<String, String>>?,
    @SerializedName("description") val description: Map<String, String>?,
    @SerializedName("isLocked") val isLocked: Boolean?,
    @SerializedName("links") val links: Map<String, String>?,
    @SerializedName("originalLanguage") val originalLanguage: String?,
    @SerializedName("lastVolume") val lastVolume: String?,
    @SerializedName("lastChapter") val lastChapter: String?,
    @SerializedName("publicationDemographic") val publicationDemographic: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("contentRating") val contentRating: String?,
    @SerializedName("tags") val tags: List<MangaDexTag>?,
    @SerializedName("state") val state: String?,
    @SerializedName("chapterNumbersResetOnNewVolume") val chapterNumbersResetOnNewVolume: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("version") val version: Int?
)

data class MangaDexTag(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: MangaDexTagAttributes
)

data class MangaDexTagAttributes(
    @SerializedName("name") val name: Map<String, String>?,
    @SerializedName("description") val description: Map<String, String>?,
    @SerializedName("group") val group: String?,
    @SerializedName("version") val version: Int?
)

data class MangaDexRelationship(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: MangaDexRelationshipAttributes? = null
)

data class MangaDexRelationshipAttributes(
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("locale") val locale: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("volume") val volume: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("title") val title: String?
)

// Chapter model
data class MangaDexChapter(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: MangaDexChapterAttributes,
    @SerializedName("relationships") val relationships: List<MangaDexRelationship>? = null
)

data class MangaDexChapterAttributes(
    @SerializedName("volume") val volume: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("translatedLanguage") val translatedLanguage: String?,
    @SerializedName("externalUrl") val externalUrl: String?,
    @SerializedName("publishAt") val publishAt: String?,
    @SerializedName("readableAt") val readableAt: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("pages") val pages: Int?,
    @SerializedName("version") val version: Int?
)

// Chapter pages
data class MangaDexChapterPages(
    @SerializedName("result") val result: String,
    @SerializedName("baseUrl") val baseUrl: String,
    @SerializedName("chapter") val chapter: MangaDexChapterData
)

data class MangaDexChapterData(
    @SerializedName("hash") val hash: String,
    @SerializedName("data") val data: List<String>,
    @SerializedName("dataSaver") val dataSaver: List<String>? = null
)

// Tag list response
data class MangaDexTagList(
    @SerializedName("result") val result: String,
    @SerializedName("response") val response: String,
    @SerializedName("data") val data: List<MangaDexTag>,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("total") val total: Int?
)

// Extension functions for convenience
fun MangaDexManga.getTitle(): String {
    return attributes.title?.get("en") 
        ?: attributes.title?.values?.firstOrNull()
        ?: attributes.altTitles?.firstOrNull()?.values?.firstOrNull()
        ?: "Untitled"
}

fun MangaDexManga.getDescription(): String? {
    return attributes.description?.get("en")
        ?: attributes.description?.values?.firstOrNull()
}

fun MangaDexManga.getCoverUrl(relationships: List<MangaDexRelationship>?): String? {
    val coverRel = relationships?.find { it.type == "cover_art" }
    return coverRel?.attributes?.fileName?.let { fileName ->
        "https://uploads.mangadex.org/covers/${this.id}/$fileName"
    }
}

fun MangaDexTag.getName(): String {
    return attributes.name?.get("en") 
        ?: attributes.name?.values?.firstOrNull()
        ?: "Unknown"
}

fun MangaDexChapter.getPageUrl(baseUrl: String, hash: String, page: String, dataSaver: Boolean = false): String {
    val quality = if (dataSaver) "data-saver" else "data"
    return "$baseUrl/$quality/$hash/$page"
}

