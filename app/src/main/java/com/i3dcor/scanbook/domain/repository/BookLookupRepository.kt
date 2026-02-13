package com.i3dcor.scanbook.domain.repository

import com.i3dcor.scanbook.domain.model.ScannedIsbn

/**
 * Repository para buscar información de libros por ISBN.
 * Interface definida en Domain - implementación en Data.
 */
interface BookLookupRepository {
    
    /**
     * Busca información de un libro por su ISBN en fuentes externas.
     * @param isbn El ISBN-13 del libro
     * @return Result con ScannedIsbn si se encuentra, o error si falla
     */
    suspend fun lookupByIsbn(isbn: String): Result<ScannedIsbn>
}
