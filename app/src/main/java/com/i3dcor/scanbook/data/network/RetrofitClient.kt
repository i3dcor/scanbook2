package com.i3dcor.scanbook.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/**
 * Singleton que provee la instancia de Retrofit para Open Library API.
 */
object RetrofitClient {
    
    private const val BASE_URL = "https://openlibrary.org/"
    private const val TIMEOUT_SECONDS = 15L
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val openLibraryApi: OpenLibraryApi by lazy {
        retrofit.create(OpenLibraryApi::class.java)
    }
}
