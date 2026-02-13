package com.i3dcor.scanbook.data.network.dto

/**
 * DTOs para la respuesta de Google Books API.
 * Documentación: https://developers.google.com/books/docs/v1/using
 */

/**
 * Respuesta principal de búsqueda de volúmenes.
 */
data class GoogleBooksResponse(
    val totalItems: Int,
    val items: List<GoogleBookItem>?
)

/**
 * Representa un libro/volumen de Google Books.
 */
data class GoogleBookItem(
    val id: String,
    val volumeInfo: GoogleVolumeInfo
)

/**
 * Información detallada del volumen.
 */
data class GoogleVolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val imageLinks: GoogleImageLinks?,
    val categories: List<String>?
)

/**
 * Enlaces a las imágenes de portada del libro.
 */
data class GoogleImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)
