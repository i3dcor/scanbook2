package com.i3dcor.scanbook.domain.model

/**
 * Excepción que indica que el libro no fue encontrado en ninguna fuente de datos.
 * Es una excepción de dominio (Kotlin puro, sin dependencias Android).
 */
class BookNotFoundException(
    val isbn: String
) : Exception("Book not found for ISBN: $isbn")
