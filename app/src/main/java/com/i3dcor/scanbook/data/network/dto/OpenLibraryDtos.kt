package com.i3dcor.scanbook.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de Open Library API al buscar por ISBN.
 * GET https://openlibrary.org/isbn/{isbn}.json
 */
data class OpenLibraryBookDto(
    val title: String?,
    val authors: List<AuthorRef>?,
    val publishers: List<String>?,
    @SerializedName("publish_date")
    val publishDate: String?,
    val covers: List<Int>?,
    val subjects: List<String>?
)

/**
 * Referencia a un autor en Open Library.
 * Contiene solo la key, requiere otra llamada para obtener el nombre.
 */
data class AuthorRef(
    val key: String
)

/**
 * DTO para la respuesta de autor de Open Library API.
 * GET https://openlibrary.org/authors/{authorKey}.json
 */
data class AuthorDto(
    val name: String?,
    @SerializedName("personal_name")
    val personalName: String?
)
