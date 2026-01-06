package com.example.myapplication1.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network Module - Singleton for Retrofit and OkHttp configuration
 * 
 * Implements proper error handling with X-Request-ID logging as recommended
 * by MangaDex security guidelines.
 */
object NetworkModule {
    
    private const val TAG = "MangaNetwork"
    private const val JIKAN_BASE_URL = "https://api.jikan.moe/v4/"
    private const val MANGADEX_BASE_URL = "https://api.mangadex.org/"
    
    /**
     * MangaDex-specific interceptor with proper error handling as per security guidelines
     * Logs X-Request-ID, request body, and response body for errors
     */
    private val mangadexErrorInterceptor = Interceptor { chain ->
        val request = chain.request()
        val requestBody = request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: Exception) {
                "Unable to read request body: ${e.message}"
            }
        } ?: "No request body"
        
        val response = chain.proceed(request)
        val requestId = response.header("X-Request-ID") ?: "N/A"
        
        if (response.code >= 400) {
            val responseBody = try {
                val peekBody = response.peekBody(2048L) // Peek up to 2KB without consuming
                peekBody.string()
            } catch (e: Exception) {
                "Error reading response: ${e.message}"
            }
            
            Log.e(TAG, """
                |
                |══════════════════════════════════════════════════════════════
                |MANGADEX API ERROR
                |══════════════════════════════════════════════════════════════
                |Request ID: $requestId
                |Status: ${response.code} ${response.message}
                |URL: ${request.url}
                |Method: ${request.method}
                |
                |Request:
                |$requestBody
                |
                |Response:
                |$responseBody
                |══════════════════════════════════════════════════════════════
            """.trimMargin())
        } else {
            Log.d(TAG, "MangaDex request successful - ID: $requestId, URL: ${request.url}")
        }
        
        response
    }
    
    /**
     * General interceptor for logging request/response with X-Request-ID
     */
    private val requestIdInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Log X-Request-ID for debugging as recommended
        val requestId = response.header("X-Request-ID") ?: "N/A"
        
        if (!response.isSuccessful) {
            Log.e(TAG, """
                |
                |══════════════════════════════════════════════════════════════
                |REQUEST FAILED
                |══════════════════════════════════════════════════════════════
                |Request ID: $requestId
                |Status: ${response.code} ${response.message}
                |URL: ${request.url}
                |Method: ${request.method}
                |Request Headers: ${request.headers}
                |══════════════════════════════════════════════════════════════
            """.trimMargin())
        } else {
            Log.d(TAG, "Request successful - ID: $requestId, URL: ${request.url}")
        }
        
        response
    }
    
    /**
     * Rate limiting interceptor for Jikan API (3 requests per second limit)
     */
    private val rateLimitInterceptor = Interceptor { chain ->
        // Simple rate limiting - wait a bit between requests
        Thread.sleep(350) // ~3 requests per second max
        chain.proceed(chain.request())
    }
    
    /**
     * Logging interceptor for debug builds
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    
    /**
     * OkHttp client for Jikan API
     */
    private val jikanOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(rateLimitInterceptor)
            .addInterceptor(requestIdInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * OkHttp client for MangaDex API with enhanced error handling
     */
    private val mangadexOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(mangadexErrorInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit instance for Jikan API
     */
    val jikanRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(JIKAN_BASE_URL)
            .client(jikanOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Retrofit instance for MangaDex API
     */
    val mangadexRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MANGADEX_BASE_URL)
            .client(mangadexOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Create Jikan API service instance
     */
    inline fun <reified T> createJikanService(): T {
        return jikanRetrofit.create(T::class.java)
    }
    
    /**
     * Create MangaDex API service instance
     */
    inline fun <reified T> createMangaDexService(): T {
        return mangadexRetrofit.create(T::class.java)
    }
    
    // Legacy support
    val retrofit: Retrofit get() = jikanRetrofit
    inline fun <reified T> createService(): T = createJikanService<T>()
}

/**
 * Sealed class for API results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val requestId: String? = null) : ApiResult<T>()
    data class Error(
        val code: Int,
        val message: String,
        val requestId: String? = null,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

/**
 * Extension function to safely handle API responses
 */
suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>
): ApiResult<T> {
    return try {
        val response = call()
        val requestId = response.headers()["X-Request-ID"]
        
        if (response.isSuccessful) {
            response.body()?.let {
                ApiResult.Success(it, requestId)
            } ?: ApiResult.Error(
                code = response.code(),
                message = "Empty response body",
                requestId = requestId
            )
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e("MangaAPI", """
                |API Error - Request ID: $requestId
                |Code: ${response.code()}
                |Error: $errorBody
            """.trimMargin())
            
            ApiResult.Error(
                code = response.code(),
                message = when (response.code()) {
                    400 -> "Bad request"
                    404 -> "Manga not found"
                    429 -> "Too many requests - please slow down"
                    500 -> "Server error"
                    else -> "Error: ${response.message()}"
                },
                requestId = requestId
            )
        }
    } catch (e: Exception) {
        Log.e("MangaAPI", "Network exception", e)
        ApiResult.Error(
            code = -1,
            message = e.localizedMessage ?: "Network error occurred",
            exception = e
        )
    }
}

