package com.i3dcor.scanbook.data.network

import com.i3dcor.scanbook.data.network.dto.AuthorDto
import com.i3dcor.scanbook.data.network.dto.OpenLibraryBookDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface Retrofit para Open Library API.
 * Base URL: https://openlibrary.org/
 */
interface OpenLibraryApi {
    
    /**
     * Obtiene información de un libro por su ISBN.
     * @param isbn ISBN-13 o ISBN-10 del libro
     */
    @GET("isbn/{isbn}.json")
    suspend fun getBookByIsbn(@Path("isbn") isbn: String): OpenLibraryBookDto
    
    /**
     * Obtiene información de un autor por su key.
     * @param authorKey Key del autor (ej: "/authors/OL34184A")
     */
    @GET("{authorKey}.json")
    suspend fun getAuthor(@Path("authorKey", encoded = true) authorKey: String): AuthorDto
}
