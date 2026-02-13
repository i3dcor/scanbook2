package com.i3dcor.scanbook.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * Singleton que provee las instancias de Retrofit para las APIs de libros.
 * - Open Library API (fuente primaria)
 * - Google Books API (fallback)
 */
object RetrofitClient {
    
    private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"
    private const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/"
    private const val TIMEOUT_SECONDS = 15L
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    // Retrofit para Open Library
    private val openLibraryRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // Retrofit para Google Books
    private val googleBooksRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_BOOKS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val openLibraryApi: OpenLibraryApi by lazy {
        openLibraryRetrofit.create(OpenLibraryApi::class.java)
    }
    
    val googleBooksApi: GoogleBooksApi by lazy {
        googleBooksRetrofit.create(GoogleBooksApi::class.java)
    }
}
