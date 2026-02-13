package com.i3dcor.scanbook.domain.model

/**
 * Modelo de dominio que representa un ISBN escaneado con información del libro.
 * Kotlin puro, sin dependencias de Android.
 */
data class ScannedIsbn(
    val isbn: String,
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val price: Double? = null,
    val condition: String? = null,
    val coverUrl: String? = null
)
