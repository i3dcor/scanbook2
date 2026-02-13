package com.i3dcor.scanbook.data.network

import com.i3dcor.scanbook.data.network.dto.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz Retrofit para Google Books API.
 * Permite buscar libros por ISBN usando el parámetro q=isbn:XXXXX
 */
interface GoogleBooksApi {
    
    /**
     * Busca volúmenes en Google Books.
     * @param query Consulta de búsqueda. Para ISBN usar formato "isbn:978XXXXXXXXXX"
     * @return Respuesta con la lista de volúmenes encontrados
     */
    @GET("books/v1/volumes")
    suspend fun searchByIsbn(@Query("q") query: String): GoogleBooksResponse
}
